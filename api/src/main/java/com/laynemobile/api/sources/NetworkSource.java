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
import com.laynemobile.api.Source;
import com.laynemobile.api.exceptions.NetworkUnavailableException;
import com.laynemobile.api.experimental.Processor;
import com.laynemobile.api.experimental.RequestProcessor;
import com.laynemobile.api.internal.ApiLog;
import com.laynemobile.api.util.NetworkChecker;

import rx.Observable;

public interface NetworkSource<T, P extends Params> extends Source<T, P> {
    NetworkChecker getNetworkChecker();

    class Transformer<T, P extends Params> implements Processor.Interceptor.Transformer<NetworkSource<T, P>, RequestProcessor.Interceptor<T, P>> {
        private static final String TAG = Transformer.class.getSimpleName();

        @Override public RequestProcessor.Interceptor<T, P> call(final NetworkSource<T, P> source) {
            return new RequestProcessor.Interceptor<T, P>() {
                @Override public Observable<T> intercept(Processor.Interceptor.Chain<P, Observable<T>> chain) {
                    P p = chain.value();
                    ApiLog.d(TAG, "checking network connection");
                    NetworkChecker networkChecker = source.getNetworkChecker();
                    if (networkChecker == null) {
                        networkChecker = NetworkChecker.ALWAYS_AVAILABLE;
                    }
                    if (!networkChecker.isNetworkAvailable()) {
                        ApiLog.i(TAG, "not running request. no network");
                        return Observable.error(new NetworkUnavailableException("no network"));
                    }
                    return chain.proceed(p);
                }
            };
        }
    }
}
