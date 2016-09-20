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

@file:JvmName("Outcomes")

package com.laynemobile.api.util

sealed class Outcome<out T : Any, out E : Throwable> : Conclusion<T, E> {

    constructor(value: T) : super(value)

    constructor(error: E) : super(error)

    class Success<out T : Any>
    private constructor(
            val value: T
    ) : Outcome<T, Nothing>(value) {
        override fun component1(): T = value
        override fun component2(): Nothing? = null

        fun <E : Throwable> generify(): Outcome<T, E> = this

        internal companion object {
            internal fun <T : Any, E : Throwable> create(value: T): Outcome<T, E> = Success(value)
        }
    }

    class Failure<out E : Throwable>
    private constructor(
            val error: E
    ) : Outcome<Nothing, E>(error) {
        override fun component1(): Nothing? = null
        override fun component2(): E = error

        fun <T : Any> generify(): Outcome<T, E> = this

        internal companion object {
            internal fun <T : Any, E : Throwable> create(error: E): Outcome<T, E> = Failure(error)
        }
    }

    companion object {
        @JvmStatic
        fun <T : Any, E : Throwable> success(value: T): Outcome<T, E> = Success.create(value)

        @JvmStatic
        fun <T : Any, E : Throwable> failure(error: E): Outcome<T, E> = Failure.create(error)
    }
}

infix fun <T : Any, E : Throwable> Outcome<T, E>.or(fallback: T): Outcome<T, E> = when (this) {
    is Outcome.Success -> this
    else -> Outcome.success(fallback)
}

inline fun <V : Any, V2 : Any, E : Throwable> Outcome<V, E>.map(
        transform: (V) -> V2
): Outcome<V2, E> = when (this) {
    is Outcome.Success -> Outcome.success(transform(value))
    is Outcome.Failure -> Outcome.failure(error)
}

inline fun <V : Any, V2 : Any, E : Throwable> Outcome<V, E>.flatMap(
        transform: (V) -> Outcome<V2, E>
): Outcome<V2, E> = when (this) {
    is Outcome.Success -> transform(value)
    is Outcome.Failure -> Outcome.failure(error)
}

inline fun <V : Any, E : Throwable, E2 : Throwable> Outcome<V, E>.mapError(
        transform: (E) -> E2
): Outcome<V, E2> = when (this) {
    is Outcome.Success -> Outcome.success(value)
    is Outcome.Failure -> Outcome.failure(transform(error))
}

inline fun <V : Any, E : Throwable, E2 : Throwable> Outcome<V, E>.flatMapError(
        transform: (E) -> Outcome<V, E2>
): Outcome<V, E2> = when (this) {
    is Outcome.Success -> Outcome.success(value)
    is Outcome.Failure -> transform(error)
}

