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

open class InterceptProcessor<in T : Any, out R : Any?>
internal constructor(
        val processor: ErrorHandlerProcessor<T, R>,
        val extensions: List<Extension<T, R>> = emptyList()
) : Processor<T, R> {
    override fun invoke(p1: T): R = ImmutableChain(this).proceed(p1)
}

private class ImmutableChain<T : Any, R : Any?>
private constructor(
        private val processor: ImmutableProcessor<T, R>,
        private val interceptors: List<Extension.Interceptor<T, R>> = processor.extensions.interceptors,
        private val index: Int = 0,
        private val _value: T? = null
) : Extension.Interceptor.Chain<T, R> {

    override val value: T
        get() = _value ?: throw IllegalStateException("value cannot be null")

    internal constructor(parent: InterceptProcessor<T, R>) : this(
            processor = ImmutableProcessor(parent)
    )

    private constructor(prev: ImmutableChain<T, R>, t: T) : this(
            processor = prev.processor,
            interceptors = prev.interceptors,
            index = prev.index + 1,
            _value = t
    )

    override fun proceed(t: T): R = try {
        interceptors.getOrNull(index)?.intercept(next(t))
                ?: processor.invoke(t)
    } catch (e: Throwable) {
        processor.onError(e)
    }

    private fun next(t: T) = ImmutableChain(this, t)
}

private class ImmutableProcessor<T : Any, R : Any?>
internal constructor(
        parent: InterceptProcessor<T, R>
) : ErrorHandlerProcessor<T, R> {

    private val processor: ErrorHandlerProcessor<T, R> = parent.processor

    internal val extensions: Extensions<T, R> = parent.extensions.toExtensions()

    override fun invoke(t: T): R = tryInvoke(processor) { p ->
        // Validate with checkers
        for (checker in extensions.checkers) {
            checker.check(t)
        }

        // Make actual call
        var result = p.invoke(t)

        // Allow modifications to original result
        for (modifier in extensions.modifiers) {
            result = modifier.modify(t, result)
        }

        // return potentially modified  result
        result
    }

    override fun onError(throwable: Throwable): R = processor.onError(throwable)

    private inline fun tryInvoke(p: ErrorHandlerProcessor<T, R>, block: (Processor<T, R>) -> R): R = try {
        block(p)
    } catch (e: Throwable) {
        p.onError(e)
    }
}
