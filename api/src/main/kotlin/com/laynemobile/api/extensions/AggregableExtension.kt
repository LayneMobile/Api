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

@file:JvmName("AggregableExtension")

package com.laynemobile.api.extensions;

import com.laynemobile.api.Tailor
import com.laynemobile.api.alter
import com.laynemobile.api.internal.request.AggregableProcessor

interface Aggregable {
    val key: Any
    val keepAliveSeconds: Int
    val keepAliveOnError: Boolean
}

data class SimpleAggregable
@JvmOverloads constructor(
        override val key: Any,
        override val keepAliveSeconds: Int = 10,
        override val keepAliveOnError: Boolean = false
) : Aggregable

fun <T : Any, R : Any> Tailor<T, R>.aggregate(function: (T) -> Aggregable) {
    alter { AggregableProcessor(function) }
}

fun <T : Any, R : Any> Tailor<T, R>.aggregate() {
    aggregate { SimpleAggregable(it) }
}
