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

import com.laynemobile.api.observableTry
import com.laynemobile.processor.ErrorHandlingProcessor
import io.reactivex.Observable

private fun <T : Any?, R : Any> ((T, Throwable) -> Observable<R>)?.orDefault() = this ?: { t, throwable ->
    Observable.error(throwable)
}

fun <T : Any?, R : Any> observableProcessor(
        processor: (T) -> Observable<R>,
        errorHandler: ((T, Throwable) -> Observable<R>)? = null
) = object : ErrorHandlingProcessor<T, Observable<R>>() {

    private val errorHandler: (T, Throwable) -> Observable<R> = errorHandler.orDefault()

    override fun tryInvoke(p1: T): Observable<R> = processor(p1)

    override fun invoke(p1: T, p2: Throwable): Observable<R> = errorHandler(p1, p2)
}

fun <T : Any?, R : Any> ((T) -> R?).toObservableProcessor() = observableProcessor({ t: T ->
    observableTry { this@toObservableProcessor(t) }
})

//fun <T : Any, R : Any> ((T, ObservableEmitter<R>) -> Unit).extend(
//        extender: Extender<T, Observable<R>>
//): ExtensionProcessor<T, Observable<R>> {
//
//    fun <T : Any, R : Any> ((T) -> R).extend(init: Extender<T, R>.() -> Unit): ExtensionProcessor<T, R> {
//        val extender = DefaultExtender<T, R>()
//        extender.init()
//        return ExtensionProcessor(this, extender.build())
//    }
//
//}
//
//fun <T : Any, R : Any> ProcessorBuilder<T, Observable<R>>.source(block: (T) -> R?) {
//    source { t, emitter ->
//        try {
//            emitter.onNext(block(t)!!)
//            emitter.onComplete()
//        } catch (e: Throwable) {
//            emitter.onError(e)
//        }
//    }
//}

