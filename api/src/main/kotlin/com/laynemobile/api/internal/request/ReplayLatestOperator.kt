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

import com.laynemobile.api.internal.ApiLog
import io.reactivex.Flowable
import io.reactivex.FlowableOperator
import io.reactivex.subscribers.DisposableSubscriber
import org.reactivestreams.Subscriber
import java.util.concurrent.atomic.AtomicReference


private val TAG = ReplayLatestOperator::class.java.simpleName

private fun <T : Any> postLatest(
        operator: ReplayLatestOperator<T>,
        subscriber: Subscriber<in T>,
        lastUpdated: AtomicReference<NotificationNode<T>>,
        subscribedToSource: Boolean
): Boolean {

    val latestNode: NotificationNode<T>
    val currentNode: NotificationNode<T>
    while (true) {
        val _latestNode = operator.latest.get()
        val _currentNode = lastUpdated.get()
        if (_latestNode === _currentNode) {
            ApiLog.d(TAG, "already current")
            return false
        } else if (!lastUpdated.compareAndSet(_currentNode, _latestNode)) {
            ApiLog.w(TAG, "current was updated :(")
            continue
        }
        latestNode = _latestNode
        currentNode = _currentNode
        break
    }

    val next = latestNode.current ?: return false
    val finished = next.foldFinished(onComplete = {
        operator.sleep()
        for (prev in latestNode.prevList(currentNode)) {
            prev.ifOnNext { subscriber.onNext(prev.value) }
        }
        subscriber.onComplete()
    }, onError = {
        operator.sleep()
        for (prev in latestNode.prevList(currentNode)) {
            prev.ifOnNext { subscriber.onNext(it) }
        }
        subscriber.onError(it)
    }, onNext = {
        for (prev in latestNode.prevList(currentNode)) {
            prev.ifOnNext { subscriber.onNext(it) }
        }
        subscriber.onNext(it)
    })

    operator.lastUpdatedTime = System.currentTimeMillis()

    return finished
}

internal class ReplayLatestOperator<T : Any>
internal constructor(
        internal val latest: Latest<NotificationNode<T>>
) : FlowableOperator<T, Flowable<T>> {

    @Volatile internal var lastUpdatedTime: Long = 0

    override fun apply(subscriber: Subscriber<in T>): Subscriber<in Flowable<T>> {
        return ReplaySubscriber(subscriber, { subscriber, lastUpdated, subscribedToSource ->
            postLatest(this, subscriber, lastUpdated, subscribedToSource)
        })
    }

    internal fun sleep() {
        val currentTime = System.currentTimeMillis()
        val diff = currentTime - lastUpdatedTime
        if (diff < MIN_POST_DIFF_MILLIS) {
            val sleep = MIN_POST_DIFF_MILLIS - diff
            try {
                Thread.sleep(sleep)
            } catch (e: InterruptedException) {
                // Ignore
            }
        }
    }

    companion object {
        private val MIN_POST_DIFF_MILLIS: Long = 25
    }
}

private abstract class BaseSubscriber<T : Any, S : Any?>
protected constructor(
        protected val subscriber: Subscriber<in T>,
        protected val subscribedToSource: Boolean = false,
        protected val current: AtomicReference<NotificationNode<T>> = AtomicReference<NotificationNode<T>>(),
        protected val postLatest: (Subscriber<in T>, AtomicReference<NotificationNode<T>>, Boolean) -> Boolean
) : DisposableSubscriber<S>() {

    protected constructor(
            parent: BaseSubscriber<T, *>,
            subscribedToSource: Boolean = false
    ) : this(
            subscriber = parent.subscriber,
            subscribedToSource = subscribedToSource,
            current = parent.current,
            postLatest = parent.postLatest
    )

    fun postLatest(): Boolean {
        return postLatest.invoke(subscriber, current, subscribedToSource)
    }
}


private class ReplaySubscriber<T : Any>
internal constructor(
        subscriber: Subscriber<in T>,
        postLatest: (Subscriber<in T>, AtomicReference<NotificationNode<T>>, Boolean) -> Boolean
) : BaseSubscriber<T, Flowable<T>>(
        subscriber = subscriber,
        subscribedToSource = false,
        postLatest = postLatest
) {
    override fun onComplete() {
        // we only expect one
    }

    override fun onError(e: Throwable) {
        // shouldn't ever happen
        subscriber.onError(e)
    }

    override fun onNext(flowable: Flowable<T>) {
        // If we've already completed, no need to subscribe to the source
        if (postLatest()) {
            ApiLog.d(TAG, "already complete. not subscribing to the source")
            return
        }

        // subscribe since the source is not finished
        ApiLog.d(TAG, "subscribing to source")
        flowable.subscribe(SourceSubscriber(this))
    }
}

private class SourceSubscriber<T : Any>
internal constructor(
        parent: ReplaySubscriber<T>
) : BaseSubscriber<T, T>(parent, true) {

    override fun onComplete() {
        // do this to ensure nothing was missed
        if (!postLatest()) {
            subscriber.onComplete()
        }
    }

    override fun onError(e: Throwable) {
        // do this to ensure nothing was missed
        if (!postLatest()) {
            subscriber.onError(e)
        }
    }

    override fun onNext(t: T) {
        ApiLog.d(TAG, "onNext from source")
        postLatest()
    }

    fun syncLatest() {
        postLatest()
    }
}
