///*
// * Copyright 2016 Layne Mobile, LLC
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *     http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package com.laynemobile.api.experimental;
//
//import com.laynemobile.api.Aggregable;
//import com.laynemobile.api.Api;
//import com.laynemobile.api.Builder;
//import com.laynemobile.api.Params;
//import com.laynemobile.api.Source;
//import com.laynemobile.api.sources.SourceBuilder;
//import com.laynemobile.api.sources.SourceHandler;
//import com.laynemobile.api.sources.modules.AggregableSourceModule;
//import com.laynemobile.api.sources.modules.NetworkSourceModule;
//import com.laynemobile.api.sources.modules.PreparableSourceModule;
//import com.laynemobile.api.sources.modules.SourceModule;
//import com.laynemobile.api.util.NetworkChecker;
//
//import java.util.List;
//
//import rx.Observable;
//import rx.Subscriber;
//import rx.functions.Action2;
//import rx.functions.Func1;
//import rx.functions.Func2;
//
//public final class ApiBuilder<T, P extends Params> implements Builder<Api<T, P>> {
//    private final SourceBuilder<T, P> builder = new SourceBuilder<T, P>();
//
//    public ApiBuilder() {}
//
//    public ApiBuilder<T, P> source(Action2<P, Subscriber<? super T>> source) {
//        return new SourceModuleBuilder()
//                .source(source)
//                .add();
//    }
//
//    public ApiBuilder<T, P> source(Func1<P, T> source) {
//        return new SourceModuleBuilder()
//                .source(source)
//                .add();
//    }
//
//    public ApiBuilder<T, P> requiresNetwork() {
//        return new NetworkSourceModuleBuilder()
//                .requiresNetwork()
//                .add();
//    }
//
//    public ApiBuilder<T, P> requiresNetwork(NetworkChecker checker) {
//        return new NetworkSourceModuleBuilder()
//                .requiresNetwork(checker)
//                .add();
//    }
//
//    public ApiBuilder<T, P> aggregate(Func1<P, Aggregable> action) {
//        return new AggregableSourceModuleBuilder()
//                .aggregate(action)
//                .add();
//    }
//
//    public ApiBuilder<T, P> prepareSource(Func2<Observable<T>, P, Observable<T>> func) {
//        return new PreparableSourceModuleBuilder()
//                .prepareSource(func)
//                .add();
//    }
//
//    public ApiBuilder<T, P> addModule(SourceHandler module) {
//        builder.module(module);
//        return this;
//    }
//
//    public ApiBuilder<T, P> addModules(SourceHandler... modules) {
//        builder.modules(modules);
//        return this;
//    }
//
//    public ApiBuilder<T, P> addModules(List<SourceHandler> modules) {
//        builder.modules(modules);
//        return this;
//    }
//
//    public Source<T, P> source() {
//        return builder.build();
//    }
//
//    public com.laynemobile.api.RequestProcessor<T, P> requestProcessor() {
//        return new com.laynemobile.api.RequestProcessor.Builder<T, P>()
//                .setSource(source())
//                .build();
//    }
//
//    @Override
//    public Api<T, P> build() {
//        return new Api<T, P>(requestProcessor()){};
//    }
//
//    public final class SourceModuleBuilder {
//        private final SourceModule<T, P> builder = new SourceModule<T, P>();
//
//        private SourceModuleBuilder() {
//        }
//
//        public SourceModuleBuilder source(Action2<P, Subscriber<? super T>> source) {
//            builder.source(source);
//            return this;
//        }
//
//        public SourceModuleBuilder source(Func1<P, T> source) {
//            builder.source(source);
//            return this;
//        }
//
//        public ApiBuilder<T, P> add() {
//            return addModule(builder.build());
//        }
//    }
//
//    public final class NetworkSourceModuleBuilder {
//        private final NetworkSourceModule builder = new NetworkSourceModule();
//
//        private NetworkSourceModuleBuilder() {
//        }
//
//        public NetworkSourceModuleBuilder requiresNetwork() {
//            builder.requiresNetwork();
//            return this;
//        }
//
//        public NetworkSourceModuleBuilder requiresNetwork(NetworkChecker checker) {
//            builder.requiresNetwork(checker);
//            return this;
//        }
//
//        public ApiBuilder<T, P> add() {
//            return addModule(builder.build());
//        }
//    }
//
//    public final class AggregableSourceModuleBuilder {
//        private final AggregableSourceModule<P> builder = new AggregableSourceModule<P>();
//
//        private AggregableSourceModuleBuilder() {
//        }
//
//        public AggregableSourceModuleBuilder aggregate(Func1<P, Aggregable> action) {
//            builder.aggregate(action);
//            return this;
//        }
//
//        public ApiBuilder<T, P> add() {
//            return addModule(builder.build());
//        }
//    }
//
//    public final class PreparableSourceModuleBuilder {
//        private final PreparableSourceModule<T, P> builder = new PreparableSourceModule<T, P>();
//
//        private PreparableSourceModuleBuilder() {
//        }
//
//        public PreparableSourceModuleBuilder prepareSource(Func2<Observable<T>, P, Observable<T>> func) {
//            builder.prepareSource(func);
//            return this;
//        }
//
//        public ApiBuilder<T, P> add() {
//            return addModule(builder.build());
//        }
//    }
//}
