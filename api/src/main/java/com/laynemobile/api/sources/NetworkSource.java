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
import com.laynemobile.api.internal.ApiLog;
import com.laynemobile.api.processor.Processor;
import com.laynemobile.api.util.NetworkChecker;

public interface NetworkSource<T, P extends Params> extends Source<T, P> {
    NetworkChecker getNetworkChecker();

    class Checker<T, P extends Params> implements Processor.Checker<T, P> {
        private static final String TAG = Checker.class.getSimpleName();

        private final NetworkSource<T, P> source;

        private Checker(NetworkSource<T, P> source) {
            this.source = source;
        }

        public static <T, P extends Params> Checker<T, P> from(NetworkSource<T, P> source) {
            return new Checker<>(source);
        }

        @Override public void check(P p) throws Exception {
            ApiLog.d(TAG, "checking network connection");
            NetworkChecker networkChecker = source.getNetworkChecker();
            if (networkChecker == null) {
                networkChecker = NetworkChecker.ALWAYS_AVAILABLE;
            }
            if (!networkChecker.isNetworkAvailable()) {
                ApiLog.i(TAG, "not running request. no network");
                throw new NetworkUnavailableException("no network");
            }
        }
    }
}
