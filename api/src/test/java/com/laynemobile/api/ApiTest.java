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

package com.laynemobile.api;

import com.laynemobile.api.aggregables.SimpleAggregable;
import com.laynemobile.api.experimental.ApiBuilder;
import com.laynemobile.api.processor.Processor;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import rx.Observable;
import rx.functions.Func1;
import rx.functions.Func2;

import static org.junit.Assert.assertEquals;

public class ApiTest {
    @Before public void setup() {}

    @After public void teardown() {}

    @Test public void createApi() {
        final Params p = new Params() {};
        final Object o = new Object();
        Processor<Object, Params> processor = new ApiBuilder<>()
                .source(new Func1<Params, Object>() {
                    @Override public Object call(Params params) {
                        return o;
                    }
                })
                .requiresNetwork()
                .aggregate(new Func1<Params, Aggregable>() {
                    @Override public Aggregable call(Params params) {
                        return new SimpleAggregable(params);
                    }
                })
                .prepareSource(new Func2<Observable<Object>, Params, Observable<Object>>() {
                    @Override public Observable<Object> call(Observable<Object> objectObservable, Params params) {
                        return objectObservable;
                    }
                })
                .build();

        Object that = processor.call(p)
                .toBlocking()
                .last();
        assertEquals(o, that);
    }
}
