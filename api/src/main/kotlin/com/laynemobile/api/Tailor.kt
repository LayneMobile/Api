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

interface Tailor<out T : Any?, in R : Any?> {
    fun alter(alteration: Alteration<T, R>)
}

fun <T : Any?, R : Any?> Tailor<T, R>.alter(vararg alterations: Alteration<T, R>) {
    for (alteration in alterations) {
        alter(alteration)
    }
}

inline fun <T : Any?, R : Any?> Tailor<T, R>.alter(block: () -> Alteration<T, R>) {
    alter(block())
}

fun <T : Any?, R : Any?> Tailor<T, R>.validate(validator: Alteration.Validator<T>) {
    alter(validator)
}

fun <T : Any?, R : Any?> Tailor<T, R>.validate(validator: (T) -> Unit) = alter {
    object : Alteration.Validator<T>() {
        override fun invoke(param: T): Unit = validator(param)
    }
}

fun <T : Any?, R : Any?> Tailor<T, R>.modify(modifier: Alteration.Modifier<T, R>) {
    alter(modifier)
}

fun <T : Any?, R : Any?> Tailor<T, R>.modify(modifier: (T, R) -> R) = alter {
    object : Alteration.Modifier<T, R>() {
        override fun invoke(param: T, result: R): R = modifier(param, result)
    }
}

fun <T : Any?, R : Any?> Tailor<T, R>.intercept(interceptor: Alteration.Interceptor<T, R>) {
    alter(interceptor)
}

fun <T : Any?, R : Any?> Tailor<T, R>.intercept(interceptor: (Alteration.Interceptor.Chain<T, R>) -> R) = alter {
    object : Alteration.Interceptor<T, R>() {
        override fun invoke(chain: Chain<T, R>): R = interceptor(chain)
    }
}

