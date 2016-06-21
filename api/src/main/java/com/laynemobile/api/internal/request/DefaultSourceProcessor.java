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
import com.laynemobile.api.params.NetworkParams;
import com.laynemobile.api.Source;
import com.laynemobile.api.SourceProcessor;
import com.laynemobile.api.exceptions.NetworkUnavailableException;
import com.laynemobile.api.internal.ApiLog;
import com.laynemobile.api.sources.NetworkSource;
import com.laynemobile.api.sources.PreparableSource;
import com.laynemobile.api.util.NetworkChecker;
import rx.Observable;
import rx.Subscriber;

class DefaultSourceProcessor<T, P extends Params> {
    private static final String TAG = DefaultSourceProcessor.class.getSimpleName();

    private final Source<T, P> source;

    protected DefaultSourceProcessor(Source<T, P> source) {
        this.source = source;
    }

    static <T, P extends Params> DefaultSourceProcessor<T, P> create(Source<T, P> source) {
        return new DefaultSourceProcessor<>(source);
    }

    public Observable<T> getSourceRequest(P p) {
        Observable<T> request = Observable.create(new OnSubscribeImpl(p));
        if (source instanceof PreparableSource) {
            return ((PreparableSource<T, P>) source).prepareSourceRequest(request, p);
        }
        return request;
    }

    public Observable<T> peekSourceRequest(P p) {
        return null;
    }

    private NetworkChecker getNetworkChecker() {
        if (source instanceof NetworkSource) {
            return ((NetworkSource) source).getNetworkChecker();
        }
        return null;
    }

    SourceProcessor<T, P> asSourceProcessor() {
        return new SourceProcessor<T, P>() {
            @Override public Observable<T> getSourceRequest(P p) {
                return DefaultSourceProcessor.this.getSourceRequest(p);
            }

            @Override public Observable<T> peekSourceRequest(P p) {
                return DefaultSourceProcessor.this.peekSourceRequest(p);
            }
        };
    }

    private final class OnSubscribeImpl implements Observable.OnSubscribe<T> {
        private final P p;

        private OnSubscribeImpl(P p) {
            this.p = p;
        }

        @Override public void call(final Subscriber<? super T> subscriber) {
            if (subscriber.isUnsubscribed()) { return; }
            if (source instanceof NetworkSource || p instanceof NetworkParams) {
                ApiLog.d(TAG, "checking network connection");
                NetworkChecker networkChecker = getNetworkChecker();
                if (networkChecker == null) {
                    networkChecker = NetworkChecker.ALWAYS_AVAILABLE;
                }
                if (!networkChecker.isNetworkAvailable()) {
                    ApiLog.i(TAG, "not running request. no network");
                    subscriber.onError(new NetworkUnavailableException("no network"));
                    return;
                }
            }
            source.call(p, new Subscriber<T>(subscriber) {
                @Override public void onCompleted() {
                    if (!subscriber.isUnsubscribed()) {
                        subscriber.onCompleted();
                    }
                }

                @Override public void onError(Throwable e) {
                    if (!subscriber.isUnsubscribed()) {
                        subscriber.onError(e);
                    }
                }

                @Override public void onNext(T t) {
                    if (!subscriber.isUnsubscribed()) {
                        subscriber.onNext(t);
                    }
                }
            });
        }
    }
}
