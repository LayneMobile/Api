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

import com.laynemobile.api.annotations.GenerateApiBuilder;
import com.laynemobile.api.params.SimpleParams;
import com.laynemobile.api.sources.modules.AggregableSourceSimpleModule;
import com.laynemobile.api.sources.modules.NetworkSourceModule;
import com.laynemobile.api.sources.modules.PreparableSourceSimpleModule;
import com.laynemobile.api.sources.modules.SourceSimpleModule;

@GenerateApiBuilder({
        SourceSimpleModule.class,
        NetworkSourceModule.class,
        AggregableSourceSimpleModule.class,
        PreparableSourceSimpleModule.class
})
public class SimpleApi<T> extends BaseApi<T, SimpleParams> {
    protected SimpleApi(RequestProcessor<T, SimpleParams> requestProcessor) {
        super(requestProcessor);
    }

    public final Request<T> getRequest() {
        return requestProcessor()
                .getRequest(SimpleParams.INSTANCE);
    }
}
