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

package com.laynemobile.processor

import com.laynemobile.util.copy

class ExtensionProcessor<in T : Any, out R : Any>(
        private val processor: (T) -> R,
        private val extensions: Extensions<T, R>
) : Processor<T, R> {

    @Throws(Exception::class)
    override fun invoke(p1: T): R {
        return ImmutableChain(processor, extensions)
                .proceed(p1)
    }
}

@Throws(Exception::class)
fun <T : Any, R : Any> ((T) -> R).process(
        p1: T,
        extensions: Extensions<T, R>
): R {
    return ImmutableChain(this, extensions)
            .proceed(p1)
}

inline fun <T : Any, R : Any> ((T) -> R).tryProcess(
        p1: T,
        extensions: Extensions<T, R>,
        onError: (T, Throwable) -> R
): R = try {
    process(p1, extensions)
} catch (e: Throwable) {
    onError(p1, e)
}

fun <T : Any, R : Any> ErrorHandlingProcessor<T, R>.tryProcess(
        p1: T,
        extensions: Extensions<T, R>
): R {
    return tryProcess(p1, extensions, this)
}


private class ImmutableChain<T : Any, R : Any>
private constructor(
        private val processor: (T) -> R,
        private val validators: List<Extension.Validator<T>> = emptyList(),
        private val modifiers: List<Extension.Modifier<T, R>> = emptyList(),
        private val interceptors: List<Extension.Interceptor<T, R>> = emptyList(),
        private val index: Int = 0,
        private val _value: T? = null
) : Extension.Interceptor.Chain<T, R> {

    override val value: T
        get() = _value ?: throw IllegalStateException("value cannot be null")

    internal constructor(processor: (T) -> R,
                         extensions: Extensions<T, R>) : this(
            processor = processor,
            validators = extensions.validators.copy(),
            modifiers = extensions.modifiers.copy(),
            interceptors = extensions.interceptors.copy()
    )

    private constructor(prev: ImmutableChain<T, R>, t: T) : this(
            processor = prev.processor,
            validators = prev.validators,
            modifiers = prev.modifiers,
            interceptors = prev.interceptors,
            index = prev.index + 1,
            _value = t
    )

    @Throws(Exception::class)
    override fun proceed(t: T): R {
        val interceptor = interceptors.getOrNull(index)
        return if (interceptor != null) {
            interceptor.invoke(next(t))
        } else {
            process(t)
        }
    }

    private fun process(t: T): R {
        val p = processor
        // Validate with checkers
        for (validator in validators) {
            validator.invoke(t)
        }

        // Make actual call
        var result = p.invoke(t)

        // Allow modifications to original result
        for (modifier in modifiers) {
            result = modifier.invoke(t, result)
        }

        // return potentially modified  result
        return result
    }

    private fun next(t: T) = ImmutableChain(this, t)
}
