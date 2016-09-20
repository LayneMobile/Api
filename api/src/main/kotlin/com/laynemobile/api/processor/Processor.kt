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

package com.laynemobile.api.processor

import com.laynemobile.api.util.Result
import com.laynemobile.api.util.ifFailure
import com.laynemobile.api.util.resultTry
import com.laynemobile.api.util.toResult
import java.util.*

interface Processor<in T : Any?, out R : Any?> : (T) -> R

interface ErrorHandler<out R : Any?> {
    fun onError(throwable: Throwable): R
}

interface ErrorHandlerProcessor<in T : Any?, out R : Any?> : Processor<T, R>, ErrorHandler<R>

interface ResultProcessor<in T : Any?, out R : Any> : ErrorHandlerProcessor<T, Result<R>> {
    override fun onError(throwable: Throwable): Result<R> = throwable.toResult()
}

fun <T : Any?> ((Throwable) -> T).toErrorHandler() = object : ErrorHandler<T> {
    override fun onError(throwable: Throwable) = this@toErrorHandler(throwable)
}

inline fun <T : Any?, R : Any?> errorHandlerProcessor(
        crossinline processor: (T) -> R,
        errorHandler: ErrorHandler<R>
): ErrorHandlerProcessor<T, R> {
    return object : ErrorHandlerProcessor<T, R> {
        override fun invoke(p1: T) = processor(p1)

        override fun onError(throwable: Throwable) = errorHandler.onError(throwable)
    }
}

inline fun <T : Any?, R : Any?> errorHandlerProcessor(
        crossinline processor: (T) -> R,
        noinline errorHandler: (Throwable) -> R
): ErrorHandlerProcessor<T, R> {
    return errorHandlerProcessor(processor, errorHandler.toErrorHandler())
}

fun <T : Any?, R : Any?> ((T) -> R).toProcessor(): Processor<T, R> {
    return object : Processor<T, R> {
        override fun invoke(p1: T) = this@toProcessor(p1)
    }
}

fun <T : Any?, R : Any> ((T) -> R?).toResultProcessor(): ResultProcessor<T, R> {
    return object : ResultProcessor<T, R> {
        override fun invoke(p1: T): Result<R> = resultTry {
            this@toResultProcessor(p1)
        }
    }
}

sealed class Extension<in T : Any?, out R : Any?> {

    abstract class Checker<in T : Any?> : Extension<T, Nothing>() {
        @Throws(Exception::class)
        abstract fun check(t: T)

        fun <R : Any?> generify(): Extension<T, R> = this
    }

    abstract class Modifier<in T : Any?, R : Any?> : Extension<T, R>() {
        abstract fun modify(t: T, r: R): R
    }

    abstract class Interceptor<T : Any?, R : Any?> : Extension<T, R>() {
        abstract fun intercept(chain: Chain<T, R>): R

        interface Chain<T : Any?, out R : Any?> {
            val value: T

            fun proceed(t: T): R
        }
    }
}

fun <T : Any?, R : Any?> List<Extension<T, R>>.toExtensions(): Extensions<T, R> {
    val checkers = ArrayList<Extension.Checker<T>>()
    val modifiers = ArrayList<Extension.Modifier<T, R>>()
    val interceptors = ArrayList<Extension.Interceptor<T, R>>()
    for (extension in this) {
        when (extension) {
            is Extension.Checker -> checkers += extension
            is Extension.Modifier -> modifiers += extension
            is Extension.Interceptor -> interceptors += extension
        }
    }
    return Extensions(checkers, modifiers, interceptors)
}

data class Extensions<T : Any?, R : Any?>(
        val checkers: List<Extension.Checker<T>> = emptyList(),
        val modifiers: List<Extension.Modifier<T, R>> = emptyList(),
        val interceptors: List<Extension.Interceptor<T, R>> = emptyList()
)

open class ForwardingProcessor<in T : Any?, out R : Any?>(
        delegate: Processor<T, R>
) : Processor<T, R> by delegate

open class ForwardingErrorHandlerProcessor<in T : Any?, out R : Any?>(
        delegate: ErrorHandlerProcessor<T, R>
) : ErrorHandlerProcessor<T, R> by delegate

fun <T : Any?, R : Any?> ((T) -> R).withErrorHandler(errorHandler: ErrorHandler<R>): ErrorHandlerProcessor<T, R> {
    return errorHandlerProcessor(this, errorHandler)
}

class ResultChecker<in T : Any?>(
        private val handler: (T) -> Result<Unit>
) : Extension.Checker<T>() {

    override fun check(t: T) {
        handler(t).ifFailure { throw it }
    }
}

fun <T : Any?> resultChecker(handler: (T) -> Result<Unit>) = ResultChecker(handler)
