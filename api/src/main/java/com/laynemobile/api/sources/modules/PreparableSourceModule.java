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
import com.laynemobile.api.sources.SourceHandlerBuilder;
import com.laynemobile.api.types.MethodHandler;
import com.laynemobile.api.types.MethodResult;
import com.laynemobile.api.types.TypeHandler;
import com.laynemobile.api.types.TypeToken;

import java.lang.reflect.Method;

import rx.Observable;
import rx.functions.Func2;

@SourceHandlerModule(PreparableSource.class)
public final class PreparableSourceModule<T, P extends Params> implements SourceHandlerBuilder<PreparableSource> {
    private Func2<Observable<T>, P, Observable<T>> func;

    public PreparableSourceModule<T, P> prepareSource(Func2<Observable<T>, P, Observable<T>> func) {
        this.func = func;
        return this;
    }

    @Override public TypeHandler<PreparableSource> build() {
        return TypeHandler.builder(new TypeToken<PreparableSource>() {})
                .handle("prepareSourceRequest", new Handler<>(func))
                .build();
    }

    private static final class Handler<T, P extends Params> implements MethodHandler {
        private final Func2<Observable<T>, P, Observable<T>> func;

        private Handler(Func2<Observable<T>, P, Observable<T>> func) {
            this.func = func;
        }

        @Override
        @SuppressWarnings("unchecked")
        public boolean handle(Object proxy, Method method, Object[] args, MethodResult result) throws Throwable {
            Class<?>[] paramTypes = method.getParameterTypes();
            if (paramTypes.length == 2
                    && paramTypes[0].isAssignableFrom(Observable.class)
                    && paramTypes[1].isAssignableFrom(Params.class)) {
                result.set(func.call((Observable<T>) args[0], (P) args[1]));
                return true;
            }
            return false;
        }
    }
}
