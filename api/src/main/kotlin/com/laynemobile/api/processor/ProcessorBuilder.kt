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

import com.laynemobile.api.Builder
import com.laynemobile.api.util.copy
import java.util.*

open class ProcessorBuilder<T : Any, R : Any?>
internal constructor() : Builder<Processor<T, R>> {

    private var _processor: ((T) -> R)? = null
    private var _errorHandlerProcessor: ErrorHandlerProcessor<T, R>? = null
    private val _extensions: MutableList<Extension<T, R>> = ArrayList()

    protected val processor: ErrorHandlerProcessor<T, R>
        get() = _errorHandlerProcessor!!
    protected val extensions: List<Extension<T, R>>
        get() = _extensions.copy()

    open fun withProcessor(function: (T) -> R) {
        _processor = function
    }

    fun withProcessor(processor: ErrorHandlerProcessor<T, R>) {
        _errorHandlerProcessor = processor
    }

    fun withErrorHandler(errorHandler: ErrorHandler<R>) {
        val p = _errorHandlerProcessor ?: _processor
        _errorHandlerProcessor = p?.let { it.withErrorHandler(errorHandler) }
    }

    fun withErrorHandler(errorHandler: (Throwable) -> R) {
        withErrorHandler(errorHandler.toErrorHandler())
    }

    fun extend(extension: Extension<T, R>) {
        _extensions += extension
    }

    inline fun extend(block: () -> Extension<T, R>) {
        extend(block())
    }

    fun extend(vararg extensions: Extension<T, R>) {
        _extensions += extensions
    }

    fun check(checker: Extension.Checker<T>) {
        extend(checker)
    }

    fun check(checker: (T) -> Unit) = extend {
        object : Extension.Checker<T>() {
            override fun check(t: T): Unit = checker(t)
        }
    }

    fun modify(modifier: Extension.Modifier<T, R>) {
        extend(modifier)
    }

    fun modify(modifier: (T, R) -> R) = extend {
        object : Extension.Modifier<T, R>() {
            override fun modify(t: T, r: R): R = modifier(t, r)
        }
    }

    fun intercept(interceptor: Extension.Interceptor<T, R>) {
        extend(interceptor)
    }

    fun intercept(interceptor: (Extension.Interceptor.Chain<T, R>) -> R) = extend {
        object : Extension.Interceptor<T, R>() {
            override fun intercept(chain: Chain<T, R>): R = interceptor(chain)
        }
    }

    override fun build(): Processor<T, R> = InterceptProcessor(
            processor = processor,
            extensions = extensions
    )
}

fun <T : Any, R : Any?> buildProcessor(init: ProcessorBuilder<T, R>.() -> Unit): Processor<T, R> {
    val builder: ProcessorBuilder<T, R> = ProcessorBuilder()
    builder.init()
    return builder.build()
}
