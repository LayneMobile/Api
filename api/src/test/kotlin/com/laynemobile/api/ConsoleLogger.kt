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

import com.laynemobile.api.internal.ApiLog
import com.laynemobile.api.util.Logger

class ConsoleLogger : Logger {
    override fun v(tag: String, msg: String) {
        println(tag + ": " + msg)
    }

    override fun v(tag: String, msg: String, tr: Throwable) {
        println(tag + ": " + msg + ", " + ApiLog.getStackTraceString(tr))
    }

    override fun d(tag: String, msg: String) {
        v(tag, msg)
    }

    override fun d(tag: String, msg: String, tr: Throwable) {
        v(tag, msg, tr)
    }

    override fun i(tag: String, msg: String) {
        v(tag, msg)
    }

    override fun i(tag: String, msg: String, tr: Throwable) {
        v(tag, msg, tr)
    }

    override fun w(tag: String, msg: String) {
        v(tag, msg)
    }

    override fun w(tag: String, msg: String, tr: Throwable) {
        v(tag, msg, tr)
    }

    override fun e(tag: String, msg: String) {
        v(tag, msg)
    }

    override fun e(tag: String, msg: String, tr: Throwable) {
        v(tag, msg, tr)
    }
}

