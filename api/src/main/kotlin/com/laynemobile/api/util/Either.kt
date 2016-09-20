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

@file:JvmName("Eithers")

package com.laynemobile.api.util

import org.funktionale.either.Disjunction
import org.funktionale.either.EitherLike
import org.funktionale.either.LeftLike
import org.funktionale.either.RightLike

sealed class Either<out L : Any, out R : Any> : EitherLike {

    abstract val value: Any

    val left by lazy { LeftProjection(this) }
    val right by lazy { RightProjection(this) }

    abstract operator fun component1(): L?
    abstract operator fun component2(): R?

    inline fun <T : Any?> fold(left: (L) -> T, right: (R) -> T): T = when (this) {
        is Left -> left(value)
        is Right -> right(value)
    }

    fun toDisjunction(): Disjunction<L, R> = when (this) {
        is Right -> Disjunction.Right(value)
        is Left -> Disjunction.Left(value)
    }

    fun swap(): Either<R, L> = when (this) {
        is Left -> right(value)
        is Right -> left(value)
    }

    class Left<out L : Any>
    private constructor(
            override val value: L
    ) : Either<L, Nothing>(), LeftLike {
        override fun component1(): L = value
        override fun component2(): Nothing? = null

        override fun hashCode() = value.hashCode()

        override fun equals(other: Any?): Boolean = if (this === other) {
            true
        } else when (other) {
            is Left<*> -> value == other.value
            else -> false
        }

        override fun toString(): String = "Either.Left($value)"

        internal companion object {
            internal fun <L : Any, R : Any> create(value: L): Either<L, R> = Left(value)
        }
    }

    class Right<out R : Any>
    private constructor(
            override val value: R
    ) : Either<Nothing, R>(), RightLike {
        override fun component1(): Nothing? = null
        override fun component2(): R = value

        override fun hashCode() = value.hashCode()

        override fun equals(other: Any?): Boolean = if (this === other) {
            true
        } else when (other) {
            is Right<*> -> value == other.value
            else -> false
        }

        override fun toString(): String = "Either.Right($value)"

        internal companion object {
            internal fun <L : Any, R : Any> create(value: R): Either<L, R> = Right(value)
        }
    }

    companion object {
        @JvmStatic
        fun <L : Any, R : Any> left(value: L): Either<L, R> = Left.create(value)

        @JvmStatic
        fun <L : Any, R : Any> right(value: R): Either<L, R> = Right.create(value)
    }
}

open class OneOf<out L : Any, out R : Any>
protected constructor(
        protected val delegate: Either<L, R>
) {
    open fun component1(): L? = delegate.component1()
    open fun component2(): R? = delegate.component2()

    override fun hashCode() = delegate.hashCode()

    override fun equals(other: Any?) = if (this === other) {
        true
    } else when (other) {
        null -> false
        is Either<*, *> -> delegate.equals(other)
        is OneOf<*, *> -> delegate.equals(other.delegate)
        else -> false
    }

    override fun toString() = delegate.toString()
}

fun <T : Any> Either<T, T>.merge(): T = when (this) {
    is Either.Left -> value
    is Either.Right -> value
}

fun <L : Any, R : Any> Pair<L, R>.toLeft(): Either<L, R> = Either.left(component1())

fun <L : Any, R : Any> Pair<L, R>.toRight(): Either<L, R> = Either.right(component2())

fun <T : Any> eitherTry(body: () -> T?): Either<Exception, T> = try {
    Either.right(body()!!)
} catch(e: Exception) {
    Either.left(e)
}
