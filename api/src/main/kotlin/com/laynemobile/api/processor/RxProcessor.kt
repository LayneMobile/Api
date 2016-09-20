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

import com.laynemobile.api.util.Result
import com.laynemobile.api.util.resultTry
import io.reactivex.Observable
import io.reactivex.Single

interface SingleProcessor<in T : Any?, R : Any?> : ErrorHandlerProcessor<T, Single<R>> {
    override fun onError(throwable: Throwable): Single<R> = Single.error(throwable)
}

interface ObservableProcessor<in T : Any?, R : Any?> : ErrorHandlerProcessor<T, Observable<R>> {
    override fun onError(throwable: Throwable): Observable<R> = Observable.error(throwable)
}

internal abstract class AbstractObservingProcessor<in T : Any, R1 : Any, out R2 : Any>(
        private val processor: (T) -> R1?
) : ErrorHandlerProcessor<T, R2> {

    protected fun process(p1: T): Result<R1> = resultTry {
        processor(p1)
    }
}

internal class ForwardingSingleProcessor<in T : Any, R : Any>(
        processor: (T) -> R?
) : AbstractObservingProcessor<T, R, Single<R>>(processor), SingleProcessor<T, R> {

    override fun invoke(p1: T): Single<R> {
        return Single.defer { process(p1).toSingle() }
    }
}

internal class ForwardingObservableProcessor<in T : Any, R : Any>(
        processor: (T) -> R?
) : AbstractObservingProcessor<T, R, Observable<R>>(processor), ObservableProcessor<T, R> {

    override fun invoke(p1: T): Observable<R> {
        return Observable.defer { process(p1).toObservable() }
    }
}

fun <T : Any> Result<T>.toSingle(): Single<T> = fold(success = {
    Single.just(it)
}, failure = {
    Single.error(it)
})

fun <T : Any> Result<T>.toObservable(): Observable<T> = fold(success = {
    Observable.just(it)
}, failure = {
    Observable.error(it)
})

fun <T : Any, R : Any> ((T) -> R?).toSingleProcessor(): SingleProcessor<T, R> {
    return ForwardingSingleProcessor(this)
}

fun <T : Any, R : Any> ((T) -> R?).toObservableProcessor(): ObservableProcessor<T, R> {
    return ForwardingObservableProcessor(this)
}

fun <T : Any, R : Any> observableProcessor(block: (T) -> Observable<R>) = object : ObservableProcessor<T, R> {
    override fun invoke(p1: T): Observable<R> = block(p1)
}
