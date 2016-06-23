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
import com.laynemobile.api.Processor.Interceptor;
import com.laynemobile.api.Source;
import com.laynemobile.api.internal.request.Interceptors;

public interface AggregableSource<T, P extends Params> extends Source<T, P> {
    Aggregable getAggregable(P p);

    class Transformer<T, P extends Params> implements Interceptor.Transformer<AggregableSource<T, P>, Interceptor<T, P>> {
        @Override public Interceptor<T, P> call(final AggregableSource<T, P> source) {
            return Interceptors.aggregate(source);
        }
    }
}
