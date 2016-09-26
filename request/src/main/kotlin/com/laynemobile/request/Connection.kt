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

import io.reactivex.disposables.Disposable
import io.reactivex.disposables.Disposables
import org.reactivestreams.Subscription

sealed class Connection {

    inline fun <R : Any?> fold(
            subscription: (Subscription) -> R,
            disposable: (Disposable) -> R
    ): R = when (this) {
        is AsSubscription -> subscription(this)
        is AsDisposable -> disposable(this)
    }

    fun disconnect(): Unit = when (this) {
        is AsSubscription -> cancel()
        is AsDisposable -> dispose()
    }

    fun toDisposable(): Disposable = when (this) {
        is AsSubscription -> Disposables.fromSubscription(this)
        is AsDisposable -> this
    }

    fun toSubscription(): Subscription = when (this) {
        is AsSubscription -> this
        is AsDisposable -> object : Subscription {
            override fun cancel() {
                dispose()
            }

            override fun request(n: Long) {
                // no-op
            }
        }
    }

    class AsSubscription
    internal constructor(
            delegate: Subscription
    ) : Subscription by delegate, Connection()

    class AsDisposable
    internal constructor(
            delegate: Disposable
    ) : Disposable by delegate, Connection()

    companion object {
        fun from(subscription: Subscription): Connection = Connection.AsSubscription(subscription)

        fun from(disposable: Disposable): Connection = Connection.AsDisposable(disposable)
    }
}
