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

import com.laynemobile.api.aggregables.simpleAggregable
import com.laynemobile.api.internal.ApiLog
import com.laynemobile.api.processor.source
import com.laynemobile.api.sources.aggregate
import com.laynemobile.api.sources.requireNetwork
import org.junit.After;
import org.junit.Assert
import org.junit.Before;
import org.junit.Test;

private val TAG: String = "ApiTest"

class ApiTest {
    @Before fun setup() {
        ApiLog.setLogger(ConsoleLogger())
    }

    @After fun teardown() {
    }

    @Test fun createApi() {
        val api = buildApi<Params, Any> {
            source { p ->
                ApiLog.d(TAG, "source: $p")
                p
            }
            requireNetwork {
                ApiLog.d(TAG, "checking network")
                true
            }
            aggregate { p ->
                ApiLog.d(TAG, "aggregating")
                simpleAggregable(key = p)
            }
            modify { params, observable ->
                ApiLog.d(TAG, "modifying")
                observable
            }
        }
        val expected = object : Params {}
        val result = api.invoke(expected)
                .blockingLast()
        Assert.assertEquals(expected, result)
    }
}
