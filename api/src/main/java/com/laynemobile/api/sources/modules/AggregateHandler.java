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

import com.laynemobile.api.Aggregable;
import com.laynemobile.api.Params;
import com.laynemobile.api.annotations.SourceHandlerModule;
import com.laynemobile.api.processor.Processor;
import com.laynemobile.api.processor.ProcessorHandler;
import com.laynemobile.api.processor.ProcessorHandlerBuilder;
import com.laynemobile.api.sources.AggregableSource;
import com.laynemobile.api.types.MethodHandler;
import com.laynemobile.api.types.MethodResult;
import com.laynemobile.api.types.TypeHandler;

import java.lang.reflect.Method;

import rx.functions.Func1;

@SourceHandlerModule(AggregableSource.class)
public final class AggregateHandler<T, P extends Params> implements ProcessorHandlerBuilder<T, P, AggregableSource<T, P>> {
    private Func1<P, Aggregable> action;

    public AggregateHandler<T, P> aggregate(Func1<P, Aggregable> action) {
        this.action = action;
        return this;
    }

    @Override public ProcessorHandler<T, P, AggregableSource<T, P>> build() {
        if (action == null) {
            throw new IllegalStateException("source must be set");
        }
        return build(TypeHandler.<AggregableSource<T, P>>builder()
                .handle("getAggregable", new Handler<P>(action))
                .build());
    }

    private static <T, P extends Params> ProcessorHandler<T, P, AggregableSource<T, P>> build(
            final TypeHandler<AggregableSource<T, P>> typeHandler) {
        return new ProcessorHandler<T, P, AggregableSource<T, P>>() {
            @Override public TypeHandler<AggregableSource<T, P>> typeHandler() {
                return typeHandler;
            }

            @Override public Processor.Extension<T, P> extension(AggregableSource<T, P> source) {
                return AggregableSource.Interceptor.from(source);
            }
        };
    }

    private static final class Handler<P extends Params> implements MethodHandler {
        private final Func1<P, Aggregable> action;

        private Handler(Func1<P, Aggregable> action) {
            this.action = action;
        }

        @Override
        @SuppressWarnings("unchecked")
        public boolean handle(Object proxy, Method method, Object[] args, MethodResult result) throws Throwable {
            Class<?>[] paramTypes = method.getParameterTypes();
            if (paramTypes.length == 1 && paramTypes[0].isAssignableFrom(Params.class)) {
                result.set(action.call((P) args[0]));
                return true;
            }
            return false;
        }
    }
}
