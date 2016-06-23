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

import org.immutables.value.Value;

@Value.Immutable
public abstract class AbstractSourceRequestProcessor<T, P extends Params>
        extends RequestInterceptProcessor<T, P> {

    abstract Source<T, P> source();

    @Value.Lazy
    @Override protected RequestProcessor<T, P> processor() {
        return SourceProcessor.<T, P>builder()
                .setSource(source())
                .build();
    }
}