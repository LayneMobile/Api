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

import rx.Observable;

public interface RequestProcessor<T, P extends Params> extends Processor<P, Observable<T>> {

    interface Transformer<T, P extends RequestProcessor<?, ?>>
            extends Processor.Transformer<T, P> {}

    interface Interceptor<T, P extends Params>
            extends Processor.Interceptor<P, Observable<T>> {

        interface Transformer<T, I extends Interceptor<?, ?>>
                extends Processor.Interceptor.Transformer<T, I> {}
    }
}
