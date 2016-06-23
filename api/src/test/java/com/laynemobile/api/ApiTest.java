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

//
//package api;
//
//import org.junit.Assert;
//
//import org.junit.After;
//import org.junit.Before;
//import org.junit.Test;
//
//import java.util.concurrent.Callable;
//import java.util.concurrent.TimeUnit;
//
//import com.laynemobile.api.internal.ApiLog;
//import com.laynemobile.api.internal.subscribers.LatchSubscriber;
//import rx.Observable;
//import rx.Subscription;
//
//import static org.mockito.Mockito.mock;
//import static org.mockito.Mockito.spy;
//import static org.mockito.Mockito.times;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.verifyNoMoreInteractions;
//import static org.mockito.Mockito.when;
//
//public class ApiTest {
//    @Before
//    public void setup() {
//        ApiLog.setLogger(new ConsoleLogger());
//    }
//
//    @After
//    public void teardown() {
//    }
//
//    @Test
//    public void testRequestAggregation() throws Exception {
//        final String value = "test";
//        @SuppressWarnings("unchecked")
//        final Callable<String> callable = (Callable<String>) mock(Callable.class);
//        when(callable.call()).thenReturn(value);
//        final AggregableApi<String> api = AggregableApi.create(callable, 10);
//        final AggregableApi.Params params = new AggregableApi.Params("key");
//        Observable<String> request = api.request(params).asAsyncObservable();
//        Observable<String> request2 = api.request(params).asAsyncObservable();
//        Observable<String> request3 = api.request(params).asAsyncObservable();
//
//        LatchSubscriber<String> subscriber = new LatchSubscriber<String>();
//        Observable.merge(request, request2, request3)
//                .subscribe(subscriber);
//        subscriber.await();
//
//        // Assert three onNext's were sent, but callable was called only once
//        verify(callable, times(1)).call();
//        Assert.assertNull(subscriber.getError());
//        Assert.assertTrue(subscriber.isCompleted());
//        Assert.assertEquals(value, subscriber.getLatest());
//        Assert.assertEquals(3, subscriber.getOnNextCount());
//
//        // Now make call again
//        Thread.sleep(TimeUnit.SECONDS.toMillis(2));
//        subscriber = new LatchSubscriber<String>();
//        request = api.request(params).asAsyncObservable();
//        request2 = api.request(params).asAsyncObservable();
//        request3 = api.request(params).asAsyncObservable();
//        Observable.merge(request, request2, request3)
//                .subscribe(subscriber);
//        subscriber.await();
//
//        // Assert three onNext's were sent, but callable wasn't called again
//        verifyNoMoreInteractions(callable);
//        Assert.assertNull(subscriber.getError());
//        Assert.assertTrue(subscriber.isCompleted());
//        Assert.assertEquals(value, subscriber.getLatest());
//        Assert.assertEquals(3, subscriber.getOnNextCount());
//    }
//
//    @Test
//    public void testRequestAggregationUnsubscribe() throws Exception {
//        final String value = "test";
//        final Callable<String> callable = spy(new Callable<String>() {
//            @Override public String call() throws Exception {
//                try {
//                    Thread.sleep(5000);
//                } catch (InterruptedException e) {
//                    ApiLog.printAllStackTraces("testing");
//                    throw e;
//                }
//                return value;
//            }
//        });
//        final AggregableApi<String> api = AggregableApi.create(callable, 10);
//        final AggregableApi.Params params = new AggregableApi.Params("key");
//        Observable<String> request = api.request(params).asAsyncObservable();
//
//        LatchSubscriber<String> subscriber = new LatchSubscriber<String>();
//        Subscription sub = request.subscribe(subscriber);
//        Thread.sleep(1000);
//        sub.unsubscribe();
//
//        // Assert callable called, nothing else
//        verify(callable, times(1)).call();
//        Assert.assertNull(subscriber.getError());
//        Assert.assertFalse(subscriber.isCompleted());
//        Assert.assertEquals(0, subscriber.getOnNextCount());
//
//        // Now make call again
//        subscriber = new LatchSubscriber<String>();
//        request = api.request(params).asAsyncObservable();
//        Observable<String> request2 = api.request(params).asAsyncObservable();
//        Observable<String> request3 = api.request(params).asAsyncObservable();
//        Observable.merge(request, request2, request3)
//                .subscribe(subscriber);
//        subscriber.await();
//
//        // Assert three onNext's were sent, but callable wasn't called again
//        verifyNoMoreInteractions(callable);
//        Assert.assertNull(subscriber.getError());
//        Assert.assertTrue(subscriber.isCompleted());
//        Assert.assertEquals(value, subscriber.getLatest());
//        Assert.assertEquals(3, subscriber.getOnNextCount());
//    }
//}
