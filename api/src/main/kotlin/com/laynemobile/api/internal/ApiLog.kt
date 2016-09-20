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

import com.laynemobile.api.util.Logger
import java.io.PrintWriter
import java.io.StringWriter
import java.net.UnknownHostException


object ApiLog {
    @Volatile private var sLogger = Logger.NONE

    fun setLogger(logger: Logger?) {
        sLogger = logger ?: Logger.NONE
    }

    fun v(tag: String, msg: String) {
        sLogger.v(tag, msg)
    }

    fun v(tag: String, format: String, vararg args: Any) {
        sLogger.v(tag, String.format(format, *args))
    }

    fun v(tag: String, msg: String, tr: Throwable) {
        sLogger.v(tag, msg, tr)
    }

    fun d(tag: String, msg: String) {
        sLogger.d(tag, msg)
    }

    fun d(tag: String, format: String, vararg args: Any) {
        sLogger.d(tag, String.format(format, *args))
    }

    fun d(tag: String, msg: String, tr: Throwable) {
        sLogger.d(tag, msg, tr)
    }

    fun i(tag: String, msg: String) {
        sLogger.i(tag, msg)
    }

    fun i(tag: String, format: String, vararg args: Any) {
        sLogger.i(tag, String.format(format, *args))
    }

    fun i(tag: String, msg: String, tr: Throwable) {
        sLogger.i(tag, msg, tr)
    }

    fun w(tag: String, msg: String) {
        sLogger.w(tag, msg)
    }

    fun w(tag: String, format: String, vararg args: Any) {
        sLogger.w(tag, String.format(format, *args))
    }

    fun w(tag: String, msg: String, tr: Throwable) {
        sLogger.w(tag, msg, tr)
    }

    fun e(tag: String, msg: String) {
        sLogger.e(tag, msg)
    }

    fun e(tag: String, format: String, vararg args: Any) {
        sLogger.e(tag, String.format(format, *args))
    }

    fun e(tag: String, msg: String, tr: Throwable) {
        sLogger.e(tag, msg, tr)
    }

    /**
     * Handy function to get a loggable stack trace from a Throwable

     * @param tr
     * *         An exception to log
     */
    fun getStackTraceString(tr: Throwable?): String {
        if (tr == null) {
            return ""
        }

        // This is to reduce the amount of log spew that apps do in the non-error
        // condition of the network being unavailable.
        var t = tr
        while (t != null) {
            if (t is UnknownHostException) {
                return ""
            }
            t = t.cause
        }

        val sw = StringWriter()
        val pw = PrintWriter(sw)
        tr.printStackTrace(pw)
        pw.flush()
        return sw.toString()
    }

    fun printAllStackTraces(tag: String) {
        for ((key, value) in Thread.getAllStackTraces()) {
            ApiLog.e(tag, "\nthread: %s", key.name)
            printStackTrace(tag, value)
        }
    }

    private fun printStackTrace(tag: String, stackTrace: Array<StackTraceElement>) {
        for (element in stackTrace) {
            ApiLog.e(tag, "'    %s'", element)
        }
    }
}

fun Throwable?.toStackTraceString(): String {
    return ApiLog.getStackTraceString(this)
}
