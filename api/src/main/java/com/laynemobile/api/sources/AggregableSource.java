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
import com.laynemobile.api.Source;
import com.laynemobile.api.internal.request.Interceptors;
import com.laynemobile.api.processor.Processor;

import rx.Observable;

public interface AggregableSource<T, P extends Params> extends Source<T, P> {
    Aggregable getAggregable(P p);

    class Interceptor<T, P extends Params> implements Processor.Interceptor<T, P> {
        private final Processor.Interceptor<T, P> actual;

        private Interceptor(AggregableSource<T, P> source) {
            this.actual = Interceptors.aggregate(source);
        }

        public static <T, P extends Params> Interceptor<T, P> from(AggregableSource<T, P> source) {
            return new Interceptor<>(source);
        }

        @Override public Observable<T> intercept(Chain<T, P> chain) {
            return actual.intercept(chain);
        }
    }
}
