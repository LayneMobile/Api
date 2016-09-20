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

import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.Single
import io.reactivex.SingleEmitter

private class SingleSourceProcessor<in T : Any, R : Any>
internal constructor(
        private val source: (T, SingleEmitter<R>) -> Unit
) : SingleProcessor<T, R> {
    override fun invoke(p1: T): Single<R> = Single.create<R> { emitter ->
        source(p1, emitter)
    }
}

private class ObservableSourceProcessor<in T : Any, R : Any>
internal constructor(
        private val source: (T, ObservableEmitter<R>) -> Unit
) : ObservableProcessor<T, R> {
    override fun invoke(p1: T): Observable<R> = Observable.create { emitter ->
        source(p1, emitter)
    }
}

fun <T : Any, R : Any> ProcessorBuilder<T, Single<R>>.withSingleSource(block: (T, SingleEmitter<R>) -> Unit) {
    withProcessor(SingleSourceProcessor(block))
}

fun <T : Any, R : Any> ProcessorBuilder<T, Single<R>>.withSingleSource(block: (T) -> R?) {
    withSingleSource { t, emitter ->
        try {
            emitter.onSuccess(block(t)!!)
        } catch (e: Throwable) {
            emitter.onError(e)
        }
    }
}

fun <T : Any, R : Any> ProcessorBuilder<T, Observable<R>>.source(block: (T, ObservableEmitter<R>) -> Unit) {
    withProcessor(ObservableSourceProcessor(block))
}

fun <T : Any, R : Any> ProcessorBuilder<T, Observable<R>>.source(block: (T) -> R?) {
    source { t, emitter ->
        try {
            emitter.onNext(block(t)!!)
            emitter.onComplete()
        } catch (e: Throwable) {
            emitter.onError(e)
        }
    }
}
