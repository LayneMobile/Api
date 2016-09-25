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

@file:JvmName("RxUtil")

package com.laynemobile.tailor

import com.laynemobile.result.Result
import io.reactivex.Observable
import io.reactivex.Single


fun <T : Any> Single<T>.mapToResult(): Single<Result<T>> {
    return map { Result.success(it) }
            .onErrorReturn { Result.failure(it) }
}

fun <T : Any> Single<Result<T>>.flatMapResult(): Single<T> = flatMap { result ->
    result.fold(success = {
        Single.just(it)
    }, failure = {
        Single.error(it)
    })
}

inline fun <T : Any> singleCreate(crossinline source: () -> T?): Single<T> = Single.create { emitter ->
    try {
        val value: T = source()!!
        emitter.onSuccess(value)
    } catch (e: Throwable) {
        emitter.onError(e)
    }
}

fun <T : Any> (() -> T?).toSingle(): Single<T> = singleCreate(this)

fun <T : Any> Observable<T>.mapToResult(): Observable<Result<T>> {
    return map { Result.success(it) }
            .onErrorReturn { Result.failure(it) }
}

fun <T : Any> Observable<Result<T>>.flatMapResult(): Observable<T> = flatMap { result ->
    result.fold(success = {
        Observable.just(it)
    }, failure = {
        Observable.error(it)
    })
}

inline fun <T : Any> observableCreate(crossinline source: () -> T?): Observable<T> = Observable.create { emitter ->
    try {
        val value: T = source()!!
        emitter.onNext(value)
        emitter.onComplete()
    } catch (e: Throwable) {
        emitter.onError(e)
    }
}

fun <T : Any> (() -> T?).toObservable(): Observable<T> = observableCreate(this)
