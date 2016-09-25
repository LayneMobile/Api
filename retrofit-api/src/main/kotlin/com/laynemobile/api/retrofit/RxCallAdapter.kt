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

package com.laynemobile.api.retrofit

import com.laynemobile.result.Result
import com.laynemobile.result.resultTry
import io.reactivex.Scheduler
import io.reactivex.Single
import retrofit2.Call
import retrofit2.CallAdapter
import retrofit2.Response
import java.lang.reflect.Type
import java.util.concurrent.Callable

fun Response<*>?.httpMessage(): String = if (this == null) {
    throw NullPointerException("response == null")
} else {
    "HTTP ${code()} ${message()}"
}

inline fun <T : Any, R : Any> Response<T>.fold(success: Response<T>.() -> R, error: Response<T>.() -> R): R {
    return if (isSuccessful) {
        success()
    } else {
        error()
    }
}

/** Exception for an unexpected, non-2xx HTTP response.  */
class HttpException(
        @Transient private val response: Response<*>
) : Exception(response.httpMessage()) {
    val httpCode: Int = response.code()
    val httpMessage: String = response.message()
}

fun <R : Any> Call<R>.executor(): LamdaCallable<Response<R>> = lambdaCallable {
    clone().execute()
}

fun <R : Any> (() -> Response<R>).toBody(): LamdaCallable<R> = lambdaCallable {
    invoke().fold({
        body()
    }, {
        throw HttpException(this)
    })
}

fun <R : Any> LamdaCallable<R>.toResult(): LamdaCallable<Result<R>> = lambdaCallable {
    resultTry(this@toResult)
}

fun <R : Any> LamdaCallable<R>.toSingle(): Single<R> {
    return Single.fromCallable<R>(this)
}

abstract class LamdaCallable<R : Any?> : () -> R, Callable<R> {
    final override fun call(): R = invoke()
}

inline fun <R : Any?> lambdaCallable(crossinline block: () -> R) = object : LamdaCallable<R>() {
    override fun invoke(): R = block()
}

internal class RxCallAdapter(
        private val responseType: Type,
        private val scheduler: Scheduler?,
        private val isResult: Boolean,
        private val isBody: Boolean
) : CallAdapter<Single<*>> {

    override fun responseType(): Type {
        return responseType
    }

    override fun <R : Any> adapt(call: Call<R>): Single<*> {
        val executor = call.executor()
        val single: Single<*> = when {
            isResult -> executor.toResult().toSingle()
            isBody -> executor.toBody().toSingle()
            else -> executor.toSingle()
        }
        return scheduler?.let { single.subscribeOn(it) } ?: single
    }
}

