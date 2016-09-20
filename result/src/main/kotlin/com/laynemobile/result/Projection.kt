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

@file:JvmName("Projections")

package com.laynemobile.result

import java.util.*

open class Projection<out T : Any>
protected constructor(
        private val delegate: Either<T, *>
) {
    fun forEach(block: (T) -> Unit) {
        val e = delegate
        when (e) {
            is Either.Left -> block(e.value)
        }
    }

    fun exists(predicate: (T) -> Boolean): Boolean {
        val e = delegate
        return when (e) {
            is Either.Left -> predicate(e.value)
            is Either.Right -> false
        }
    }

    fun toList(): List<T> {
        val e = delegate
        return when (e) {
            is Either.Left -> listOf(e.value)
            is Either.Right -> listOf()
        }
    }

    companion object {
        fun <T : Any> delegate(projection: Projection<T>): Either<T, *> = projection.delegate
    }
}

inline fun <T : Any> Projection<T>.getOrElse(default: () -> T): T {
    val e = Projection.delegate(this)
    return when (e) {
        is Either.Left -> e.value
        is Either.Right -> default()
    }
}

class LeftProjection<out L : Any, out R : Any>(
        val either: Either<L, R>
) : Projection<L>(either) {

    fun get(): L {
        val e = either
        return when (e) {
            is Either.Left -> e.value
            is Either.Right -> throw NoSuchElementException("Either.left.value on Right")
        }
    }

    inline fun <T : Any> map(f: (L) -> T): Either<T, R> = flatMap { Either.left<T, R>(f(it)) }
}

class RightProjection<out L : Any, out R : Any>(
        val either: Either<L, R>
) : Projection<R>(either.swap()) {

    fun get(): R {
        val e = either
        return when (e) {
            is Either.Right -> e.value
            is Either.Left -> throw NoSuchElementException("Either.right.value on Left")
        }
    }

    inline fun <T : Any> map(transform: (R) -> T): Either<L, T> = flatMap { Either.right<L, T>(transform(it)) }
}

inline fun <L : Any, R : Any, T : Any> LeftProjection<L, R>.flatMap(transform: (L) -> Either<T, R>): Either<T, R> {
    val e = either
    return when (e) {
        is Either.Left -> transform(e.value)
        is Either.Right -> Either.right(e.value)
    }
}

inline fun <L : Any, R : Any, T : Any> RightProjection<L, R>.flatMap(transform: (R) -> Either<L, T>): Either<L, T> {
    val e = either
    return when (e) {
        is Either.Right -> transform(e.value)
        is Either.Left -> Either.left(e.value)
    }
}
