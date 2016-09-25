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

package com.laynemobile.api;

import com.laynemobile.api.extensions.SimpleAggregable
import com.laynemobile.api.extensions.aggregate
import com.laynemobile.api.extensions.requireNetwork
import com.laynemobile.api.internal.ApiLog
import com.laynemobile.api.internal.request.fold
import com.laynemobile.api.Api
import com.laynemobile.api.buildApi
import com.laynemobile.api.modify
import com.laynemobile.api.observableSource
import io.reactivex.Notification
import io.reactivex.Observable
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import java.util.*

private val TAG: String = "ApiTest"

class ApiTest {
    @Before fun setup() {
        ApiLog.setLogger(ConsoleLogger())
    }

    @Test fun createApi() {
        fun _execute(param: Int, log: Boolean): String {
            if (log) {
                ApiLog.d(TAG, "executing: param = $param")
            }
            return "${param + 10}"
        }

        fun execute(param: Int): String = _execute(param, true)

        val api = buildApi<Int, Observable<String>>({ observableSource(::execute) }) {
            requireNetwork {
                ApiLog.d(TAG, "checking network")
                true
            }
            aggregate { p ->
                ApiLog.d(TAG, "aggregating")
                SimpleAggregable(key = p)
            }
            modify { params, observable ->
                ApiLog.d(TAG, "modifying")
                observable
            }
        }

        val param: Int = 5
        var result: String? = null
        var error: Throwable? = null
        api.request(param)
                .materialize()
                .doOnNext { n: Notification<String> ->
                    n.fold(onNext = {
                        result = it
                    }, onError = {
                        error = it
                    }, onComplete = {})
                }
                .blockingLast()
        result?.let { ApiLog.d(TAG, "result: $it") }
        error?.let { ApiLog.e(TAG, "error", it) }
        assertEquals(_execute(param, false), result)
    }

    @Test fun createApi2() {
        fun isNetworkAvailable(p1: Int): Boolean = (p1 % 2) == 0
        fun mapper(p1: String): String = p1 + 3

        val api: Api<Int, Observable<String>>
                = buildApi({ observableSource(Int::toString) }) {
            requireNetwork(::isNetworkAvailable)
            aggregate()
        }

        val p1: Int = Random().nextInt()
        var onNext: String? = null
        var onError: Throwable? = null
        api.request(p1)
                .map(::mapper)
                .materialize()
                .doOnNext { n: Notification<String> ->
                    n.fold(onNext = {
                        onNext = it
                    }, onError = {
                        onError = it
                    }, onComplete = {})
                }
                .blockingLast()

        if (isNetworkAvailable(p1)) {
            assertEquals(mapper("$p1"), onNext)
            println("onNext: $onNext")
        } else {
            onError?.let {
                println("onError: ${it.message}?")
                it
            } ?: fail("expected onError but was null")
        }
    }
}
