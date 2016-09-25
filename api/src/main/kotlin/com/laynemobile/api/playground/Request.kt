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

package com.laynemobile.api.playground

import io.reactivex.*
import org.reactivestreams.Publisher
import org.reactivestreams.Subscriber

interface RequestSource<in R : Any> {
    fun subscribe(receiver: R)
}

interface SingleRequest<T : Any> : RequestSource<SingleObserver<in T>>, SingleSource<T> {
    override fun subscribe(receiver: SingleObserver<in T>)
}

interface ObservableRequest<T : Any> : RequestSource<Observer<in T>>, ObservableSource<T> {
    override fun subscribe(receiver: Observer<in T>)
}

interface FlowableRequest<T : Any> : RequestSource<Subscriber<in T>>, Publisher<T> {
    override fun subscribe(receiver: Subscriber<in T>)
}

sealed class Request<T : Any> {
    abstract val delegate: Any

    class AsSingle<T : Any>(
            override val delegate: Single<T>
    ) : Request<T>(), SingleRequest<T> {
        override fun subscribe(receiver: SingleObserver<in T>) {
            delegate.subscribe(receiver)
        }
    }

    class AsObservable<T : Any>(
            override val delegate: Observable<T>
    ) : Request<T>(), ObservableRequest<T> {
        override fun subscribe(receiver: Observer<in T>) {
            delegate.subscribe(receiver)
        }
    }

    class AsFlowable<T : Any>(
            override val delegate: Flowable<T>
    ) : Request<T>(), FlowableRequest<T> {
        override fun subscribe(receiver: Subscriber<in T>) {
            delegate.subscribe(receiver)
        }
    }
}

fun <T : Any, R : Any> Request<T>.map(mapper: (T) -> R): Request<R> = when (this) {
    is Request.AsSingle -> Request.AsSingle(delegate.map(mapper))
    is Request.AsObservable -> Request.AsObservable(delegate.map(mapper))
    is Request.AsFlowable -> Request.AsFlowable(delegate.map(mapper))
}

