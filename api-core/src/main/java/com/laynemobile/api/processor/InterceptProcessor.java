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

public abstract class InterceptProcessor<T, P> implements Processor<T, P> {
    public abstract Processor<T, P> processor();

    public abstract List<Checker<T, P>> checkers();

    public abstract List<Modifier<T, P>> modifiers();

    public abstract List<Interceptor<T, P>> interceptors();

    @Override public final Observable<T> call(P p) {
        return new Chain()
                .proceed(p);
    }

    private Observable<T> process(P p) {
        // Validate with checkers
        List<Checker<T, P>> checkers = ImmutableList.copyOf(checkers());
        for (Checker<T, P> checker : checkers) {
            try {
                checker.check(p);
            } catch (Exception e) {
                return Observable.error(e);
            }
        }

        // Make actual call
        Observable<T> result = processor().call(p);

        // Allow modifications to original result
        List<Modifier<T, P>> modifiers = ImmutableList.copyOf(modifiers());
        for (Modifier<T, P> modifier : modifiers) {
            result = modifier.call(p, result);
        }

        // return potentially modified  result
        return result;
    }

    private final class Chain implements Interceptor.Chain<T, P> {
        private final ImmutableList<? extends Interceptor<T, P>> interceptors;
        private final int index;
        private final P params;

        private Chain() {
            this.interceptors = ImmutableList.copyOf(interceptors());
            this.index = 0;
            this.params = null;
        }

        private Chain(Chain prev, P params) {
            this.interceptors = prev.interceptors;
            this.index = prev.index + 1;
            this.params = params;
        }

        @Override public P params() {
            return params;
        }

        @Override public Observable<T> proceed(P p) {
            int index = this.index;
            List<? extends Interceptor<T, P>> interceptors = this.interceptors;
            if (index < interceptors.size()) {
                return interceptors.get(index)
                        .intercept(next(p));
            }
            return process(p);
        }

        private Chain next(P p) {
            return new Chain(this, p);
        }
    }
}
