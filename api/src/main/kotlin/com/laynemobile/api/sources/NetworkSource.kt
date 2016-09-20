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

package com.laynemobile.api.sources;

import com.laynemobile.api.exceptions.NetworkUnavailableException
import com.laynemobile.api.internal.ApiLog
import com.laynemobile.api.processor.Extension
import com.laynemobile.api.processor.ProcessorBuilder
import com.laynemobile.api.util.NetworkChecker

private class NetworkSourceChecker<in T : Any?>
internal constructor(
        private val isNetworkAvailable: (T) -> Boolean
) : Extension.Checker<T>() {

    private companion object {
        private val TAG = NetworkSourceChecker::class.java.simpleName
    }

    @Throws(Exception::class)
    override fun check(t: T) {
        ApiLog.d(TAG, "checking network connection")
        if (!isNetworkAvailable(t)) {
            ApiLog.i(TAG, "not running request. no network")
            throw NetworkUnavailableException("no network")
        }
    }
}

fun <T : Any> ProcessorBuilder<T, *>.requireNetwork(block: (T) -> Boolean) {
    extend { NetworkSourceChecker(block) }
}

fun ProcessorBuilder<*, *>.requireNetwork(networkChecker: NetworkChecker = NetworkChecker.ALWAYS_AVAILABLE) {
    requireNetwork { networkChecker.isNetworkAvailable() }
}
