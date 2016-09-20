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

package com.laynemobile.processor

import com.laynemobile.result.Result
import io.reactivex.Observable
import io.reactivex.Single

fun <T : Any> Observable<T>.mapToResult(): Observable<Result<T>> {
    return map { Result.success(it) }
            .onErrorReturn { Result.failure(it) }
}

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

inline fun <T : Any> singleTry(block: () -> T?): Single<T> = try {
    Single.just(block()!!)
} catch (e: Throwable) {
    Single.error(e)
}

inline fun <T : Any> observableTry(block: () -> T?): Observable<T> = try {
    Observable.just(block()!!)
} catch (e: Throwable) {
    Observable.error(e)
}
