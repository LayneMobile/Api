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
import com.laynemobile.api.Request;
import com.laynemobile.api.Source;
import com.laynemobile.api.SourceProcessor;

import rx.Observable;

class DefaultRequestProcessor<T, P extends Params> {
    private static final String TAG = DefaultRequestProcessor.class.getSimpleName();

    private final SourceProcessor<T, P> sourceProcessor;

    DefaultRequestProcessor(Source<T, P> source) {
        this.sourceProcessor = DefaultSourceProcessor.create(source)
                .asSourceProcessor();
    }

    DefaultRequestProcessor(SourceProcessor<T, P> sourceProcessor) {
        this.sourceProcessor = sourceProcessor;
    }

    static <T, P extends Params> DefaultRequestProcessor<T, P> create(Source<T, P> source) {
        if (source instanceof AggregableSource) {
            return AggregableProcessor.create((AggregableSource<T, P>) source);
        }
        final DefaultSourceProcessor<T, P> sourceProcessor = DefaultSourceProcessor.create(source);
        return new DefaultRequestProcessor<>(sourceProcessor.asSourceProcessor());
    }

    static <T, P extends Params> DefaultRequestProcessor<T, P> create(SourceProcessor<T, P> sourceProcessor) {
        return new DefaultRequestProcessor<T, P>(sourceProcessor);
    }

    public final Request<T> getRequest(P p) {
        return Request.from(getRequestObservable(p));
    }

    protected Observable<T> getRequestObservable(final P p) {
        return sourceProcessor.getSourceRequest(p);
    }
}
