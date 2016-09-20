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

import rx.Notification
import java.util.*


internal class NotificationNode<T : Any?>
@JvmOverloads constructor(
        val prev: NotificationNode<T>? = null,
        val current: Notification<T>? = null
) {

    fun update(current: Notification<T>): NotificationNode<T> {
        return NotificationNode(this, current)
    }

    @JvmOverloads fun prevList(until: NotificationNode<T>? = null): List<Notification<T>> {
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
