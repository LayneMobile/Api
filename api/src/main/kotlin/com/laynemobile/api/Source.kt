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

@file:JvmName("SourceUtil")

package com.laynemobile.api

import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single

interface Source<out T : Any, R : Any> {
    fun source(source: (T) -> Request<R>): Unit

    class Builder<T : Any, R : Any>
    internal constructor() : Source<T, R>, com.laynemobile.api.Builder<(T) -> Request<R>> {
        private var _source: ((T) -> Request<R>)? = null

        override fun source(source: (T) -> Request<R>) {
            _source = source
        }

        override fun build(): (T) -> Request<R> {
            return _source!!
        }
    }

    companion object {
        fun <T : Any, R : Any> builder(): Builder<T, R> = Builder<T, R>()

        fun <T : Any, R : Any> build(init: Source<T, R>.() -> Unit): (T) -> Request<R> {
            val builder = Source.Builder<T, R>()
            builder.init()
            return builder.build()
        }
    }
}


fun <T : Any, R : Any> Source<T, R>.singleSource(source: (T) -> Single<R>): Unit {
    source { p1: T ->
        Request.from(source(p1))
    }
}

fun <T : Any, R : Any> Source<T, R>.observableSource(source: (T) -> Observable<R>): Unit {
    source { p1: T ->
        Request.from(source(p1))
    }
}

fun <T : Any, R : Any> Source<T, R>.flowableSource(source: (T) -> Flowable<R>): Unit {
    source { p1: T ->
        Request.from(source(p1))
    }
}

fun <T : Any, R : Any> Source<T, R>.callableSource(source: (T) -> R): Unit {
    source { p1: T ->
        Request.just(source(p1))
    }
}

inline fun <T : Any, R : Any> Source<T, R>.callableSource(block: () -> ((T) -> R)): Unit {
    callableSource(block())
}
