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

import com.laynemobile.api.subscribers.ApiSubscribers;
import com.laynemobile.api.util.ResultCallable;

import java.util.concurrent.Callable;

import rx.Notification;
import rx.Observable;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class Request<T> {
    private final Observable<T> observable;

    protected Request(Observable.OnSubscribe<T> onSubscribe) {
        this(Observable.create(onSubscribe));
    }

    protected Request(Observable<T> observable) {
        this.observable = observable;
    }

    public static <T> Request<T> create(Observable.OnSubscribe<T> onSubscribe) {
        return new Request<T>(onSubscribe);
    }

    public static <T> Request<T> just(T t) {
        return new Request<T>(Observable.just(t));
    }

    public static <T> Request<T> from(Observable<T> observable) {
        return new Request<T>(observable);
    }

    public static <T> Request<T> from(Callable<T> callable) {
        return new Request<T>(ApiObservables.from(callable));
    }

    public static <T> Request<T> from(ResultCallable<T> callable) {
        return new Request<T>(ApiObservables.from(callable));
    }

    public static <T> Request<T> error(Throwable throwable) {
        return new Request<T>(Observable.<T>error(throwable));
    }

    public final Observable<T> asObservable() {
        return observable;
    }

    public final Observable<T> asAsyncObservable() {
        return asObservable()
                .subscribeOn(Schedulers.io());
    }

    public final Iterable<Notification<T>> asIterable() {
        return asObservable()
                .materialize()
                .toBlocking()
                .toIterable();
    }

    public final Callable<T> asCallable() {
        return ApiObservables.callable(asObservable());
    }

    public final ResultCallable<T> asResultCallable() {
        return ApiObservables.resultCallable(asObservable());
    }

    public final <R> Request<R> map(Func1<? super T, ? extends R> func) {
        return new Request<R>(observable.map(func));
    }

    public final <R> Request<R> concatMap(Func1<? super T, ? extends Observable<? extends R>> func) {
        return new Request<R>(observable.concatMap(func));
    }

    public static <T> Extender<T> extender() {
        return new Extender<T>() {
            @Override public Request<T> call(Observable.OnSubscribe<T> onSubscribe) {
                return new Request<T>(onSubscribe);
            }
        };
    }

    public final void execute() {
        asAsyncObservable()
                .subscribe(ApiSubscribers.empty());
    }

    public interface Extender<T> extends Func1<Observable.OnSubscribe<T>, Request<T>> {}
}
