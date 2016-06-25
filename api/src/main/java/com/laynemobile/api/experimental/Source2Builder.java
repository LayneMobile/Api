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

import com.laynemobile.api.Params;
import com.laynemobile.api.Source;
import com.laynemobile.api.SourceParent;
import com.laynemobile.api.processor.Processor;
import com.laynemobile.api.sources.SourceBuilder;
import com.laynemobile.api.sources.modules.SourceModule;
import com.laynemobile.api.types.TypeHandler;

import rx.Subscriber;
import rx.functions.Action2;
import rx.functions.Func1;

public class Source2Builder<T, P extends Params> {
    private final SourceBuilder<T, P> builder = new SourceBuilder<>();

    public Step2 source(Action2<P, Subscriber<? super T>> source) {
        return new Step1()
                .source(source)
                .add();
    }

    public Step2 source(Func1<P, T> source) {
        return new Step1()
                .source(source)
                .add();
    }

    private void addModule(TypeHandler<? extends Source> module) {
        builder.module(module);
    }

    public final class Step1 {
        private final SourceModule<T, P> builder = new SourceModule<T, P>();

        private Step1() {}

        public Step1 source(Action2<P, Subscriber<? super T>> source) {
            builder.source(source);
            return this;
        }

        public Step1 source(Func1<P, T> source) {
            builder.source(source);
            return this;
        }

        public Step2 add() {
            addModule(builder.build());
            return new Step2();
        }
    }

    public class Step2 {

        Source<T, P> build() {
            return builder.build();
        }

        Processor<T, P> requestProcessor() {
            Source<T, P> source = build();
            SourceParent.Builder<T, P> b = SourceParent.<T, P>builder()
                    .setSource(source);

//            // network checker
//            if (source instanceof NetworkSource) {
//                b.addCheckers(NetworkSource.Checker.from((NetworkSource<T, P>) source));
//            }
//
//            // preparable source modifier
//            if (source instanceof PreparableSource) {
//                b.addModifiers(PreparableSource.Modifier.from((PreparableSource<T, P>) source));
//            }
//
//            // Aggregate interceptor
//            if (source instanceof AggregableSource) {
//                b.addInterceptors(Interceptors.aggregate((AggregableSource<T, P>) source));
//            }

            return b.build();
        }
    }
}
