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

package com.laynemobile.api

import com.laynemobile.processor.*
import io.reactivex.Observable

class Api<in T : Any, R : Any>
internal constructor(
        private val processor: ErrorHandlingProcessor<T, Observable<R>>,
        private val extensions: Extensions<T, Observable<R>>
) : Processor<T, Observable<R>> {

    override fun invoke(p1: T): Observable<R> {
        return processor.tryProcess(p1, extensions)
    }

    fun request(params: T): Observable<R> = invoke(params)
}

class ApiBuilder<T : Any, R : Any>
internal constructor(
        private val processor: ErrorHandlingProcessor<T, Observable<R>>
) : AbstractProcessorBuilder<T, Observable<R>, Api<T, R>>() {

    override fun build(extensions: Extensions<T, Observable<R>>) = Api(
            processor = processor,
            extensions = extensions
    )
}

fun <T : Any, R : Any> api(source: (T) -> R): ApiBuilder<T, R> {
    return ApiBuilder(source.toObservableProcessor())
}

fun <T : Any, R : Any> api(
        source: ((T) -> R),
        initializer: (Extender<T, Observable<R>>.() -> Unit)
): Api<T, R> {
    return api(source)
            .build(initializer)
}
