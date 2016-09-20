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

package com.laynemobile.api.sources;

import com.laynemobile.api.Params
import com.laynemobile.api.Source
import com.laynemobile.api.processor.Extension
import rx.Observable

interface PreparableSource<T, P : Params> : Source<T, P> {
    /**
     * Prepare the observable after it's created.

     * @param sourceRequest
     * *         the source request observable
     * *
     * @param p
     * *         the parameters
     * *
     * *
     * @return the prepared source request observable
     */
    fun prepareSourceRequest(sourceRequest: Observable<T>, p: P): Observable<T>
}

private class PreparableSourceModifier<T, P : Params>
internal constructor(
        private val source: PreparableSource<T, P>
) : Extension.Modifier<P, Observable<T>>() {

    override fun modify(t: P, r: Observable<T>): Observable<T> {
        return source.prepareSourceRequest(r, t)
    }
//
//    companion object {
//
//        fun <T, P : Params> from(source: PreparableSource<T, P>): Extension<P, Observable<T>> {
//            return PreparableSource.Modifier(source)
//        }
//    }
}
