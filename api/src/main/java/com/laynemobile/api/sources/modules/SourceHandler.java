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
import com.laynemobile.api.Source;
import com.laynemobile.api.SourceParent;
import com.laynemobile.api.annotations.SourceHandlerModule;
import com.laynemobile.api.processor.Processor;
import com.laynemobile.api.processor.ProcessorHandlerParent;
import com.laynemobile.api.processor.ProcessorHandlerParentBuilder;
import com.laynemobile.api.types.MethodHandler;
import com.laynemobile.api.types.MethodResult;
import com.laynemobile.api.types.TypeHandler;
import com.laynemobile.api.types.TypeToken;

import java.lang.reflect.Method;

import rx.Subscriber;
import rx.functions.Action2;
import rx.functions.Func1;

@SourceHandlerModule(Source.class)
public final class SourceHandler<T, P extends Params> implements ProcessorHandlerParentBuilder<T, P, Source<T, P>> {
    private Action2<P, Subscriber<? super T>> source;

    public SourceHandler<T, P> source(Action2<P, Subscriber<? super T>> source) {
        this.source = source;
        return this;
    }

    public SourceHandler<T, P> source(Func1<P, T> source) {
        return sourceInternal(this, source);
    }

    @Override public ProcessorHandlerParent<T, P, Source<T, P>> build() {
        if (source == null) {
            throw new IllegalStateException("source must be set");
        }
        return build(TypeHandler.builder(new TypeToken<Source<T, P>>() {})
                .handle("call", new Handler<>(source))
                .build());
    }

    private static <T, P extends Params> ProcessorHandlerParent<T, P, Source<T, P>> build(
            final TypeHandler<Source<T, P>> typeHandler) {
        return new ProcessorHandlerParent<T, P, Source<T, P>>() {
            @Override public Processor.Parent<T, P> extension(Source<T, P> source) {
                return SourceParent.<T, P>builder()
                        .setSource(source)
                        .build();
            }

            @Override public TypeHandler<Source<T, P>> typeHandler() {
                return typeHandler;
            }
        };
    }

    private static <T, P extends Params> SourceHandler<T, P> sourceInternal(SourceHandler<T, P> module,
            final Func1<P, T> source) {
        // Create anonymous inner class in static context to avoid holding Module instance in memory
        return module.source(new Action2<P, Subscriber<? super T>>() {
            @Override public void call(P p, Subscriber<? super T> subscriber) {
                try {
                    T t = source.call(p);
                    if (!subscriber.isUnsubscribed()) {
                        subscriber.onNext(t);
                        subscriber.onCompleted();
                    }
                } catch (Throwable e) {
                    if (!subscriber.isUnsubscribed()) {
                        subscriber.onError(e);
                    }
                }
            }
        });
    }

    private static final class Handler<T, P extends Params> implements MethodHandler {
        private final Action2<P, Subscriber<? super T>> source;

        private Handler(Action2<P, Subscriber<? super T>> source) {
            this.source = source;
        }

        @Override
        @SuppressWarnings("unchecked")
        public boolean handle(Object proxy, Method method, Object[] args, MethodResult result) throws Throwable {
            Class<?>[] paramTypes = method.getParameterTypes();
            if (paramTypes.length == 2
                    && paramTypes[0].isAssignableFrom(Params.class)
                    && paramTypes[1].isAssignableFrom(Subscriber.class)) {
                source.call((P) args[0], (Subscriber<? super T>) args[1]);
                return true;
            }
            return false;
        }
    }
}
