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

package com.laynemobile.tailor.internal

import com.laynemobile.tailor.Alteration
import com.laynemobile.tailor.Api
import com.laynemobile.tailor.Tailor
import com.laynemobile.tailor.api
import java.util.*

internal fun <T : Any?, R : Any?> buildTailor(init: Tailor<T, R>.() -> Unit): ((T) -> R) -> Api<T, R> {
    val tailor = DefaultTailor<T, R>()
    tailor.init()
    return TailorFunction(tailor.get())
}

private class DefaultTailor<T : Any?, R : Any?>
internal constructor() : Tailor<T, R> {
    internal val value: MutableSet<Alteration<T, R>> = HashSet()

    override fun alter(alteration: Alteration<T, R>) {
        value += alteration
    }

    fun get(): Collection<Alteration<T, R>> = value
}

private class TailorFunction<T : Any?, R : Any?>
private constructor(
        private val validators: List<Alteration.Validator<T>>,
        private val modifiers: List<Alteration.Modifier<T, R>>,
        private val interceptors: List<Alteration.Interceptor<T, R>>
) : ((T) -> R) -> Api<T, R> {
    internal constructor(alterations: Collection<Alteration<T, R>>) : this(
            alterations.validators(),
            alterations.modifiers(),
            alterations.interceptors()
    )

    override fun invoke(source: (T) -> R) = api { p1: T ->
        AlterationChain(source, validators, modifiers, interceptors)
                .proceed(p1)
    }
}

private class AlterationChain<T : Any?, R : Any?>
internal constructor(
        private val source: (T) -> R,
        private val validators: List<Alteration.Validator<T>>,
        private val modifiers: List<Alteration.Modifier<T, R>>,
        private val interceptors: List<Alteration.Interceptor<T, R>>,
        private val index: Int = 0,
        private val _value: T? = null
) : Alteration.Interceptor.Chain<T, R> {

    override val value: T
        get() = _value ?: throw IllegalStateException("value cannot be null")

    private constructor(prev: AlterationChain<T, R>, t: T) : this(
            source = prev.source,
            validators = prev.validators,
            modifiers = prev.modifiers,
            interceptors = prev.interceptors,
            index = prev.index + 1,
            _value = t
    )

    override fun proceed(t: T): R {
        val interceptor = interceptors.getOrNull(index)
        return if (interceptor != null) {
            interceptor.invoke(next(t))
        } else {
            request(t)
        }
    }

    private fun request(t: T): R {
        val source = this.source
        // Validate with checkers
        for (validator in validators) {
            validator.invoke(t)
        }

        // Make actual call
        var result = source.invoke(t)

        // Allow modifications to original result
        for (modifier in modifiers) {
            result = modifier.invoke(t, result)
        }

        // return potentially modified  result
        return result
    }

    private fun next(t: T) = AlterationChain(this, t)
}
