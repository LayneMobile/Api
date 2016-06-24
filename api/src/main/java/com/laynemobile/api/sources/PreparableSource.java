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

package com.laynemobile.api.sources;

import com.laynemobile.api.Params;
import com.laynemobile.api.processor.Processor;
import com.laynemobile.api.Source;

import rx.Observable;

public interface PreparableSource<T, P extends Params> extends Source<T, P> {
    /**
     * Prepare the observable after it's created.
     *
     * @param sourceRequest
     *         the source request observable
     * @param p
     *         the parameters
     *
     * @return the prepared source request observable
     */
    Observable<T> prepareSourceRequest(Observable<T> sourceRequest, P p);

    class Modifier<T, P extends Params> implements Processor.Modifier<T, P> {
        private final PreparableSource<T, P> source;

        private Modifier(PreparableSource<T, P> source) {
            this.source = source;
        }

        public static <T, P extends Params> Modifier<T, P> from(PreparableSource<T, P> source) {
            return new Modifier<>(source);
        }

        @Override public Observable<T> call(P p, Observable<T> request) {
            return source.prepareSourceRequest(request, p);
        }
    }
}
