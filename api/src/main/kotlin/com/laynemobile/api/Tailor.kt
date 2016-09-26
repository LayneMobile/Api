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

@file:JvmName("TailorUtil")

package com.laynemobile.api

import com.laynemobile.api.internal.TailorFunction
import java.util.*

interface Tailor<out T : Any, in R : Any> {
    fun alter(alteration: Alteration<T, R>)

    class Builder<T : Any, R : Any>
    internal constructor()
    : Tailor<T, R>, com.laynemobile.api.Builder<((T) -> Request<R>) -> Api<T, R>> {
        private val value: MutableSet<Alteration<T, R>> = HashSet()

        override fun alter(alteration: Alteration<T, R>) {
            value += alteration
        }

        override fun build(): ((T) -> Request<R>) -> Api<T, R> {
            return TailorFunction(value)
        }
    }

    companion object {
        fun <T : Any, R : Any> build(init: Tailor<T, R>.() -> Unit): ((T) -> Request<R>) -> Api<T, R> {
            val builder = Tailor.Builder<T, R>()
            builder.init()
            return builder.build()
        }

        fun <T : Any, R : Any> builder(): Tailor.Builder<T, R> = Tailor.Builder<T, R>()
    }
}

inline fun <T : Any, R : Any> Tailor<T, R>.alter(block: () -> Alteration<T, R>) {
    alter(block())
}

fun <T : Any, R : Any> Tailor<T, R>.validate(validator: Alteration.Validator<T>) {
    alter(validator)
}

fun <T : Any, R : Any> Tailor<T, R>.validate(validator: (T) -> Unit) = alter {
    object : Alteration.Validator<T>() {
        override fun invoke(param: T): Unit = validator(param)
    }
}

fun <T : Any, R : Any> Tailor<T, R>.modify(modifier: Alteration.Modifier<T, R>) {
    alter(modifier)
}

fun <T : Any, R : Any> Tailor<T, R>.modify(modifier: (T, Request<R>) -> Request<R>) = alter {
    object : Alteration.Modifier<T, R>() {
        override fun invoke(param: T, result: Request<R>): Request<R> = modifier(param, result)
    }
}

fun <T : Any, R : Any> Tailor<T, R>.intercept(interceptor: Alteration.Interceptor<T, R>) {
    alter(interceptor)
}

fun <T : Any, R : Any> Tailor<T, R>.intercept(interceptor: (Alteration.Interceptor.Chain<T, R>) -> Request<R>) = alter {
    object : Alteration.Interceptor<T, R>() {
        override fun invoke(chain: Chain<T, R>): Request<R> = interceptor(chain)
    }
}

