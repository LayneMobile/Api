package com.laynemobile.api.experimental;

import com.laynemobile.api.Aggregable;
import com.laynemobile.api.Builder;
import com.laynemobile.api.Params;
import com.laynemobile.api.Source;
import com.laynemobile.api.processor.Processor;
import com.laynemobile.api.processor.ProcessorBuilder;
import com.laynemobile.api.processor.ProcessorHandler;
import com.laynemobile.api.processor.ProcessorHandlerParent;
import com.laynemobile.api.sources.modules.AggregateHandler;
import com.laynemobile.api.sources.modules.NetworkCheckHandler;
import com.laynemobile.api.sources.modules.PrepareHandler;
import com.laynemobile.api.sources.modules.SourceHandler;
import com.laynemobile.api.types.TypeToken;
import com.laynemobile.api.util.NetworkChecker;

import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Action2;
import rx.functions.Func1;
import rx.functions.Func2;

public final class ApiBuilder<T, P extends Params> {
    private final TypeToken<Source<T, P>> type;

    public ApiBuilder() {
        this.type = new TypeToken<Source<T, P>>() {};
    }

    public Extensions<T, P> source(Action2<P, Subscriber<? super T>> source) {
        return new SourceModuleBuilder()
                .source(source)
                .add();
    }

    public Extensions<T, P> source(Func1<P, T> source) {
        return new SourceModuleBuilder()
                .source(source)
                .add();
    }

    public final class SourceModuleBuilder {
        private final SourceHandler<T, P> builder = new SourceHandler<>();

        private SourceModuleBuilder() {}

        public SourceModuleBuilder source(Action2<P, Subscriber<? super T>> source) {
            builder.source(source);
            return this;
        }

        public SourceModuleBuilder source(Func1<P, T> source) {
            builder.source(source);
            return this;
        }

        public Extensions<T, P> add() {
            return new Extensions<>(type, builder.build());
        }
    }

    public static final class Extensions<T, P extends Params> implements Builder<Processor<T, P>> {
        private final ProcessorBuilder<T, P, Source<T, P>> builder;

        private Extensions(TypeToken<Source<T, P>> type, ProcessorHandlerParent<T, P, Source<T, P>> parent) {
            builder = ProcessorBuilder.create(type);
            builder.add(parent);
        }

        public Extensions<T, P> requiresNetwork() {
            return new NetworkSourceModuleBuilder()
                    .requiresNetwork()
                    .add();
        }

        public Extensions<T, P> requiresNetwork(NetworkChecker checker) {
            return new NetworkSourceModuleBuilder()
                    .requiresNetwork(checker)
                    .add();
        }

        public Extensions<T, P> aggregate(Func1<P, Aggregable> action) {
            return new AggregableSourceModuleBuilder()
                    .aggregate(action)
                    .add();
        }

        public Extensions<T, P> prepareSource(Func2<Observable<T>, P, Observable<T>> func) {
            return new PreparableSourceModuleBuilder()
                    .prepareSource(func)
                    .add();
        }

        public Extensions<T, P> add(ProcessorHandler<T, P, ? extends Source<T, P>> module) {
            builder.add(module);
            return this;
        }

        public Extensions<T, P> addAll(ProcessorHandler<T, P, ? extends Source<T, P>>... modules) {
            builder.addAll(modules);
            return this;
        }

        public Extensions<T, P> addAll(List<ProcessorHandler<T, P, ? extends Source<T, P>>> modules) {
            builder.addAll(modules);
            return this;
        }

        @Override public Processor<T, P> build() {
            return builder.build();
        }

        public final class NetworkSourceModuleBuilder {
            private final NetworkCheckHandler<T, P> builder = new NetworkCheckHandler<>();

            private NetworkSourceModuleBuilder() {}

            public NetworkSourceModuleBuilder requiresNetwork() {
                builder.requiresNetwork();
                return this;
            }

            public NetworkSourceModuleBuilder requiresNetwork(NetworkChecker checker) {
                builder.requiresNetwork(checker);
                return this;
            }

            public Extensions<T, P> add() {
                return Extensions.this.add(builder.build());
            }
        }

        public final class AggregableSourceModuleBuilder {
            private final AggregateHandler<T, P> builder = new AggregateHandler<>();

            private AggregableSourceModuleBuilder() {}

            public AggregableSourceModuleBuilder aggregate(Func1<P, Aggregable> action) {
                builder.aggregate(action);
                return this;
            }

            public Extensions<T, P> add() {
                return Extensions.this.add(builder.build());
            }
        }

        public final class PreparableSourceModuleBuilder {
            private final PrepareHandler<T, P> builder = new PrepareHandler<>();

            private PreparableSourceModuleBuilder() {}

            public PreparableSourceModuleBuilder prepareSource(Func2<Observable<T>, P, Observable<T>> func) {
                builder.prepareSource(func);
                return this;
            }

            public Extensions<T, P> add() {
                return Extensions.this.add(builder.build());
            }
        }
    }
}
