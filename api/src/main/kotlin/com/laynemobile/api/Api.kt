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

import com.laynemobile.request.Request
import com.laynemobile.request.toRequest

interface Api<in T : Any, R : Any> {
    fun request(p1: T): Request<R>

    class Builder<T : Any, R : Any>
    internal constructor() : ApiBuilder<T, R> {
        private val sourceBuilder: Source.Builder<T, R> = Source.builder()
        private val tailorBuilder: Tailor.Builder<T, R> = Tailor.builder()

        override fun source(source: (T) -> Request<R>) {
            sourceBuilder.source(source)
        }

        override fun alter(alteration: Alteration<T, R>) {
            tailorBuilder.alter(alteration)
        }

        override fun build(): Api<T, R> {
            val source = sourceBuilder.build()
            val tailor = tailorBuilder.build()
            return tailor(source)
        }
    }

    companion object {
        inline fun <T : Any, R : Any> create(crossinline source: (T) -> Request<R>) = object : Api<T, R> {
            override fun request(p1: T): Request<R> = try {
                source(p1)
            } catch (e: Throwable) {
                e.toRequest<R>()
            }
        }

        fun <T : Any, R : Any> builder(): Builder<T, R> = Builder()

        inline fun <T : Any, R : Any> build(init: ApiBuilder<T, R>.() -> Unit): Api<T, R> {
            val builder = builder<T, R>()
            builder.init()
            return builder.build()
        }

        inline fun <T : Any, R : Any> build(source: Source<T, R>.() -> Unit, tailor: Tailor<T, R>.() -> Unit): Api<T, R> {
            val builder = builder<T, R>()
            builder.source()
            builder.tailor()
            return builder.build()
        }
    }
}
