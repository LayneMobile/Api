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

import org.immutables.value.Value;

import rx.Observable;
import rx.Subscriber;

@Value.Immutable
abstract class AbstractSourceProcessor<T, P extends Params> extends InterceptProcessor<T, P> {
    abstract Source<T, P> source();

    @Value.Derived
    @Override public Processor<T, P> processor() {
        return new ProcessorImpl();
    }

    private final class ProcessorImpl implements Processor<T, P> {
        @Override public Observable<T> call(P p) {
            return Observable.create(new OnSubscribeImpl(p));
        }
    }

    private final class OnSubscribeImpl implements Observable.OnSubscribe<T> {
        private final P p;

        private OnSubscribeImpl(P p) {
            this.p = p;
        }

        @Override public void call(final Subscriber<? super T> subscriber) {
            if (subscriber.isUnsubscribed()) { return; }
            source().call(p, new Subscriber<T>(subscriber) {
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
