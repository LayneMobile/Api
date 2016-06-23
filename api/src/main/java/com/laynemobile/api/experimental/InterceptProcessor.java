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

package com.laynemobile.api.experimental;

import com.google.common.collect.ImmutableList;

import java.util.List;

public abstract class InterceptProcessor<T, R> implements Processor<T, R> {

    protected abstract Processor<T, R> processor();

    protected abstract List<? extends Interceptor<T, R>> interceptors();

    @Override public final R call(T t) {
        return Chain.with(this)
                .proceed(t);
    }

    private static final class Chain<T, R> implements Interceptor.Chain<T, R> {
        private final Processor<T, R> processor;
        private final ImmutableList<? extends Interceptor<T, R>> interceptors;
        private final int index;
        private final T value;

        private Chain(Processor<T, R> processor, List<? extends Interceptor<T, R>> interceptors) {
            this.processor = processor;
            this.interceptors = ImmutableList.copyOf(interceptors);
            this.index = 0;
            this.value = null;
        }

        private Chain(Chain<T, R> prev, T value) {
            this.processor = prev.processor;
            this.interceptors = prev.interceptors;
            this.index = prev.index + 1;
            this.value = value;
        }

        private static <T, R> Chain<T, R> with(InterceptProcessor<T, R> parent) {
            return new Chain<>(parent.processor(), parent.interceptors());
        }

        @Override public T value() {
            return value;
        }

        @Override public R proceed(T t) {
            int index = this.index;
            List<? extends Interceptor<T, R>> interceptors = this.interceptors;
            if (index < interceptors.size()) {
                return interceptors.get(index)
                        .intercept(next(t));
            }
            return processor.call(t);
        }

        private Chain<T, R> next(T t) {
            return new Chain<>(this, t);
        }
    }
}
