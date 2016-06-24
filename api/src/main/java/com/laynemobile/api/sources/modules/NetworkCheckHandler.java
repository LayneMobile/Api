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
import com.laynemobile.api.processor.Processor;
import com.laynemobile.api.processor.ProcessorHandler;
import com.laynemobile.api.processor.ProcessorHandlerBuilder;
import com.laynemobile.api.sources.NetworkSource;
import com.laynemobile.api.types.MethodHandler;
import com.laynemobile.api.types.MethodResult;
import com.laynemobile.api.types.TypeHandler;
import com.laynemobile.api.util.NetworkChecker;

import java.lang.reflect.Method;

@SourceHandlerModule(NetworkSource.class)
public final class NetworkCheckHandler<T, P extends Params> implements ProcessorHandlerBuilder<T, P, NetworkSource<T, P>> {
    private NetworkChecker networkChecker;

    public NetworkCheckHandler requiresNetwork() {
        this.networkChecker = null;
        return this;
    }

    public NetworkCheckHandler requiresNetwork(NetworkChecker checker) {
        this.networkChecker = checker;
        return this;
    }

    @Override public ProcessorHandler<T, P, NetworkSource<T, P>> build() {
        return build(TypeHandler.<NetworkSource<T, P>>builder()
                .handle("getNetworkChecker", new Handler(networkChecker))
                .build());
    }

    private static final <T, P extends Params> ProcessorHandler<T, P, NetworkSource<T, P>> build(
            final TypeHandler<NetworkSource<T, P>> typeHandler) {
        return new ProcessorHandler<T, P, NetworkSource<T, P>>() {
            @Override public TypeHandler<NetworkSource<T, P>> typeHandler() {
                return typeHandler;
            }

            @Override public Processor.Extension<T, P> extension(NetworkSource<T, P> source) {
                return NetworkSource.Checker.from(source);
            }
        };
    }

    private static final class Handler implements MethodHandler {
        private final NetworkChecker networkChecker;

        private Handler(NetworkChecker networkChecker) {
            this.networkChecker = networkChecker;
        }

        @Override
        public boolean handle(Object proxy, Method method, Object[] args, MethodResult result) throws Throwable {
            Class<?>[] paramTypes = method.getParameterTypes();
            if (paramTypes.length == 0) {
                result.set(networkChecker);
                return true;
            }
            return false;
        }
    }
}
