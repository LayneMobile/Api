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

import java.util.*

sealed class Extension<in T : Any?, out R : Any?> {

    abstract class Validator<in T : Any?> :
            Extension<T, Nothing>(),
            (T) -> Unit {

        @Throws(Exception::class)
        abstract override fun invoke(param: T)

        fun <R : Any?> generify(): Extension<T, R> = this
    }

    abstract class Modifier<in T : Any?, R : Any?> :
            Extension<T, R>(),
            (T, R) -> R

    abstract class Interceptor<T : Any?, R : Any?> :
            Extension<T, R>(),
            (Interceptor.Chain<T, R>) -> R {

        interface Chain<T : Any?, out R : Any?> {
            val value: T

            fun proceed(t: T): R
        }
    }
}

internal fun <T : Any?, R : Any?> List<Extension<T, R>>?.toExtensions(): Extensions<T, R> = if (this == null) {
    Extensions()
} else {
    val validators = ArrayList<Extension.Validator<T>>()
    val modifiers = ArrayList<Extension.Modifier<T, R>>()
    val interceptors = ArrayList<Extension.Interceptor<T, R>>()
    for (extension in this) {
        when (extension) {
            is Extension.Validator -> validators += extension
            is Extension.Modifier -> modifiers += extension
            is Extension.Interceptor -> interceptors += extension
        }
    }
    Extensions(validators, modifiers, interceptors)
}

data class Extensions<T : Any?, R : Any?>
internal constructor(
        val validators: List<Extension.Validator<T>> = emptyList(),
        val modifiers: List<Extension.Modifier<T, R>> = emptyList(),
        val interceptors: List<Extension.Interceptor<T, R>> = emptyList()
)
