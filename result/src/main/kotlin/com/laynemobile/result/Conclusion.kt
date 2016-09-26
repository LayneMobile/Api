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


@file:JvmName("Conclusions")

package com.laynemobile.result

import java.io.IOException


internal interface ConclusionLike<out T : Any, out E : Throwable> {
    operator fun component1(): T?
    operator fun component2(): E?

    fun isSuccess(): Boolean

    fun get(): T

    fun toEither(): Either<E, T>

    fun toResult(): Result<T>

    fun toOutcome(): Outcome<T, E>
}

open class Conclusion<out T : Any, out E : Throwable>
private constructor(
        delegate: Either<T, E>,
        private val success: Boolean = delegate is Either.Left
) : OneOf<T, E>(delegate), ConclusionLike<T, E> {

    protected constructor(value: T) : this(Either.left(value))
    protected constructor(error: E) : this(Either.right(error))

    final override fun isSuccess() = success

    inline fun <R : Any?> fold(success: (T) -> R, failure: (E) -> R): R {
        return delegate.fold(success, failure)
    }

    final override fun get(): T {
        return fold(success = {
            it
        }, failure = {
            throw it
        })
    }

    final override fun toEither() = delegate.swap()

    final override fun toOutcome(): Outcome<T, E> = when (this) {
        is Outcome<T, E> -> this
        else -> fold(success = {
            Outcome.success<T, E>(it)
        }, failure = {
            Outcome.failure<T, E>(it)
        })
    }

    final override fun toResult(): Result<T> = when (this) {
        is Result<T> -> this
        else -> fold(success = {
            Result.success(it)
        }, failure = {
            Result.failure(it)
        })
    }

    override fun toString(): String {
        return fold(success = {
            "[Success: $it]"
        }, failure = {
            "[Failure: $it]"
        })
    }
}

inline fun <reified R : Any> Conclusion<*, *>.getAs(): R? {
    return fold(success = {
        it as? R
    }, failure = {
        it as? R
    })
}

inline fun <T : Any> Conclusion<T, *>.ifSuccess(success: (T) -> Unit) {
    return fold(success, {})
}

inline fun <E : Throwable> Conclusion<*, E>.ifFailure(failure: (E) -> Unit) {
    return fold({}, failure)
}

infix fun <V : Any> Conclusion<V, *>.getOrElse(fallback: V): V {
    return fold(success = {
        it
    }, failure = {
        fallback
    })
}

inline fun <V : Any> Conclusion<V, *>.any(predicate: (V) -> Boolean): Boolean {
    return fold(success = {
        predicate(it)
    }, failure = {
        false
    })
}

fun main(args: Array<String>) {
    val result: Result<String> = Result.success("hey")
    result.ifSuccess { println("Success: $it") }
    result.ifFailure { println("Error: ${it.message}") }

    val outcome: Outcome<String, IOException> = result.map { "$it-map" }
            .mapError { IOException(it) }
    outcome.ifSuccess { println(it) }

    val error = outcome.flatMap { Outcome.failure<String, IOException>(IOException("$it-errorMap")) }
    error.ifFailure { println("Error: ${it.message}") }
    println(error.getOrElse("orElse"))
}
