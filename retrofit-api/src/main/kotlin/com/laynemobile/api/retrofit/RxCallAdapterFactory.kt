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
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.Single
import retrofit2.CallAdapter
import retrofit2.Response
import retrofit2.Retrofit
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

/**
 * A [call adapter][CallAdapter.Factory] which uses RxJava for creating observables.
 *
 *
 * Adding this class to [Retrofit] allows you to return an [Observable], [Single],
 * or [Completable] from service methods.
 * `
 * interface MyService {
 * &#64;GET("user/me")
 * Observable&lt;User&gt; getUser()
 * }
` *
 * There are three configurations supported for the `Observable` or `Single` type
 * parameter:
 *
 *  * Direct body (e.g., `Observable&lt;User&gt;`) calls `onNext` with the deserialized body
 * for 2XX responses and calls `onError` with [HttpException] for non-2XX responses and
 * [IOException] for network errors.
 *  * Response wrapped body (e.g., `Observable&lt;Response&lt;User&gt;&gt;`) calls `onNext`
 * with a [Response] object for all HTTP responses and calls `onError` with
 * [IOException] for network errors
 *  * Result wrapped body (e.g., `Observable&lt;Result&lt;User&gt;&gt;`) calls `onNext` with a
 * [Result] object for all HTTP responses and errors.
 *
 *
 *
 * *Note:* Support for [Single] and [Completable] is experimental and subject
 * to backwards-incompatible changes at any time since both of these types are not considered
 * stable by RxJava.
 */
class RxCallAdapterFactory
private constructor(
        private val scheduler: Scheduler? = null
) : CallAdapter.Factory() {


    override fun get(
            returnType: Type,
            annotations: Array<Annotation>,
            retrofit: Retrofit
    ): CallAdapter<*>? {
        val rawType = getRawType(returnType)
        if (rawType != Single::class.java) {
            return null
        }

        var isResult = false
        var isBody = false
        val responseType: Type
        if (returnType !is ParameterizedType) {
            val name = "Single"
            throw IllegalStateException("$name return type must be parameterized as $name<Foo> or $name<? extends Foo>")
        }

        val observableType = getParameterUpperBound(0, returnType)
        val rawObservableType = getRawType(observableType)
        if (rawObservableType == Response::class.java) {
            if (observableType !is ParameterizedType) {
                throw IllegalStateException("Response must be parameterized" + " as Response<Foo> or Response<? extends Foo>")
            }
            responseType = getParameterUpperBound(0, observableType)
        } else if (rawObservableType == Result::class.java) {
            if (observableType !is ParameterizedType) {
                throw IllegalStateException("Result must be parameterized" + " as Result<Foo> or Result<? extends Foo>")
            }
            responseType = getParameterUpperBound(0, observableType)
            isResult = true
        } else {
            responseType = observableType
            isBody = true
        }

        return RxCallAdapter(responseType, scheduler, isResult, isBody)
    }

    companion object {
        /**
         * Returns an instance which creates synchronous observables that do not operate on any scheduler
         * by default.
         */
        @JvmStatic
        fun create(): RxCallAdapterFactory {
            return RxCallAdapterFactory()
        }

        /**
         * Returns an instance which creates synchronous observables that
         * [subscribe on][Observable.subscribeOn] `scheduler` by default.
         */
        @JvmStatic
        fun createWithScheduler(scheduler: Scheduler): RxCallAdapterFactory {
            return RxCallAdapterFactory(scheduler)
        }
    }
}
