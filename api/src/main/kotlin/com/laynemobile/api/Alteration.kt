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

package com.laynemobile.api

sealed class Alteration<in T : Any?, out R : Any?> {

    abstract class Validator<in T : Any?> :
            Alteration<T, Nothing>(),
            (T) -> Unit {

        @Throws(Exception::class)
        abstract override fun invoke(param: T)

        fun <R : Any?> generify(): Alteration<T, R> = this
    }

    abstract class Modifier<in T : Any?, R : Any?> :
            Alteration<T, R>(),
            (T, R) -> R

    abstract class Interceptor<T : Any?, R : Any?> :
            Alteration<T, R>(),
            (Interceptor.Chain<T, R>) -> R {

        interface Chain<T : Any?, out R : Any?> {
            val value: T

            fun proceed(t: T): R
        }
    }
}
