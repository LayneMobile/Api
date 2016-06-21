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

package com.laynemobile.api.internal.request;

import com.laynemobile.api.Params;
import com.laynemobile.api.Request;
import com.laynemobile.api.RequestProcessor;
import com.laynemobile.api.Source;
import com.laynemobile.api.SourceProcessor;

public final class RequestProcessorBuilder<T, P extends Params> {
    private DefaultRequestProcessor<T, P> processor;

    public RequestProcessorBuilder() {}

    public void setSource(Source<T, P> source) {
        this.processor = DefaultRequestProcessor.create(source);
    }

    public void setSourceProcessor(SourceProcessor<T, P> sourceProcessor) {
        this.processor = DefaultRequestProcessor.create(sourceProcessor);
    }

    public RequestProcessor<T, P> build() {
        DefaultRequestProcessor<T, P> processor = this.processor;
        if (processor == null) {
            throw new IllegalStateException("not enough information specified to create request processor");
        }
        return new RequestProcessorImpl<>(processor);
    }

    private static final class RequestProcessorImpl<T, P extends Params> implements RequestProcessor<T, P> {
        private final DefaultRequestProcessor<T, P> impl;

        private RequestProcessorImpl(DefaultRequestProcessor<T, P> impl) {
            this.impl = impl;
        }

        @Override public Request<T> getRequest(P p) {
            return impl.getRequest(p);
        }
    }
}
