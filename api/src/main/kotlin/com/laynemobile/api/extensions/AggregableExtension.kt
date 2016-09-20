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

import com.laynemobile.api.Aggregable
import com.laynemobile.api.aggregables.Aggregables
import com.laynemobile.processor.Extender
import com.laynemobile.processor.Extension
import io.reactivex.Observable

private class AggregableInterceptor<T : Any, R : Any>
internal constructor(
        private val aggregableSource: (T) -> Aggregable
) : Extension.Interceptor<T, Observable<R>>() {

    override fun invoke(chain: Chain<T, Observable<R>>): Observable<R> {
        val params: T = chain.value
        val aggregable: Aggregable = aggregableSource(params)

        return chain.proceed(params)
    }
}

fun <T : Any, R : Any> Extender<T, Observable<R>>.aggregate(block: (T) -> Aggregable) {
    extend { AggregableInterceptor(block) }
}

fun <T : Any, R : Any> Extender<T, Observable<R>>.aggregate() {
    aggregate { Aggregables.simple(it) }
}