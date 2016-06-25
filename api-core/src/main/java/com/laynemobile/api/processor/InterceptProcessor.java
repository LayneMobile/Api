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

package com.laynemobile.api.processor;

import com.google.common.collect.ImmutableList;

import java.util.List;

import rx.Observable;
import rx.functions.Func0;

public abstract class InterceptProcessor<T, P> implements Processor<T, P> {
    public abstract Processor<T, P> processor();

    public abstract List<Checker<T, P>> checkers();

    public abstract List<Modifier<T, P>> modifiers();

    public abstract List<Interceptor<T, P>> interceptors();

    @Override public final Observable<T> call(final P p) {
        return Observable.defer(new DeferFunction(p));
    }

    private final class DeferFunction implements Func0<Observable<T>> {
        private final ImmutableChain chain;
        private final P params;

        private DeferFunction(P params) {
            this.chain = new ImmutableChain();
            this.params = params;
        }

        @Override public Observable<T> call() {
            return chain.proceed(params);
        }
    }

    private final class ImmutableProcessor implements Processor<T, P> {
        private final Processor<T, P> processor;
        private final ImmutableList<Checker<T, P>> checkers;
        private final ImmutableList<Modifier<T, P>> modifiers;

        private ImmutableProcessor() {
            this.processor = processor();
            this.checkers = ImmutableList.copyOf(checkers());
            this.modifiers = ImmutableList.copyOf(modifiers());
        }

        @Override public Observable<T> call(P p) {
            // Validate with checkers
            for (Checker<T, P> checker : checkers) {
                try {
                    checker.check(p);
                } catch (Exception e) {
                    return Observable.error(e);
                }
            }

            // Make actual call
            Observable<T> result = processor.call(p);

            // Allow modifications to original result
            for (Modifier<T, P> modifier : modifiers) {
                result = modifier.call(p, result);
            }

            // return potentially modified  result
            return result;
        }
    }

    private final class ImmutableChain implements Interceptor.Chain<T, P> {
        private final ImmutableProcessor processor;
        private final ImmutableList<Interceptor<T, P>> interceptors;
        private final int index;
        private final P params;

        private ImmutableChain() {
            this.processor = new ImmutableProcessor();
            this.interceptors = ImmutableList.copyOf(interceptors());
            this.index = 0;
            this.params = null;
        }

        private ImmutableChain(ImmutableChain prev, P params) {
            this.processor = prev.processor;
            this.interceptors = prev.interceptors;
            this.index = prev.index + 1;
            this.params = params;
        }

        @Override public P params() {
            return params;
        }

        @Override public Observable<T> proceed(P p) {
            int index = this.index;
            List<Interceptor<T, P>> interceptors = this.interceptors;
            if (index < interceptors.size()) {
                return interceptors.get(index)
                        .intercept(next(p));
            }
            return processor.call(p);
        }

        private ImmutableChain next(P p) {
            return new ImmutableChain(this, p);
        }
    }
}
