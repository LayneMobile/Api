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

abstract class ErrorHandlingProcessor<in T : Any?, out R : Any?> : Processor<T, R>, (T, Throwable) -> R {

    @Throws(Exception::class)
    protected abstract fun tryInvoke(p1: T): R

    final override fun invoke(p1: T): R = try {
        tryInvoke(p1)
    } catch (e: Throwable) {
        invoke(p1, e)
    }
}

fun <T : Any?, R : Any?> ((T) -> R).withErrorHandler(
        errorHandler: (T, Throwable) -> R
): ErrorHandlingProcessor<T, R> = object : ErrorHandlingProcessor<T, R>() {
    override fun tryInvoke(p1: T): R = this@withErrorHandler(p1)
    override fun invoke(p1: T, p2: Throwable): R = errorHandler.invoke(p1, p2)
}
