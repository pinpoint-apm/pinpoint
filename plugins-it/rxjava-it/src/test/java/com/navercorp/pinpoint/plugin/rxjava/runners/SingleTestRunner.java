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
import com.navercorp.test.pinpoint.plugin.rxjava.service.EchoService;
import org.junit.Assert;
import rx.Single;
import rx.SingleSubscriber;
import rx.Subscription;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.singles.BlockingSingle;

import java.lang.reflect.Method;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.event;

/**
 * @author HyunGil Jeong
 */
public class SingleTestRunner {

    private final EchoService echoService = new EchoService();

    public void single() throws Exception {
        final String message = "Hello World";
        final AtomicReference<String> actualMessage = new AtomicReference<String>();
        final CountDownLatch latch = new CountDownLatch(1);

        Subscription subscription = echoService.echo(message)
                .subscribeOn(Schedulers.computation())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        actualMessage.set(s);
                        latch.countDown();
                    }
                });
        latch.await(500L, TimeUnit.MILLISECONDS);
        subscription.unsubscribe();
        Assert.assertEquals(message, actualMessage.get());

        TestHelper.awaitForSpanDataFlush();

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.printCache();

        // Skip rx java internal traces as they differ between versions and it's too much work to split the tests.
        // Instead, we can verify them indirectly by checking if user methods have been traced.
        verifier.ignoreServiceType("RX_JAVA_INTERNAL");

        Method subscribeMethod = Single.class.getDeclaredMethod("subscribe", Action1.class);
        verifier.verifyTrace(event("RX_JAVA", subscribeMethod));
        // event - RX_JAVA_INTERNAL some form of Worker.schedule(Action0)
        verifier.verifyTrace(event(ServiceType.ASYNC.getName(), "Asynchronous Invocation"));
        // event - RX_JAVA_INTERNAL some form of Action0 implementation's call() inside OperatorSubscribeOn that gets scheduled
        Method subscribeMethod2 = Single.class.getDeclaredMethod("subscribe", SingleSubscriber.class);
        verifier.verifyTrace(event("RX_JAVA", subscribeMethod2));
        Method echoMethod = EchoRepository.class.getDeclaredMethod("echo", String.class);
        verifier.verifyTrace(event(ServiceType.INTERNAL_METHOD.getName(), echoMethod));
    }

    public void singleError() throws Exception {
        final String message = "Hello World";
        final Exception expected = new RuntimeException("expected");
        final AtomicReference<Exception> actual = new AtomicReference<Exception>();
        final CountDownLatch latch = new CountDownLatch(1);
        Subscription subscription = echoService.echo(message, expected)
                .subscribeOn(Schedulers.computation())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        latch.countDown();
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        actual.set((Exception) throwable);
                        latch.countDown();
                    }
                });
        latch.await(500L, TimeUnit.MILLISECONDS);
        subscription.unsubscribe();
        Assert.assertSame(expected, actual.get());

        TestHelper.awaitForSpanDataFlush();

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.printCache();

        // Skip rx java internal traces as they differ between versions and it's too much work to split the tests.
        // Instead, we can verify them indirectly by checking if user methods have been traced.
        verifier.ignoreServiceType("RX_JAVA_INTERNAL");

        Method subscribeMethod = Single.class.getDeclaredMethod("subscribe", Action1.class, Action1.class);
        verifier.verifyTrace(event("RX_JAVA", subscribeMethod));
        // event - RX_JAVA_INTERNAL some form of Worker.schedule(Action0)
        verifier.verifyTrace(event(ServiceType.ASYNC.getName(), "Asynchronous Invocation"));
        // event - RX_JAVA_INTERNAL some form of Action0 implementation's call() inside OperatorSubscribeOn that gets scheduled
        Method subscribeMethod2 = Single.class.getDeclaredMethod("subscribe", SingleSubscriber.class);
        verifier.verifyTrace(event("RX_JAVA", subscribeMethod2));
        Method echoMethod = EchoRepository.class.getDeclaredMethod("echo", String.class, Exception.class);
        verifier.verifyTrace(event(ServiceType.INTERNAL_METHOD.getName(), echoMethod, expected));
    }

    public void blockingSingle() throws Exception {
        final String message = "Hello World";
        String actualMessage = echoService.echo(message)
                .subscribeOn(Schedulers.computation())
                .toBlocking()
                .value();
        Assert.assertEquals(message, actualMessage);

        TestHelper.awaitForSpanDataFlush();

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.printCache();

        // Skip rx java internal traces as they differ between versions and it's too much work to split the tests.
        // Instead, we can verify them indirectly by checking if user methods have been traced.
        verifier.ignoreServiceType("RX_JAVA_INTERNAL");

        Method toBlockingMethod = Single.class.getDeclaredMethod("toBlocking");
        verifier.verifyTrace(event("RX_JAVA", toBlockingMethod));
        Method valueMethod = BlockingSingle.class.getDeclaredMethod("value");
        verifier.verifyTrace(event("RX_JAVA", valueMethod));
        Method subscribe = Single.class.getDeclaredMethod("subscribe", SingleSubscriber.class);
        verifier.verifyTrace(event("RX_JAVA", subscribe));
        // event - RX_JAVA_INTERNAL some form of Worker.schedule(Action0)
        verifier.verifyTrace(event(ServiceType.ASYNC.getName(), "Asynchronous Invocation"));
        // event - RX_JAVA_INTERNAL some form of Action0 implementation's call() inside OperatorSubscribeOn that gets scheduled
        Method subscribeMethod2 = Single.class.getDeclaredMethod("subscribe", SingleSubscriber.class);
        verifier.verifyTrace(event("RX_JAVA", subscribeMethod2));
        Method echoMethod = EchoRepository.class.getDeclaredMethod("echo", String.class);
        verifier.verifyTrace(event(ServiceType.INTERNAL_METHOD.getName(), echoMethod));
    }
}
