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

import com.laynemobile.result.Result
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.exceptions.CompositeException
import io.reactivex.functions.BiFunction

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

fun <T : Any> Single<T>.zipResults(other: Single<T>) = zip(this.mapToResult(), other.mapToResult())

fun <T : Any> zip(first: Single<Result<T>>, second: Single<Result<T>>): Single<T> {
    val result: Single<Result<T>> = Single.zip(first, second, mergeBiFunction())
    return result.flatMapResult()
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

fun <T : Any> merge(primary: Result<T>, secondary: Result<T>): Result<T> = when (primary) {
    is Result.Success -> {
        when (secondary) {
            is Result.Success -> primary
            is Result.Failure -> secondary
        }
    }
    is Result.Failure -> {
        when (secondary) {
            is Result.Success -> primary
            is Result.Failure -> Result.failure(CompositeException(primary.error, secondary.error))
        }
    }
}

fun <T : Any> mergeBiFunction(): BiFunction<Result<T>, Result<T>, Result<T>> {
    return BiFunction { t1, t2 -> merge(t1, t2) }
}

operator fun <T : Any> Result<T>.plus(other: Result<T>): Result<T> = merge(this, other)

