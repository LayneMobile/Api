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

import com.laynemobile.tailor.*
import io.reactivex.Observable

//@JvmName("apiBuilderFromCallable")
//fun <T : Any, R : Any> apiBuilder(source: (T) -> R): ApiBuilder<T, R> {
//    return ApiBuilder(source.toObservableProcessor())
//}
//
//fun <T : Any, R : Any> apiBuilder(source: (T) -> Observable<R>): ApiBuilder<T, R> {
//    return ApiBuilder(source.toProcessor())
//}
//
//@JvmName("apiFromCallable")
//fun <T : Any, R : Any> api(
//        source: ((T) -> R),
//        initializer: (Tailor<T, Observable<R>>.() -> Unit)
//): Api<T, R> {
//    return apiBuilder(source)
//            .build(initializer)
//}
//
//fun <T : Any, R : Any> api(
//        source: ((T) -> Observable<R>),
//        initializer: (Tailor<T, Observable<R>>.() -> Unit)
//): Api<T, R> {
//    return apiBuilder(source)
//            .build(initializer)
//}
