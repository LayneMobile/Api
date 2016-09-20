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

package com.laynemobile.api.internal.request;

import com.laynemobile.api.Aggregable;
import com.laynemobile.processor.Extension;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

import kotlin.jvm.functions.Function1;
import rx.Observable;
import rx.functions.Action1;

class AggregableProcessor<T, P> extends Extension.Interceptor<P, Observable<T>> {
    private final Map<Object, Aggregate<T>> aggregates = new HashMap<>(4);
    private final OnAggregateComplete onAggregateComplete = new OnAggregateComplete();
    private final Function1<P, Aggregable> source;

    private AggregableProcessor(Function1<P, Aggregable> source) {
        this.source = source;
    }

    static <T, P> AggregableProcessor<T, P> create(Function1<P, Aggregable> source) {
        return new AggregableProcessor<>(source);
    }

    @Override public Observable<T> invoke(@NotNull Chain<P, ? extends Observable<T>> chain) {
        final P p = chain.getValue();
        final Object aggregateKey;
        final Aggregable aggregable = source.invoke(p);
        if (aggregable == null || (aggregateKey = aggregable.getKey()) == null) {
            return chain.proceed(p);
        }

        Aggregate<T> aggregate;
        synchronized (aggregates) {
            aggregate = aggregates.get(aggregateKey);
        }
        if (aggregate == null || aggregate.isCompleted()) {
            Observable<T> request = chain.proceed(p);
            synchronized (aggregates) {
                aggregate = aggregates.get(aggregateKey);
                if (aggregate == null || aggregate.isCompleted()) {
                    aggregate = new Aggregate<T>(aggregable, request, onAggregateComplete);
                    aggregates.put(aggregateKey, aggregate);
                }
            }
        }
        return aggregate.request;
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

    private class OnAggregateComplete implements Action1<Aggregate<T>> {
        @Override public void call(Aggregate<T> progressAggregate) {
            synchronized (aggregates) {
                aggregates.remove(progressAggregate.aggregable.getKey());
            }
        }
    }
}
