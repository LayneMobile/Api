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

package com.laynemobile.api.sources;

import com.laynemobile.api.Aggregable;
import com.laynemobile.api.Params;
import com.laynemobile.api.Request;
import com.laynemobile.api.Source;
import com.laynemobile.api.experimental.Processor;
import com.laynemobile.api.experimental.RequestProcessor;

public interface AggregableSource<T, P extends Params> extends Source<T, P> {
    Aggregable getAggregable(P p);

    class Transformer<T, P extends Params> implements Processor.Interceptor.Transformer<AggregableSource<T, P>, RequestProcessor.Interceptor<T, P>> {
        @Override public RequestProcessor.Interceptor<T, P> call(final AggregableSource<T, P> source) {
            return new RequestProcessor.Interceptor<T, P>() {
                @Override public Request<T> intercept(Processor.Interceptor.Chain<P, Request<T>> chain) {
                    P p = chain.params();
                    Aggregable aggregable = source.getAggregable(p);
                    if (aggregable != null) {
                        Object key = aggregable.key();
                        // TODO: look for existing
                        /*
                            Aggregate aggregate = find(key);
                            if (aggregate == null) {
                                Request<T> source = chain.proceed(p);
                                aggregate = new Aggregate(source);
                                put(key, aggregate);
                            }
                            return aggregate.request;
                         */
                    }
                    return chain.proceed(p);
                }
            };
        }
    }
}
