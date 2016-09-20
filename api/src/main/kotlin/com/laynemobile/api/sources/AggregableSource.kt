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

import com.laynemobile.api.Aggregable
import com.laynemobile.api.aggregables.simpleAggregable
import com.laynemobile.api.processor.Extension
import com.laynemobile.api.processor.ProcessorBuilder
import io.reactivex.Observable

interface AggregableSource<in T : Any?> : (T) -> Aggregable

fun <T : Any?> ((T) -> Aggregable).toAggregableSource() = object : AggregableSource<T> {
    override fun invoke(p1: T): Aggregable = this@toAggregableSource(p1)
}

private class AggregableInterceptor<T : Any, R : Any>
internal constructor(
        private val source: (T) -> Aggregable
) : Extension.Interceptor<T, Observable<R>>() {

    override fun intercept(chain: Chain<T, Observable<R>>): Observable<R> {
        val params: T = chain.value
        val aggregable: Aggregable = source(params)

        return chain.proceed(params)
    }
}

fun <T : Any, R : Any> ProcessorBuilder<T, Observable<R>>.aggregate(block: (T) -> Aggregable) {
    extend { AggregableInterceptor(block) }
}

fun <T : Any, R : Any> ProcessorBuilder<T, Observable<R>>.aggregate() {
    aggregate { simpleAggregable(it) }
}
