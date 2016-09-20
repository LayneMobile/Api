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

package com.laynemobile.api.util


interface Logger {
    fun v(tag: String, msg: String): Unit {
    }

    fun v(tag: String, msg: String, tr: Throwable): Unit {
    }

    fun d(tag: String, msg: String): Unit {
    }

    fun d(tag: String, msg: String, tr: Throwable): Unit {
    }

    fun i(tag: String, msg: String): Unit {
    }

    fun i(tag: String, msg: String, tr: Throwable): Unit {
    }

    fun w(tag: String, msg: String): Unit {
    }

    fun w(tag: String, msg: String, tr: Throwable): Unit {
    }

    fun e(tag: String, msg: String): Unit {
    }

    fun e(tag: String, msg: String, tr: Throwable): Unit {
    }

    companion object {
        val NONE: Logger = object : Logger {}
    }
}
