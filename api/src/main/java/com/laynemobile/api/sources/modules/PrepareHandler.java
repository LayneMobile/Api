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
import com.laynemobile.api.processor.Processor.Extension;
import com.laynemobile.api.processor.ProcessorHandler;
import com.laynemobile.api.processor.ProcessorHandlerBuilder;
import com.laynemobile.api.sources.PreparableSource;
import com.laynemobile.api.types.MethodHandler;
import com.laynemobile.api.types.MethodResult;
import com.laynemobile.api.types.TypeHandler;

import java.lang.reflect.Method;

import rx.Observable;
import rx.functions.Func2;

@SourceHandlerModule(PreparableSource.class)
public final class PrepareHandler<T, P extends Params> implements ProcessorHandlerBuilder<T, P, PreparableSource<T, P>> {
    private Func2<Observable<T>, P, Observable<T>> func;

    public PrepareHandler<T, P> prepareSource(Func2<Observable<T>, P, Observable<T>> func) {
        this.func = func;
        return this;
    }

    @Override public ProcessorHandler<T, P, PreparableSource<T, P>> build() {
        return build(TypeHandler.<PreparableSource<T, P>>builder()
                .handle("prepareSourceRequest", new Handler<T, P>(func))
                .build());
    }

    private static <T, P extends Params> ProcessorHandler<T, P, PreparableSource<T, P>> build(
            final TypeHandler<PreparableSource<T, P>> typeHandler) {
        return new ProcessorHandler<T, P, PreparableSource<T, P>>() {
            @Override public TypeHandler<PreparableSource<T, P>> typeHandler() {
                return typeHandler;
            }

            @Override public Extension<T, P> extension(PreparableSource<T, P> source) {
                return PreparableSource.Modifier.from(source);
            }
        };
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
