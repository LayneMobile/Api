///*
// * Copyright 2016 Layne Mobile, LLC
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *     http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package com.laynemobile.api;
//
//import com.laynemobile.api.processor.Processor;
//
//import org.immutables.value.Value;
//
//import rx.Observable;
//import rx.Subscriber;
//
//@Value.Immutable
//abstract class AbstractSourceParent<T, P extends Params> implements Processor.Parent<T, P> {
//
//    abstract Source<T, P> source();
//
//    @Override public final Observable<T> call(P p) {
//        return Observable.create(new OnSubscribeImpl<>(source(), p));
//    }
//
//    private static final class OnSubscribeImpl<T, P extends Params> implements Observable.OnSubscribe<T> {
//        private final Source<T, P> source;
//        private final P p;
//
//        private OnSubscribeImpl(Source<T, P> source, P p) {
//            this.source = source;
//            this.p = p;
//        }
//
//        @Override public void call(final Subscriber<? super T> subscriber) {
//            if (subscriber.isUnsubscribed()) { return; }
//            source.call(p, new Subscriber<T>(subscriber) {
//                @Override public void onCompleted() {
//                    if (!subscriber.isUnsubscribed()) {
//                        subscriber.onCompleted();
//                    }
//                }
//
//                @Override public void onError(Throwable e) {
//                    if (!subscriber.isUnsubscribed()) {
//                        subscriber.onError(e);
//                    }
//                }
//
//                @Override public void onNext(T t) {
//                    if (!subscriber.isUnsubscribed()) {
//                        subscriber.onNext(t);
//                    }
//                }
//            });
//        }
//    }
//}
