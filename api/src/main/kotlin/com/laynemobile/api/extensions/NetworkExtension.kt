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

@file:JvmName("NetworkExtension")

package com.laynemobile.api.extensions;

import com.laynemobile.api.exceptions.NetworkUnavailableException
import com.laynemobile.api.internal.ApiLog
import com.laynemobile.processor.Extender
import com.laynemobile.processor.Extension

private class NetworkValidator<in T : Any?>
internal constructor(
        private val isNetworkAvailable: (T) -> Boolean
) : Extension.Validator<T>() {

    private companion object {
        private val TAG = NetworkValidator::class.java.simpleName
    }

    @Throws(NetworkUnavailableException::class)
    override fun invoke(param: T) {
        ApiLog.d(TAG, "checking network connection")
        if (!isNetworkAvailable(param)) {
            ApiLog.i(TAG, "not running request. no network")
            throw NetworkUnavailableException("no network")
        }
    }
}

// TODO: better default
private fun <T : Any?> ((T) -> Boolean)?.orDefault(): (T) -> Boolean = this ?: { true }

fun <T : Any> Extender<T, *>.requireNetwork(isNetworkAvailable: ((T) -> Boolean)? = null) {
    extend { NetworkValidator(isNetworkAvailable.orDefault()) }
}
