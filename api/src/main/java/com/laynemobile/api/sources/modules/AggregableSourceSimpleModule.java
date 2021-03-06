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

package com.laynemobile.api.sources.modules;

import com.laynemobile.api.params.SimpleParams;
import com.laynemobile.api.sources.AggregableSource;
import com.laynemobile.api.sources.SourceHandler;
import com.laynemobile.api.Aggregable;
import com.laynemobile.api.aggregables.SimpleAggregable;
import com.laynemobile.api.annotations.SourceHandlerModule;
import com.laynemobile.api.sources.SourceHandlerBuilder;
import rx.functions.Func1;

@SourceHandlerModule(AggregableSource.class)
public final class AggregableSourceSimpleModule implements SourceHandlerBuilder {
    private final AggregableSourceModule<SimpleParams> module = new AggregableSourceModule<SimpleParams>();

    public AggregableSourceSimpleModule aggregate() {
        return aggregate(SimpleAggregable.DEFAULT_KEEP_ALIVE_SECONDS);
    }

    public AggregableSourceSimpleModule aggregate(int keepAliveSeconds) {
        return aggregate(keepAliveSeconds, SimpleAggregable.DEFAULT_KEEP_ALIVE_ON_ERROR);
    }

    public AggregableSourceSimpleModule aggregate(int keepAliveSeconds, boolean keepAliveOnError) {
        return aggregateInternal(this, keepAliveSeconds, keepAliveOnError);
    }

    @Override public SourceHandler build() {
        return module.build();
    }

    private static AggregableSourceSimpleModule aggregateInternal(AggregableSourceSimpleModule module,
            final int keepAliveSeconds, final boolean keepAliveOnError) {
        // Create anonymous inner class in static context to avoid holding Module instance in memory
        module.module.aggregate(new Func1<SimpleParams, Aggregable>() {
            @Override public Aggregable call(SimpleParams simpleParams) {
                return new SimpleAggregable(SimpleParams.INSTANCE, keepAliveSeconds, keepAliveOnError);
            }
        });
        return module;
    }
}
