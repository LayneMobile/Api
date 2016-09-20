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

package com.laynemobile.api.subscribers;

import rx.Subscriber
import rx.Subscription
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater

abstract class NotifyingSubscriber<T> : Subscriber<T> {

    protected constructor() : super() {
        init()
    }

    protected constructor(op: Subscriber<*>) : super(op) {
        init()
    }

    protected constructor(
            subscriber: Subscriber<*>,
            shareSubscriptions: Boolean
    ) : super(subscriber, shareSubscriptions) {
        init()
    }

    private fun init() {
        add(NotifyingSubscription(this))
    }

    protected abstract fun onUnsubscribe()

    private class NotifyingSubscription
    internal constructor(
            private val parent: NotifyingSubscriber<*>
    ) : Subscription {
        @Volatile private var unsubscribed: Int = 0

        override fun unsubscribe() {
            if (UNSUBSCRIBED_UPDATER.compareAndSet(this, 0, 1)) {
                parent.onUnsubscribe()
            }
        }

        override fun isUnsubscribed(): Boolean {
            return unsubscribed != 0
        }

        companion object {
            private val UNSUBSCRIBED_UPDATER = AtomicIntegerFieldUpdater.newUpdater(NotifyingSubscription::class.java, "unsubscribed")
        }
    }

//    companion object {
//
//        fun <T> create(onNext: Action1<in T>, onUnsubscribe: Action0): NotifyingSubscriber<T> {
//            return create(ActionSubscriber(onNext), onUnsubscribe)
//        }
//
//        fun <T> create(onNext: Action1<in T>,
//                       onError: Action1<Throwable>,
//                       onUnsubscribe: Action0): NotifyingSubscriber<T> {
//            return create(ActionSubscriber(onNext, onError), onUnsubscribe)
//        }
//
//        fun <T> create(onNext: Action1<in T>,
//                       onError: Action1<Throwable>,
//                       onCompleted: Action0,
//                       onUnsubscribe: Action0): NotifyingSubscriber<T> {
//            return create(ActionSubscriber(onNext, onError, onCompleted), onUnsubscribe)
//        }
//
//        fun <T> create(actual: Subscriber<in T>, onUnsubscribe: Action0): NotifyingSubscriber<T> {
//            return object : NotifyingSubscriber<T>(actual) {
//                override fun onUnsubscribe() {
//                    onUnsubscribe.call()
//                }
//
//                override fun onCompleted() {
//                    actual.onCompleted()
//                }
//
//                override fun onError(e: Throwable) {
//                    actual.onError(e)
//                }
//
//                override fun onNext(t: T) {
//                    actual.onNext(t)
//                }
//            }
//        }
//    }
}
