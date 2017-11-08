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

@file:JvmName("Util")

package com.laynemobile.util

import java.util.*

fun <T : Any> List<T?>.copy(): List<T> = when (size) {
    0 -> emptyList()
    1 -> this[0]?.let { listOf(it) } ?: emptyList()
    else -> filterNotNullTo(ArrayList(size))
}