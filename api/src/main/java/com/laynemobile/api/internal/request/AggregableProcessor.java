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

import com.laynemobile.api.Params;
import com.laynemobile.api.sources.AggregableSource;

import java.util.HashMap;
import java.util.Map;

import com.laynemobile.api.Aggregable;

import rx.Observable;
import rx.functions.Action1;

class AggregableProcessor<T, P extends Params> extends DefaultRequestProcessor<T, P> {
    private final Map<Object, Aggregate<T>> aggregates = new HashMap<>(4);
    private final OnAggregateComplete onAggregateComplete = new OnAggregateComplete();
    private final AggregableSource<T, P> source;

    AggregableProcessor(AggregableSource<T, P> source) {
        super(source);
        this.source = source;
    }

    static <T, P extends Params> AggregableProcessor<T, P> create(AggregableSource<T, P> source) {
        return new AggregableProcessor<>(source);
    }

    @Override public Observable<T> getRequestObservable(P p) {
        final Object aggregateKey;
        final Aggregable aggregable = source.getAggregable(p);
        if (aggregable == null || (aggregateKey = aggregable.key()) == null) {
            return super.getRequestObservable(p);
        }

        Aggregate<T> aggregate;
        synchronized (aggregates) {
            aggregate = aggregates.get(aggregateKey);
        }
        if (aggregate == null || aggregate.isCompleted()) {
            Observable<T> request = super.getRequestObservable(p);
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
                aggregates.remove(progressAggregate.aggregable.key());
            }
        }
    }
}
