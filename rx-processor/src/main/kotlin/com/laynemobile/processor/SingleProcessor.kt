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

import io.reactivex.Single

private fun <T : Any?, R : Any> ((T, Throwable) -> Single<R>)?.orDefault() = this ?: { t, throwable ->
    Single.error(throwable)
}

fun <T : Any?, R : Any> singleProcessor(
        processor: (T) -> Single<R>,
        errorHandler: ((T, Throwable) -> Single<R>)? = null
) = object : ErrorHandlingProcessor<T, Single<R>>() {

    private val errorHandler: (T, Throwable) -> Single<R> = errorHandler.orDefault()

    override fun tryInvoke(p1: T): Single<R> = processor(p1)

    override fun invoke(p1: T, p2: Throwable): Single<R> = errorHandler(p1, p2)
}

fun <T : Any?, R : Any> ((T) -> R?).toSingleProcessor() = singleProcessor({ t: T ->
    singleTry { this@toSingleProcessor(t) }
})
