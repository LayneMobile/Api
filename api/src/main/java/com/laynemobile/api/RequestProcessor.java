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

package com.laynemobile.api;

import com.laynemobile.api.internal.request.RequestProcessorBuilder;

public interface RequestProcessor<T, P extends Params> {
    Request<T> getRequest(P p);

    final class Builder<T, P extends Params> implements com.laynemobile.api.Builder<RequestProcessor<T, P>> {
        private final RequestProcessorBuilder<T, P> impl = new RequestProcessorBuilder<T, P>();

        public Builder<T, P> setSource(Source<T, P> source) {
            impl.setSource(source);
            return this;
        }

        public Builder<T, P> setSourceProcessor(SourceProcessor<T, P> sourceProcessor) {
            impl.setSourceProcessor(sourceProcessor);
            return this;
        }

        @Override public RequestProcessor<T, P> build() {
            return impl.build();
        }
    }
}
