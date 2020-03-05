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
import rx.functions.Action0;
import rx.functions.Action1;
import rx.observables.ConnectableObservable;
import rx.schedulers.Schedulers;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.event;

/**
 * @author HyunGil Jeong
 */
public class ConnectableObservableTestRunner {

    private final EchoesService echoesService = new EchoesService();

    public void connectableObservable() throws Exception {
        final int numSubscribers = 2;
        final List<String> messages = Arrays.asList("Hello", "World");
        final List<String> actualMessages = Collections.synchronizedList(new ArrayList<String>());
        final CountDownLatch completeLatch = new CountDownLatch(numSubscribers);

        final Action1<String> onNext = new Action1<String>() {
            @Override
            public void call(String s) {
                actualMessages.add(s);
            }
        };
        final Action1<Throwable> onError = new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                completeLatch.countDown();
            }
        };
        final Action0 onCompleted = new Action0() {
            @Override
            public void call() {
                completeLatch.countDown();
            }
        };

        ConnectableObservable<String> echoes = echoesService.echo(messages)
                .subscribeOn(Schedulers.computation())
                .publish();
        for (int i = 0; i < numSubscribers; i++) {
            echoes.subscribe(onNext, onError, onCompleted);
        }
        echoes.connect();

        completeLatch.await(500L, TimeUnit.MILLISECONDS);

        for (int i = 0; i < actualMessages.size(); i++) {
            String expectedMessage = messages.get(i / numSubscribers);
            String actualMessage = actualMessages.get(i);
            Assert.assertEquals(expectedMessage, actualMessage);
        }

        TestHelper.awaitForSpanDataFlush();

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.printCache();

        // Skip rx java internal traces as they differ between versions and it's too much work to split the tests.
        // Instead, we can verify them indirectly by checking if user methods have been traced.
        verifier.ignoreServiceType("RX_JAVA_INTERNAL");

        Method publishMethod = Observable.class.getDeclaredMethod("publish");
        verifier.verifyTrace(event("RX_JAVA", publishMethod));
        // event - RX_JAVA_INTERNAL some form of Worker.schedule(Action0) X numSubscribers
        Method subscribeMethod = Observable.class.getDeclaredMethod("subscribe", Action1.class, Action1.class, Action0.class);
        for (int i = 0; i < numSubscribers; i++) {
            verifier.verifyTrace(event("RX_JAVA", subscribeMethod));
        }
        Method connectMethod = ConnectableObservable.class.getDeclaredMethod("connect");
        verifier.verifyTrace(event("RX_JAVA", connectMethod));
        // event - RX_JAVA_INTERNAL some form of Worker.schedule(Action0) for scheduling a single connectable observable
        verifier.verifyTrace(event(ServiceType.ASYNC.getName(), "Asynchronous Invocation"));
        // event - RX_JAVA_INTERNAL some form of Action0 implementation's call() inside OperatorSubscribeOn that gets scheduled
        Method echoMethod = EchoRepository.class.getDeclaredMethod("echo", String.class);
        for (int i = 0; i < numSubscribers; i++) {
            verifier.verifyTrace(event(ServiceType.INTERNAL_METHOD.getName(), echoMethod));
        }
    }

    public void connectableObservableError() throws Exception {
        final int numSubscribers = 3;
        final List<String> messages = Arrays.asList("Hello", "World");
        final Exception expected = new RuntimeException("expected");
        final CountDownLatch completeLatch = new CountDownLatch(numSubscribers);
        final List<Exception> actualExceptions = Collections.synchronizedList(new ArrayList<Exception>());

        final Action1<String> onNext = new Action1<String>() {
            @Override
            public void call(String s) {
                // ignore
            }
        };
        final Action1<Throwable> onError = new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                actualExceptions.add((Exception) throwable);
                completeLatch.countDown();
            }
        };
        final Action0 onCompleted = new Action0() {
            @Override
            public void call() {
                completeLatch.countDown();
            }
        };

        ConnectableObservable<String> echoes = echoesService.echo(messages, expected)
                .subscribeOn(Schedulers.computation())
                .publish();
        for (int i = 0; i < numSubscribers; i++) {
            echoes.subscribe(onNext, onError, onCompleted);
        }
        echoes.connect();

        completeLatch.await(500L, TimeUnit.MILLISECONDS);

        Assert.assertEquals(numSubscribers, actualExceptions.size());
        for (Exception actualException : actualExceptions) {
            Assert.assertSame(expected, actualException);
        }

        TestHelper.awaitForSpanDataFlush();

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.printCache();

        // Skip rx java internal traces as they differ between versions and it's too much work to split the tests.
        // Instead, we can verify them indirectly by checking if user methods have been traced.
        verifier.ignoreServiceType("RX_JAVA_INTERNAL");

        Method publishMethod = Observable.class.getDeclaredMethod("publish");
        verifier.verifyTrace(event("RX_JAVA", publishMethod));
        // event - RX_JAVA_INTERNAL some form of Worker.schedule(Action0) X numSubscribers
        Method subscribeMethod = Observable.class.getDeclaredMethod("subscribe", Action1.class, Action1.class, Action0.class);
        for (int i = 0; i < numSubscribers; i++) {
            verifier.verifyTrace(event("RX_JAVA", subscribeMethod));
        }
        Method connectMethod = ConnectableObservable.class.getDeclaredMethod("connect");
        verifier.verifyTrace(event("RX_JAVA", connectMethod));
        // event - RX_JAVA_INTERNAL some form of Worker.schedule(Action0) for scheduling a single connectable observable
        verifier.verifyTrace(event(ServiceType.ASYNC.getName(), "Asynchronous Invocation"));
        // event - RX_JAVA_INTERNAL some form of Action0 implementation's call() inside OperatorSubscribeOn that gets scheduled
        Method echoMethod = EchoRepository.class.getDeclaredMethod("echo", String.class, Exception.class);
        verifier.verifyTrace(event(ServiceType.INTERNAL_METHOD.getName(), echoMethod, expected));
    }
}
