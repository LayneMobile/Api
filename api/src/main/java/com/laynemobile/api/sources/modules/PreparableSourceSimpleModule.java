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

import com.laynemobile.api.Params;
import com.laynemobile.api.annotations.SourceHandlerModule;
import com.laynemobile.api.sources.PreparableSource;
import com.laynemobile.api.sources.SourceHandler;
import com.laynemobile.api.sources.SourceHandlerBuilder;
import rx.Observable;
import rx.functions.Func1;
import rx.functions.Func2;

@SourceHandlerModule(PreparableSource.class)
public final class PreparableSourceSimpleModule<T, P extends Params> implements SourceHandlerBuilder {
    private final PreparableSourceModule<T, P> module = new PreparableSourceModule<T, P>();

    public PreparableSourceSimpleModule<T, P> prepareSource(Func1<Observable<T>, Observable<T>> func) {
        return prepareSourceInternal(this, func);
    }

    @Override public SourceHandler build() {
        return module.build();
    }

    private static <T, P extends Params> PreparableSourceSimpleModule<T, P> prepareSourceInternal(
            PreparableSourceSimpleModule<T, P> module,
            final Func1<Observable<T>, Observable<T>> func) {
        // Create anonymous inner class in static context to avoid holding Module instance in memory
        module.module.prepareSource(new Func2<Observable<T>, P, Observable<T>>() {
            @Override public Observable<T> call(Observable<T> observable, P p) {
                return func.call(observable);
            }
        });
        return module;
    }
}
