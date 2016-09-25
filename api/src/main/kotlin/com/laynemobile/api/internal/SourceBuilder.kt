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

package com.laynemobile.api.internal

import com.laynemobile.api.Source

internal fun <T : Any?, R : Any?> buildSource(init: Source<T, R>.() -> Unit): (T) -> R {
    val source = DefaultSource<T, R>()
    source.init()
    return source.get()
}

private class DefaultSource<T : Any?, R : Any?>
internal constructor() : Source<T, R> {
    private var _source: ((T) -> R)? = null

    override fun source(source: (T) -> R) {
        _source = source
    }

    internal fun get(): (T) -> R {
        return _source!!
    }
}
