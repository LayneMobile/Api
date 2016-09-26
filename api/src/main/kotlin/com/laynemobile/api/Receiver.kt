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

package com.laynemobile.api

import io.reactivex.Observer
import io.reactivex.SingleObserver
import io.reactivex.disposables.Disposable
import org.reactivestreams.Subscriber
import org.reactivestreams.Subscription

interface OnSubscribeAction {
    fun onSubscribe(connection: Connection)
}

interface OnErrorAction {
    fun onError(error: Throwable)
}

interface OnSuccessAction<in T : Any> {
    fun onSuccess(value: T)
}

interface OnNextAction<in T : Any> {
    fun onNext(next: T)
}

interface OnCompleteAction {
    fun onComplete()
}

interface ReceiverSource : OnSubscribeAction, OnErrorAction

interface SingleReceiver<in T : Any> : ReceiverSource, OnSuccessAction<T>

interface MultiReceiver<in T : Any> : ReceiverSource, OnNextAction<T>, OnCompleteAction

fun <T : Any> MultiReceiver<T>.toSingleObserver() = object : SingleObserver<T> {
    override fun onSubscribe(d: Disposable) {
        this@toSingleObserver.onSubscribe(Connection.from(d))
    }

    override fun onSuccess(value: T) {
        this@toSingleObserver.onNext(value)
        this@toSingleObserver.onComplete()
    }

    override fun onError(e: Throwable) {
        this@toSingleObserver.onError(e)
    }
}


fun <T : Any> MultiReceiver<T>.toObserver() = object : Observer<T> {
    override fun onSubscribe(d: Disposable) {
        this@toObserver.onSubscribe(Connection.from(d))
    }

    override fun onNext(value: T) {
        this@toObserver.onNext(value)
    }

    override fun onError(e: Throwable) {
        this@toObserver.onError(e)
    }

    override fun onComplete() {
        this@toObserver.onComplete()
    }
}

fun <T : Any> MultiReceiver<T>.toSubscriber() = object : Subscriber<T> {
    override fun onSubscribe(s: Subscription) {
        this@toSubscriber.onSubscribe(Connection.from(s))
    }

    override fun onNext(t: T) {
        this@toSubscriber.onNext(t)
    }

    override fun onError(t: Throwable) {
        this@toSubscriber.onError(t)
    }

    override fun onComplete() {
        this@toSubscriber.onComplete()
    }
}
