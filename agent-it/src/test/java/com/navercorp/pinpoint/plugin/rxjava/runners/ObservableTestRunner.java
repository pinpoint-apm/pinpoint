/*
 * Copyright 2018 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.plugin.rxjava.runners;

import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifierHolder;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.test.pinpoint.plugin.rxjava.repository.EchoRepository;
import com.navercorp.test.pinpoint.plugin.rxjava.service.EchoesService;
import org.junit.Assert;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.observables.BlockingObservable;
import rx.schedulers.Schedulers;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.event;

/**
 * @author HyunGil Jeong
 */
public class ObservableTestRunner {

    private final EchoesService echoesService = new EchoesService();

    public void observable() throws Exception {
        final List<String> messages = Arrays.asList("Hello", "World");
        final CountDownLatch latch = new CountDownLatch(1);
        final List<String> actualMessages = Collections.synchronizedList(new ArrayList<String>());

        Subscription subscription = echoesService.echo(messages)
                .subscribeOn(Schedulers.computation())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        actualMessages.add(s);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        latch.countDown();
                    }
                }, new Action0() {
                    @Override
                    public void call() {
                        latch.countDown();
                    }
                });
        latch.await(500L, TimeUnit.MILLISECONDS);
        subscription.unsubscribe();
        Assert.assertEquals(messages, actualMessages);

        TestHelper.awaitForSpanDataFlush();

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.printCache();

        // Skip rx java internal traces as they differ between versions and it's too much work to split the tests.
        // Instead, we can verify them indirectly by checking if user methods have been traced.
        verifier.ignoreServiceType("RX_JAVA_INTERNAL");

        Method subscribeMethod = Observable.class.getDeclaredMethod("subscribe", Action1.class, Action1.class, Action0.class);
        verifier.verifyTrace(event("RX_JAVA", subscribeMethod));
        // event - RX_JAVA_INTERNAL some form of Worker.schedule(Action0)
        verifier.verifyTrace(event(ServiceType.ASYNC.getName(), "Asynchronous Invocation"));
        // event - RX_JAVA_INTERNAL some form of Action0 implementation's call() inside OperatorSubscribeOn that gets scheduled
        Method echoMethod = EchoRepository.class.getDeclaredMethod("echo", String.class);
        for (int i = 0; i < messages.size(); i++) {
            verifier.verifyTrace(event(ServiceType.INTERNAL_METHOD.getName(), echoMethod));
        }
    }

    public void observableError() throws Exception {
        final List<String> messages = Arrays.asList("Hello", "World");
        final Exception expected = new RuntimeException("expected");
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicReference<Exception> actual = new AtomicReference<Exception>();
        Subscription subscription = echoesService.echo(messages, expected)
                .subscribeOn(Schedulers.computation())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        // ignore
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        try {
                            actual.set((Exception) throwable);
                        } finally {
                            latch.countDown();
                        }
                    }
                }, new Action0() {
                    @Override
                    public void call() {
                        latch.countDown();
                    }
                });
        boolean complete = latch.await(500L, TimeUnit.MILLISECONDS);
        subscription.unsubscribe();
        Assert.assertTrue("onError never called", complete);
        Assert.assertSame(expected, actual.get());

        TestHelper.awaitForSpanDataFlush();

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.printCache();

        // Skip rx java internal traces as they differ between versions and it's too much work to split the tests.
        // Instead, we can verify them indirectly by checking if user methods have been traced.
        verifier.ignoreServiceType("RX_JAVA_INTERNAL");

        Method subscribeMethod = Observable.class.getDeclaredMethod("subscribe", Action1.class, Action1.class, Action0.class);
        verifier.verifyTrace(event("RX_JAVA", subscribeMethod));
        // event - RX_JAVA_INTERNAL some form of Worker.schedule(Action0)
        verifier.verifyTrace(event(ServiceType.ASYNC.getName(), "Asynchronous Invocation"));
        // event - RX_JAVA_INTERNAL some form of Action0 implementation's call() inside OperatorSubscribeOn that gets scheduled
        Method echoMethod = EchoRepository.class.getDeclaredMethod("echo", String.class, Exception.class);
        verifier.verifyTrace(event(ServiceType.INTERNAL_METHOD.getName(), echoMethod, expected));
    }

    public void blockingObservable() throws Exception {
        final List<String> messages = Arrays.asList("Hello", "World");

        List<String> actualMessages = echoesService.echo(messages)
                .subscribeOn(Schedulers.computation())
                .toList()
                .toBlocking()
                .single();
        Assert.assertEquals(messages, actualMessages);

        TestHelper.awaitForSpanDataFlush();

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.printCache();

        // Skip rx java internal traces as they differ between versions and it's too much work to split the tests.
        // Instead, we can verify them indirectly by checking if user methods have been traced.
        verifier.ignoreServiceType("RX_JAVA_INTERNAL");

        Method toBlockingMethod = Observable.class.getDeclaredMethod("toBlocking");
        verifier.verifyTrace(event("RX_JAVA", toBlockingMethod));
        Method singleMethod = BlockingObservable.class.getDeclaredMethod("single");
        verifier.verifyTrace(event("RX_JAVA", singleMethod));
        Method subscribeMethod = Observable.class.getDeclaredMethod("subscribe", Subscriber.class);
        verifier.verifyTrace(event("RX_JAVA", subscribeMethod));
        // event - RX_JAVA_INTERNAL some form of Worker.schedule(Action0)
        verifier.verifyTrace(event(ServiceType.ASYNC.getName(), "Asynchronous Invocation"));
        // event - RX_JAVA_INTERNAL some form of Action0 implementation's call() inside OperatorSubscribeOn that gets scheduled
        Method echoMethod = EchoRepository.class.getDeclaredMethod("echo", String.class);
        for (int i = 0; i < messages.size(); i++) {
            verifier.verifyTrace(event(ServiceType.INTERNAL_METHOD.getName(), echoMethod));
        }
    }
}
