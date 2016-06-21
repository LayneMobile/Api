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
import com.laynemobile.api.Source;
import com.laynemobile.api.SourceProcessor;
import rx.Observable;

public final class SourceProcessorBuilder<T, P extends Params> {
    private Source<T, P> source;

    public void setSource(Source<T, P> source) {
        this.source = source;
    }

    public SourceProcessor<T, P> build() {
        if (source == null) {
            throw new IllegalStateException("source must not be null");
        }
        return new SourceProcessorImpl<T, P>(DefaultSourceProcessor.create(source));
    }

    private static final class SourceProcessorImpl<T, P extends Params> implements SourceProcessor<T, P> {
        private final DefaultSourceProcessor<T, P> impl;

        private SourceProcessorImpl(DefaultSourceProcessor<T, P> impl) {
            this.impl = impl;
        }

        @Override public Observable<T> getSourceRequest(P p) {
            return impl.getSourceRequest(p);
        }

        @Override public Observable<T> peekSourceRequest(P p) {
            return impl.peekSourceRequest(p);
        }
    }
}
