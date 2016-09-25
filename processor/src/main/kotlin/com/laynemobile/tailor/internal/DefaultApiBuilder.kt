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

package com.laynemobile.tailor.internal

import com.laynemobile.tailor.Api
import com.laynemobile.tailor.ApiBuilder
import com.laynemobile.tailor.Source
import com.laynemobile.tailor.Tailor

internal class DefaultApiBuilder<T : Any?, R : Any?>
internal constructor() : ApiBuilder<T, R> {
    private var _source: ((T) -> R)? = null
    private val source: ((T) -> R)
        get() {
            return _source!!
        }

    private var _tailor: (((T) -> R) -> Api<T, R>)? = null
    private val tailor: ((T) -> R) -> Api<T, R>
        get() {
            return _tailor!!
        }

    override fun source(init: Source<T, R>.() -> Unit) {
        _source = buildSource(init)
    }

    override fun tailor(init: Tailor<T, R>.() -> Unit) {
        _tailor = buildTailor(init)
    }

    override fun build(): Api<T, R> = source.to(tailor)
}
