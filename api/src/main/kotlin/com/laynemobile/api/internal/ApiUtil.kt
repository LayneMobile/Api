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

package com.laynemobile.api.internal

import com.laynemobile.api.Alteration
import com.laynemobile.api.Api
import com.laynemobile.api.Request
import com.laynemobile.api.api

internal class TailorFunction<T : Any, R : Any>
private constructor(
        private val validators: List<Alteration.Validator<T>>,
        private val modifiers: List<Alteration.Modifier<T, R>>,
        private val interceptors: List<Alteration.Interceptor<T, R>>
) : ((T) -> Request<R>) -> Api<T, R> {
    internal constructor(alterations: Collection<Alteration<T, R>>) : this(
            alterations.validators(),
            alterations.modifiers(),
            alterations.interceptors()
    )

    override fun invoke(source: (T) -> Request<R>) = api { p1: T ->
        Request.defer { request(source, p1) }
    }

    private fun request(source: (T) -> Request<R>, p1: T): Request<R> = try {
        AlterationChain(source, validators, modifiers, interceptors)
                .proceed(p1)
    } catch (e: Throwable) {
        Request.error<R>(e)
    }
}

private class AlterationChain<T : Any, R : Any>
internal constructor(
        private val source: (T) -> Request<R>,
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

    override fun proceed(t: T): Request<R> {
        val interceptor = interceptors.getOrNull(index)
        return if (interceptor != null) {
            interceptor.invoke(next(t))
        } else {
            request(t)
        }
    }

    private fun request(t: T): Request<R> {
        // Validate with checkers
        for (validate in validators) {
            validate(t)
        }

        // Make actual call
        var result = source(t)

        // Allow modifications to original result
        for (modify in modifiers) {
            result = modify(t, result)
        }

        // return potentially modified  result
        return result
    }

    private fun next(t: T) = AlterationChain(this, t)
}
