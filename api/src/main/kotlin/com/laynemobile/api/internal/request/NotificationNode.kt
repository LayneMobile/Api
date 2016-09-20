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

package com.laynemobile.api.internal.request

import io.reactivex.Notification
import java.util.*

interface Latest<out T : Any?> {
    fun get(): T
}

internal class NotificationNode<T : Any>
internal constructor(
        val prev: NotificationNode<T>? = null,
        val current: Notification<T>? = null
) {

    operator fun plus(current: Notification<T>): NotificationNode<T> {
        return NotificationNode(this, current)
    }

    internal fun prevList(until: NotificationNode<T>? = null): List<Notification<T>> {
        if (this == until) {
            return emptyList()
        }

        var prev: NotificationNode<T>? = this.prev
        var list: MutableList<Notification<T>>? = null
        while (prev != null
                && prev.current != null
                && (until == null || prev != until)) {
            if (list == null) {
                list = ArrayList<Notification<T>>()
            }
            list.add(prev.current as Notification<T>)
            prev = prev.prev
        }
        return list?.let {
            Collections.reverse(it)
            it
        } ?: emptyList()
    }

    override fun toString(): String {
        val string = StringBuilder("NotificationNode{").append("current=").append(current)
        var prev: NotificationNode<T>? = this.prev
        while (prev != null) {
            string.append(",\n\n   prev=NotificationNode{").append("current=").append(prev.current).append("}")
            prev = prev.prev
        }
        return string.append("}").toString()
    }
}

internal fun <T : Any> NotificationNode<T>?.current(): Notification<T>? {
    return this?.current
}

internal inline fun <T : Any, R : Any?> Notification<T>.fold(
        onNext: (T) -> R,
        onError: (Throwable) -> R,
        onComplete: () -> R
): R = when {
    isOnError -> onError(error)
    isOnComplete -> onComplete()
    else -> try {
        value!!.let { onNext(it) }
    } catch (e: Throwable) {
        onError(e)
    }
}

internal inline fun <T : Any> Notification<T>.foldFinished(
        onNext: (T) -> Unit,
        onError: (Throwable) -> Unit,
        onComplete: () -> Unit
): Boolean = this?.fold({
    onNext(it)
    false
}, {
    onError(it)
    true
}, {
    onComplete()
    true
})

internal inline fun <T : Any> Notification<T>.ifOnNext(block: (T) -> Unit) {
    return fold(block, {}, {})
}
