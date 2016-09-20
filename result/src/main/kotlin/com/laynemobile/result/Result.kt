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

@file:JvmName("Results")

package com.laynemobile.result

sealed class Result<out T : Any> : Conclusion<T, Throwable> {
    constructor(value: T) : super(value)
    constructor(error: Throwable) : super(error)

    class Success<out T : Any>
    private constructor(
            val value: T
    ) : Result<T>(value) {
        override fun component1(): T = value
        override fun component2(): Throwable? = null

        fun generify(): Result<T> = this

        internal companion object {
            internal fun <T : Any> create(value: T): Result<T> = Success(value)
        }
    }

    class Failure
    private constructor(
            val error: Throwable
    ) : Result<Nothing>(error) {
        override fun component1(): Nothing? = null
        override fun component2(): Throwable = error

        fun <T : Any> generify(): Result<T> = this

        internal companion object {
            internal fun <T : Any> create(error: Throwable): Result<T> = Failure(error)
        }
    }

    companion object {
        @JvmStatic
        fun <T : Any> success(value: T): Result<T> = Success.create(value)

        @JvmStatic
        fun <T : Any> failure(error: Throwable): Result<T> = Failure.create(error)
    }
}

inline fun <T : Any> resultTry(block: () -> T?): Result<T> = try {
    Result.success(block()!!)
} catch (error: Throwable) {
    Result.failure(error)
}

fun <T : Any> Throwable.toResult(): Result<T> = Result.failure(this)

fun <T : Any> T?.toResult(): Result<T> = resultTry { this@toResult }

infix fun <V : Any> Result<V>.or(fallback: V): Result<V> = when (this) {
    is Result.Success -> this
    else -> Result.success(fallback)
}

inline fun <T : Any, R : Any> Result<T>.map(
        transform: (T) -> R
): Result<R> = when (this) {
    is Result.Success -> Result.success(transform(value))
    is Result.Failure -> Result.failure(error)
}

inline fun <T : Any, R : Any> Result<T>.flatMap(
        transform: (T) -> Result<R>
): Result<R> = when (this) {
    is Result.Success -> transform(value)
    is Result.Failure -> Result.failure(error)
}

fun <T : Any, E : Throwable> Result<T>.mapError(
        transform: (Throwable) -> E
): Outcome<T, E> = when (this) {
    is Result.Success -> Outcome.success(value)
    is Result.Failure -> Outcome.failure(transform(error))
}

fun <T : Any, E : Throwable> Result<T>.flatMapError(
        transform: (Throwable) -> Outcome<T, E>
): Outcome<T, E> = when (this) {
    is Result.Success -> Outcome.success(value)
    is Result.Failure -> transform(error)
}
