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

package com.laynemobile.request

import io.reactivex.*
import org.reactivestreams.Publisher
import org.reactivestreams.Subscriber

interface RequestSource<in R : Any> {
    fun subscribe(receiver: R)
}

interface SingleRequest<T : Any> : RequestSource<SingleObserver<in T>>, SingleSource<T> {
    val delegate: Single<T>

    override fun subscribe(receiver: SingleObserver<in T>)
}

interface ObservableRequest<T : Any> : RequestSource<Observer<in T>>, ObservableSource<T> {
    val delegate: Observable<T>

    override fun subscribe(receiver: Observer<in T>)
}

interface FlowableRequest<T : Any> : RequestSource<Subscriber<in T>>, Publisher<T> {
    val delegate: Flowable<T>

    override fun subscribe(receiver: Subscriber<in T>)
}

interface DeferredRequest<out T : Any> : RequestSource<MultiReceiver<T>> {
    override fun subscribe(receiver: MultiReceiver<T>)
}

sealed class Request<T : Any> {

    inline fun <R : Any> fold(
            single: (Single<T>) -> R,
            observable: (Observable<T>) -> R,
            flowable: (Flowable<T>) -> R,
            deferred: (AsDeferred<T>) -> R
    ): R = when (this) {
        is AsSingle -> single(delegate)
        is AsObservable -> observable(delegate)
        is AsFlowable -> flowable(delegate)
        is AsDeferred -> deferred(this)
    }

    fun <R : Any> map(func: (T) -> R): Request<R> = when (this) {
        is AsSingle -> AsSingle(delegate.map(func))
        is AsObservable -> AsObservable(delegate.map(func))
        is AsFlowable -> AsFlowable(delegate.map(func))
        is AsDeferred -> defer { supplier().map(func) }
    }

    fun <R : Any> flatMap(func: (T) -> Request<R>): Request<R> = when (this) {
        is AsSingle -> AsSingle(delegate.flatMap { func(it).toSingle() })
        is AsObservable -> AsObservable(delegate.flatMap { func(it).toObservable() })
        is AsFlowable -> AsFlowable(delegate.flatMap { func(it).toFlowable() })
        is AsDeferred -> defer { supplier().flatMap(func) }
    }

    fun onErrorReturn(func: (Throwable) -> T): Request<T> = when (this) {
        is AsSingle -> AsSingle(delegate.onErrorReturn(func))
        is AsObservable -> AsObservable(delegate.onErrorReturn(func))
        is AsFlowable -> AsFlowable(delegate.onErrorReturn(func))
        is AsDeferred -> defer { supplier().onErrorReturn(func) }
    }

    fun toSingle(): Single<T> = when (this) {
        is AsSingle -> delegate
        is AsObservable -> delegate.toSingle()
        is AsFlowable -> delegate.toSingle()
        is AsDeferred -> Single.defer { supplier().toSingle() }
    }

    fun toObservable(): Observable<T> = when (this) {
        is AsSingle -> delegate.toObservable()
        is AsObservable -> delegate
        is AsFlowable -> delegate.toObservable()
        is AsDeferred -> Observable.defer { supplier().toObservable() }
    }

    fun toFlowable(strategy: BackpressureStrategy = BackpressureStrategy.BUFFER): Flowable<T> = when (this) {
        is AsSingle -> delegate.toFlowable()
        is AsObservable -> delegate.toFlowable(strategy)
        is AsFlowable -> delegate
        is AsDeferred -> Flowable.defer { supplier().toFlowable(strategy) }
    }

    fun toCompletable(): Completable = when (this) {
        is AsSingle -> delegate.toCompletable()
        is AsObservable -> delegate.toCompletable()
        is AsFlowable -> delegate.toCompletable()
        is AsDeferred -> Completable.defer { supplier().toCompletable() }
    }

    fun subscribe(receiver: MultiReceiver<T>): Unit = when (this) {
        is AsSingle -> delegate.subscribe(receiver.toSingleObserver())
        is AsObservable -> delegate.subscribe(receiver.toObserver())
        is AsFlowable -> delegate.subscribe(receiver.toSubscriber())
        is AsDeferred -> supplier().subscribe(receiver)
    }

    fun observeOn(scheduler: Scheduler): Request<T> = when (this) {
        is AsSingle -> AsSingle(delegate.observeOn(scheduler))
        is AsObservable -> AsObservable(delegate.observeOn(scheduler))
        is AsFlowable -> AsFlowable(delegate.observeOn(scheduler))
        is AsDeferred -> defer { supplier().observeOn(scheduler) }
    }

    fun subscribeOn(scheduler: Scheduler): Request<T> = when (this) {
        is AsSingle -> AsSingle(delegate.subscribeOn(scheduler))
        is AsObservable -> AsObservable(delegate.subscribeOn(scheduler))
        is AsFlowable -> AsFlowable(delegate.subscribeOn(scheduler))
        is AsDeferred -> defer { supplier().subscribeOn(scheduler) }
    }

    class AsSingle<T : Any>
    internal constructor(
            override val delegate: Single<T>
    ) : Request<T>(), SingleRequest<T> {
        override fun subscribe(receiver: SingleObserver<in T>) {
            delegate.subscribe(receiver)
        }
    }

    class AsObservable<T : Any>
    internal constructor(
            override val delegate: Observable<T>
    ) : Request<T>(), ObservableRequest<T> {
        override fun subscribe(receiver: Observer<in T>) {
            delegate.subscribe(receiver)
        }
    }

    class AsFlowable<T : Any>
    internal constructor(
            override val delegate: Flowable<T>
    ) : Request<T>(), FlowableRequest<T> {
        override fun subscribe(receiver: Subscriber<in T>) {
            delegate.subscribe(receiver)
        }
    }

    class AsDeferred<T : Any>
    internal constructor(
            internal val supplier: () -> Request<T>
    ) : Request<T>(), DeferredRequest<T> {

    }

    companion object {
        fun <T : Any> just(item: T): Request<T> {
            return AsSingle(item.toSingle())
        }

        fun <T : Any> from(single: Single<T>): Request<T> {
            return AsSingle(single)
        }

        fun <T : Any> create(source: SingleOnSubscribe<T>): Request<T> {
            return from(source.toSingle())
        }

        fun <T : Any> from(observable: Observable<T>): Request<T> {
            return AsObservable(observable)
        }

        fun <T : Any> create(source: ObservableOnSubscribe<T>): Request<T> {
            return AsObservable(source.toObservable())
        }

        fun <T : Any> from(flowable: Flowable<T>): Request<T> {
            return AsFlowable(flowable)
        }

        fun <T : Any> create(
                source: FlowableOnSubscribe<T>,
                mode: FlowableEmitter.BackpressureMode = FlowableEmitter.BackpressureMode.BUFFER
        ): Request<T> {
            return AsFlowable(source.toFlowable(mode))
        }

        fun <T : Any> error(exception: Throwable): Request<T> {
            return from(Single.error(exception))
        }

        fun <T : Any> defer(supplier: () -> Request<T>): Request<T> {
            return AsDeferred(supplier)
        }
    }
}

fun <T : Any> T.toSingle(): Single<T> = Single.just(this)
fun <T : Any> T.toRequest(): Request<T> = Request.from(toSingle())


fun <T : Any> Throwable.toSingle(): Single<T> = Single.error(this)
fun <T : Any> Throwable.toRequest(): Request<T> = Request.from(toSingle())


fun <T : Any> Single<T>.toRequest(): Request<T> = Request.from(this)
fun <T : Any> SingleOnSubscribe<T>.toSingle(): Single<T> = Single.create(this)
fun <T : Any> SingleOnSubscribe<T>.toRequest(): Request<T> = Request.from(toSingle())


fun <T : Any> Observable<T>.toRequest(): Request<T> = Request.from(this)
fun <T : Any> ObservableOnSubscribe<T>.toObservable(): Observable<T> = Observable.create(this)
fun <T : Any> ObservableOnSubscribe<T>.toRequest(): Request<T> = Request.from(toObservable())


fun <T : Any> Flowable<T>.toRequest(): Request<T> = Request.from(this)

fun <T : Any> FlowableOnSubscribe<T>.toFlowable(
        mode: FlowableEmitter.BackpressureMode = FlowableEmitter.BackpressureMode.BUFFER
): Flowable<T> {
    return Flowable.create(this, mode)
}

fun <T : Any> FlowableOnSubscribe<T>.toRequest(
        mode: FlowableEmitter.BackpressureMode = FlowableEmitter.BackpressureMode.BUFFER
): Request<T> {
    return Request.from(toFlowable(mode))
}
