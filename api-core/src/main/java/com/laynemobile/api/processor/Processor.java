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

import rx.Observable;
import rx.functions.Func1;
import rx.functions.Func2;

public interface Processor<T, P> extends Func1<P, Observable<T>> {

    // Marker interface
    interface Extension<T, P> {}

    interface Interceptor<T, P> extends Extension<T, P> {
        Observable<T> intercept(Chain<T, P> chain);

        interface Chain<T, P> {
            P params();

            Observable<T> proceed(P p);
        }
    }

    interface Checker<T, P> extends Extension<T, P> {
        void check(P p) throws Exception;
    }

    interface Modifier<T, P>
            extends Func2<P, Observable<T>, Observable<T>>,
            Extension<T, P> {}

    interface Parent<T, P>
            extends Processor<T, P>,
            Extension<T, P> {}
}
