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

import com.laynemobile.api.Alteration
import com.laynemobile.api.extensions.Aggregable
import com.laynemobile.request.Request
import java.util.*

private fun <T : Any> Aggregate<T>.validOrNull(): Aggregate<T>? = let { a ->
    if (a.isCompleted()) {
        null
    } else {
        a
    }
}

private inline fun <T : Any> Aggregate<T>?.ifNotValid(block: () -> Aggregate<T>): Aggregate<T> {
    return this?.validOrNull() ?: block()
}

internal class AggregableProcessor<T : Any, R : Any>
internal constructor(
        private val source: (T) -> Aggregable?
) : Alteration.Interceptor<T, R>() {
    private val aggregates = HashMap<Any, Aggregate<R>>(4)
    private val onAggregateComplete = fun(aggregable: Aggregable) {
        synchronized(aggregates) {
            aggregates.remove(aggregable.key)
        }
    }

    override fun invoke(chain: Alteration.Interceptor.Chain<T, R>): Request<R> {
        val p = chain.value
        val aggregable = source.invoke(p) ?: return chain.proceed(p)
        return getOrCreate(key = aggregable.key, request = {
            chain.proceed(p)
        }, aggregate = { request ->
            Aggregate(aggregable, request, onAggregateComplete)
        }).request
    }

    private fun get(key: Any): Aggregate<R>? = synchronized(aggregates) {
        aggregates[key]
    }

    private inline fun getOrCreate(key: Any, block: () -> Aggregate<R>): Aggregate<R> = synchronized(aggregates) {
        aggregates[key].ifNotValid {
            val aggregate = block()
            aggregates.put(key, aggregate)
            aggregate
        }
    }

    private inline fun getOrCreate(
            key: Any,
            request: () -> Request<R>,
            aggregate: (Request<R>) -> Aggregate<R>
    ): Aggregate<R> {
        return get(key).ifNotValid {
            val r = request()
            getOrCreate(key) { aggregate(r) }
        }
    }

    // TODO: expose peek*() in RequestProcessor
    //    @Override public Observable<T> peekSourceRequest(P p) {
    //        final Object aggregateKey;
    //        final Aggregable aggregable = source.getAggregable(p);
    //        if (aggregable != null && (aggregateKey = aggregable.key()) != null) {
    //            final Aggregate<T> aggregate;
    //            synchronized (aggregates) {
    //                aggregate = aggregates.get(aggregateKey);
    //            }
    //            if (aggregate != null && !aggregate.isCompleted() && !aggregate.isUnsubscribed()) {
    //                return aggregate.request;
    //            }
    //        }
    //        return null;
    //    }


//    companion object {
//
//        fun <T, P> create(source: Function1<P, Aggregable>): AggregableProcessor<T, P> {
//            return AggregableProcessor<Any, P>(source)
//        }
//    }
}

