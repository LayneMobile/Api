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

interface Extender<T : Any, R : Any?> : Builder<Extensions<T, R>> {
    fun extend(extension: Extension<T, R>)

    fun extend(vararg extensions: Extension<T, R>) {
        for (extension in extensions) {
            extend(extension)
        }
    }

    fun extend(block: () -> Extension<T, R>) {
        extend(block())
    }

    fun validate(validator: Extension.Validator<T>) {
        extend(validator)
    }

    fun validate(validator: (T) -> Unit) = extend {
        object : Extension.Validator<T>() {
            override fun invoke(param: T): Unit = validator(param)
        }
    }

    fun modify(modifier: Extension.Modifier<T, R>) {
        extend(modifier)
    }

    fun modify(modifier: (T, R) -> R) = extend {
        object : Extension.Modifier<T, R>() {
            override fun invoke(param: T, result: R): R = modifier(param, result)
        }
    }

    fun intercept(interceptor: Extension.Interceptor<T, R>) {
        extend(interceptor)
    }

    fun intercept(interceptor: (Extension.Interceptor.Chain<T, R>) -> R) = extend {
        object : Extension.Interceptor<T, R>() {
            override fun invoke(chain: Chain<T, R>): R = interceptor(chain)
        }
    }
}
