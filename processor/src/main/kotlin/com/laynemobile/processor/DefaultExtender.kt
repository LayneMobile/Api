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

package com.laynemobile.processor

import java.util.*

internal class DefaultExtender<T : Any, R : Any?>
internal constructor() : Extender<T, R> {
    private val extensions: MutableList<Extension<T, R>> = ArrayList()

    override fun extend(extension: Extension<T, R>) {
        extensions += extension
    }

    override fun build(): Extensions<T, R> = extensions.toExtensions()
}

fun <T : Any, R : Any> ((T) -> R).extend(init: Extender<T, R>.() -> Unit): Processor<T, R> {
    val extender = DefaultExtender<T, R>()
    extender.init()
    return ExtensionProcessor(this, extender.build())
}

fun <T : Any, R : Any> processor(source: (T) -> R, init: Extender<T, R>.() -> Unit): Processor<T, R> {
    return source.extend(init)
}
