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

package com.laynemobile.tailor

import com.laynemobile.tailor.internal.DefaultApiBuilder

interface Api<in T : Any?, out R : Any?> {
    fun request(p1: T): R
}

fun <T : Any?, R : Any?> buildApi(init: ApiBuilder<T, R>.() -> Unit): Api<T, R> {
    val apiBuilder = DefaultApiBuilder<T, R>()
    apiBuilder.init()
    return apiBuilder.build()
}

fun <T : Any?, R : Any?> buildApi(source: Source<T, R>.() -> Unit, tailor: Tailor<T, R>.() -> Unit): Api<T, R> {
    return buildApi {
        source(source)
        tailor(tailor)
    }
}

inline fun <T : Any?, R : Any?> api(crossinline block: (T) -> R) = object : Api<T, R> {
    override fun request(p1: T): R = block(p1)
}

inline fun <T : Any?, R1 : Any?, R2 : Any?> Api<T, R1>.andThen(
        crossinline after: (R1) -> R2
): Api<T, R2> = api { p1 -> after(request(p1)) }

inline fun <T1 : Any?, T2 : Any, R : Any?> Api<T1, R>.compose(
        crossinline before: (T2) -> T1
): Api<T2, R> = api { p1 -> request(before(p1)) }

