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
import rx.exceptions.OnErrorNotImplementedException


/**
 * Helper class to encapsulate actions into a subscriber.

 * @param
 */
class ActionSubscriber<T>
@JvmOverloads constructor(
        private val onNext: ((T) -> Unit)? = null,
        private val onError: ((Throwable) -> Unit)? = null,
        private val onCompleted: (() -> Unit)? = null
) : Subscriber<T>() {

    override fun onNext(t: T) {
        onNext?.invoke(t)
    }

    override fun onError(e: Throwable) {
        onError?.invoke(e) ?: throw OnErrorNotImplementedException(e)
    }

    override fun onCompleted() {
        onCompleted?.invoke()
    }
}
