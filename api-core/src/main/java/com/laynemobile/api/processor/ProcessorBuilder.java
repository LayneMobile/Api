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

import com.laynemobile.api.Builder;
import com.laynemobile.api.processor.Processor.Checker;
import com.laynemobile.api.processor.Processor.Extension;
import com.laynemobile.api.processor.Processor.Interceptor;
import com.laynemobile.api.processor.Processor.Modifier;
import com.laynemobile.api.types.TypeBuilder;
import com.laynemobile.api.types.TypeToken;

import java.util.ArrayList;
import java.util.List;

public final class ProcessorBuilder<T, P, H> implements Builder<Processor<T, P>> {
    private final TypeBuilder<H> typeBuilder;
    private ProcessorHandlerParent<T, P, H> parent;
    private final List<ProcessorHandler<T, P, ? extends H>> handlers = new ArrayList<>();

    private ProcessorBuilder(TypeToken<H> type) {
        this.typeBuilder = new TypeBuilder<>(type);
    }

    public static <T, P, H> ProcessorBuilder<T, P, H> create(TypeToken<H> type,
            ProcessorHandlerParent<T, P, H> parent) {
        return new ProcessorBuilder<T, P, H>(type)
                .add(parent);
    }

    @SuppressWarnings("unchecked")
    public ProcessorBuilder<T, P, H> add(ProcessorHandler<T, P, ? extends H> handler) {
        typeBuilder.module(handler.typeHandler());
        if (handler instanceof ProcessorHandlerParent) {
            parent = (ProcessorHandlerParent<T, P, H>) handler;
        } else {
            handlers.add(handler);
        }
        return this;
    }

    public ProcessorBuilder<T, P, H> addAll(ProcessorHandler<T, P, ? extends H>... handlers) {
        for (ProcessorHandler<T, P, ? extends H> handler : handlers) {
            add(handler);
        }
        return this;
    }

    public ProcessorBuilder<T, P, H> addAll(List<ProcessorHandler<T, P, ? extends H>> handlers) {
        for (ProcessorHandler<T, P, ? extends H> handler : handlers) {
            add(handler);
        }
        return this;
    }

    @Override public final Processor<T, P> build() {
        if (parent == null) {
            throw new IllegalArgumentException("ProcessorHandlerParent cannot be null");
        }

        final H h = typeBuilder.build();
        final ImmutableInterceptProcessor.Builder<T, P> builder
                = ImmutableInterceptProcessor.<T, P>builder()
                .setProcessor(parent.extension(h));

        for (ProcessorHandler<T, P, ? extends H> handler : handlers) {
            Extension<T, P> extension = extension(handler, h);
            if (extension instanceof Checker) {
                builder.addCheckers((Checker<T, P>) extension);
            } else if (extension instanceof Modifier) {
                builder.addModifiers((Modifier<T, P>) extension);
            } else if (extension instanceof Interceptor) {
                builder.addInterceptors((Interceptor<T, P>) extension);
            } else {
                throw new IllegalArgumentException("unknown Extension type: " + extension);
            }
        }

        return builder.build();
    }

    @SuppressWarnings("unchecked")
    private <S extends H> Extension<T, P> extension(ProcessorHandler<T, P, S> handler, H h) {
        return handler.extension((S) h);
    }
}
