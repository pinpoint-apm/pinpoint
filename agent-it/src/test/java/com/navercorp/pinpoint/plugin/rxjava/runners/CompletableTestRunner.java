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
import com.navercorp.test.pinpoint.plugin.rxjava.service.ShoutService;
import org.junit.Assert;
import rx.Completable;
import rx.Subscription;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

import java.lang.reflect.Method;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.event;

/**
 * @author HyunGil Jeong
 */
public class CompletableTestRunner {

    private final ShoutService shoutService = new ShoutService();

    public void completable() throws Exception {
        final String message = "Hello World";
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicBoolean isCompleted = new AtomicBoolean(false);
        Subscription subscription = shoutService.shout(message)
                .subscribeOn(Schedulers.computation())
                .doOnCompleted(new Action0() {
                    @Override
                    public void call() {
                        isCompleted.set(true);
                        latch.countDown();
                    }
                })
                .doOnError(new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        latch.countDown();
                    }
                })
                .subscribe();
        latch.await(500L, TimeUnit.MILLISECONDS);
        subscription.unsubscribe();
        Assert.assertTrue(isCompleted.get());

        TestHelper.awaitForSpanDataFlush();

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.printCache();

        // Skip rx java internal traces as they differ between versions and it's too much work to split the tests.
        // Instead, we can verify them indirectly by checking if user methods have been traced.
        verifier.ignoreServiceType("RX_JAVA_INTERNAL");

        Method subscribeMethod = Completable.class.getDeclaredMethod("subscribe");
        verifier.verifyTrace(event("RX_JAVA", subscribeMethod));
        // event - RX_JAVA_INTERNAL some form of Worker.schedule(Action0)
        verifier.verifyTrace(event(ServiceType.ASYNC.getName(), "Asynchronous Invocation"));
        // event - RX_JAVA_INTERNAL some form of Action0 implementation's call() inside OperatorSubscribeOn that gets scheduled
        Method shoutMethod = EchoRepository.class.getDeclaredMethod("shout", String.class);
        verifier.verifyTrace(event(ServiceType.INTERNAL_METHOD.getName(), shoutMethod));
    }

    public void completableError() throws Exception {
        final String message = "Hello World";
        final Exception expected = new RuntimeException("expected");
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicReference<Exception> actual = new AtomicReference<Exception>();
        Subscription subscription = shoutService.shout(message, expected)
                .subscribeOn(Schedulers.computation())
                .doOnCompleted(new Action0() {
                    @Override
                    public void call() {
                        latch.countDown();
                    }
                })
                .doOnError(new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        actual.set((Exception) throwable);
                        latch.countDown();
                    }
                })
                .subscribe();
        latch.await(500L, TimeUnit.MILLISECONDS);
        subscription.unsubscribe();
        Assert.assertSame(expected, actual.get());

        TestHelper.awaitForSpanDataFlush();

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.printCache();

        // Skip rx java internal traces as they differ between versions and it's too much work to split the tests.
        // Instead, we can verify them indirectly by checking if user methods have been traced.
        verifier.ignoreServiceType("RX_JAVA_INTERNAL");

        verifier.awaitTrace(event(ServiceType.ASYNC.getName(), "Asynchronous Invocation"), 20, 500);

        Method subscribeMethod = Completable.class.getDeclaredMethod("subscribe");
        verifier.verifyTrace(event("RX_JAVA", subscribeMethod));
        // event - RX_JAVA_INTERNAL some form of Worker.schedule(Action0)
        verifier.verifyTrace(event(ServiceType.ASYNC.getName(), "Asynchronous Invocation"));
        // event - RX_JAVA_INTERNAL some form of Action0 implementation's call() inside OperatorSubscribeOn that gets scheduled
        Method shoutMethod = EchoRepository.class.getDeclaredMethod("shout", String.class, Exception.class);
        verifier.verifyTrace(event(ServiceType.INTERNAL_METHOD.getName(), shoutMethod, expected));
    }
}
