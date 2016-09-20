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

import com.laynemobile.api.processor.*
import com.laynemobile.api.sources.aggregate
import com.laynemobile.api.sources.requireNetwork
import io.reactivex.Observable
import java.util.*

class Api<in T : Any, R : Any>
internal constructor(
        processor: ErrorHandlerProcessor<T, Observable<R>>,
        extensions: List<Extension<T, Observable<R>>> = emptyList()
) : InterceptProcessor<T, Observable<R>>(processor, extensions) {

    fun request(params: T): Observable<R> = invoke(params)
}

class ApiBuilder<T : Any, R : Any>
internal constructor() : ProcessorBuilder<T, Observable<R>>() {

    fun withProcessor(processor: ObservableProcessor<T, R>) {
        super.withProcessor(processor)
    }

    override fun withProcessor(function: (T) -> Observable<R>) {
        withProcessor(observableProcessor(function))
    }

    override fun build(): Api<T, R> = Api(
            processor = processor,
            extensions = extensions
    )
}

fun <T : Any, R : Any> buildApi(init: ApiBuilder<T, R>.() -> Unit): Api<T, R> {
    val builder = ApiBuilder<T, R>()
    builder.init()
    return builder.build()
}

fun main(args: Array<String>) {
    val api = buildApi<Int, String> {
        source { p: Int -> p.toString() }
        requireNetwork { it % 2 == 0 }
        aggregate()
    }

    api.request(Random().nextInt(1000))
            .map { it + 3 }
            .subscribe({
                println("num: $it")
            }, {
                println("error: ${it.message}")
            })
}
