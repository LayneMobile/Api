/*
 * Copyright 2016 Layne Mobile, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.laynemobile.api.internal.request

import com.laynemobile.api.exceptions.SourceCancelledException
import com.laynemobile.api.extensions.Aggregable
import com.laynemobile.api.internal.ApiLog
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Notification
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.processors.PublishProcessor
import io.reactivex.schedulers.Schedulers
import io.reactivex.subscribers.ResourceSubscriber
import org.reactivestreams.Subscription
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater
import java.util.concurrent.atomic.AtomicReference

fun <T : Any> Flowable<T>.nest(): Flowable<Flowable<T>> = Flowable.just(this)

internal class Aggregate<T : Any>
internal constructor(
        private val aggregable: Aggregable,
        source: Flowable<T>,
        onComplete: (Aggregable) -> Unit = {}
) : Disposable {

    val request: Flowable<T>

    private val publisher: PublishProcessor<T> = PublishProcessor.create<T>()
    private val source: Flowable<T> = source.subscribeOn(Schedulers.io())
    private val latest: AtomicReference<NotificationNode<T>> = AtomicReference(NotificationNode<T>())
    private val disposable: CompositeDisposable = CompositeDisposable()

    @Volatile private var subscribed: Int = 0
    @Volatile private var completed: Int = 0

    private val complete = completeFunction(onComplete)
    private val onSubscribe = onSubscribeFunction()

    internal constructor(aggregable: Aggregable, source: Observable<T>, onComplete: (Aggregable) -> Unit) : this(
            aggregable = aggregable,
            source = source.toFlowable(BackpressureStrategy.BUFFER),
            onComplete = onComplete
    )

    init {
        this.request = publisher.doOnSubscribe(onSubscribe)
                .nest()
                .lift(ReplayLatestOperator(AtomicLatest(latest)))
                .defaultIfEmpty(null)
    }

    fun hasSubscribed(): Boolean {
        return subscribed != 0
    }

    fun isCompleted(): Boolean {
        return completed != 0
    }

    override fun isDisposed(): Boolean {
        return hasSubscribed() && disposable.isDisposed
    }

    override fun dispose() {
        if (SUBSCRIBED_UPDATER.compareAndSet(this, 1, 2)) {
            // We've subscribed
            val d = disposable
            if (!d.isDisposed) {
                d.dispose()
                val e = SourceCancelledException()
                updateLatest(Notification.createOnError<T>(e))
                publisher.onError(e)
            }
        }
        SUBSCRIBED_UPDATER.set(this, 2)
        complete()
    }

    private fun updateLatest(notification: Notification<T>) {
        var prev = this.latest.get()
        var latest = prev + notification
        while (!this.latest.compareAndSet(prev, latest)) {
            prev = this.latest.get()
            latest = prev + notification
        }
    }

    private fun scheduleComplete() {
        val keepAliveSeconds = aggregable.keepAliveSeconds
        if (keepAliveSeconds <= 0) {
            complete()
            return
        }
        Schedulers.computation()
                .createWorker()
                .schedule(complete, keepAliveSeconds.toLong(), TimeUnit.SECONDS)
    }

    private fun completeFunction(block: (Aggregable) -> Unit) = fun() {
        if (COMPLETED_UPDATER.compareAndSet(this, 0, 1)) {
            block(aggregable)
        }
    }

    private fun onSubscribeFunction() = fun(subscription: Subscription): Unit {
        // Subscribe to the source observable here
        if (SUBSCRIBED_UPDATER.compareAndSet(this, 0, 1) && !isDisposed) {
            ApiLog.d(TAG, "onSubscribe aggregate source")
            val d = source.subscribeWith(object : ResourceSubscriber<T>() {
                override fun onComplete() {
                    if (!isDisposed) {
                        updateLatest(Notification.createOnComplete<T>())
                        publisher.onComplete()
                        scheduleComplete()
                    }
                }

                override fun onError(e: Throwable) {
                    if (!isDisposed) {
                        updateLatest(Notification.createOnError<T>(e))
                        publisher.onError(e)
                        if (aggregable.keepAliveOnError) {
                            scheduleComplete()
                        } else {
                            complete()
                        }
                    }
                }

                override fun onNext(t: T) {
                    if (!isDisposed) {
                        updateLatest(Notification.createOnNext(t))
                        publisher.onNext(t)
                    }
                }
            })
            disposable.add(d)

            // handle thread-race condition
            if (d.isDisposed) {
                disposable.dispose()
                val e = SourceCancelledException()
                updateLatest(Notification.createOnError<T>(e))
                publisher.onError(e)
            }
        }
    }

    private companion object {
        private val TAG = Aggregate::class.java.simpleName
        private val SUBSCRIBED_UPDATER = AtomicIntegerFieldUpdater.newUpdater<Aggregate<*>>(Aggregate::class.java, "subscribed")
        private val COMPLETED_UPDATER = AtomicIntegerFieldUpdater.newUpdater<Aggregate<*>>(Aggregate::class.java, "completed")
    }
}

private class AtomicLatest<out T : Any>
internal constructor(
        private val reference: AtomicReference<T>
) : Latest<T> {
    override fun get(): T = reference.get()
}
