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

import io.reactivex.Observable

interface ObservableApi<in T : Any, R : Any> : Api<T, Observable<R>>

fun <T : Any, R : Any> Api<T, R?>.toObservableApi() = object : ObservableApi<T, R> {
    override fun request(p1: T): Observable<R> = observableCreate {
        this@toObservableApi.request(p1)
    }
}

/*
fun stuff() {
    val api: Api<Int, String> = api {
        source { }
        alter { }
    }
}
*/
