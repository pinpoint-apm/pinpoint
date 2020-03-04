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
import rx.functions.Func1;
import rx.observables.GroupedObservable;
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
public class GroupedObservableTestRunner {

    private final EchoesService echoesService = new EchoesService();

    public void groupedObservable() throws Exception {
        int numMessageChunks = 2;
        final List<String> messages = new ArrayList<String>();
        final List<String> messageChunk = Arrays.asList("Hello", "World");
        for (int i = 0; i < numMessageChunks; i++) {
            messages.addAll(messageChunk);
        }
        final CountDownLatch completeLatch = new CountDownLatch(1);
        final List<String> helloMessages = Collections.synchronizedList(new ArrayList<String>());
        final List<String> worldMessages = Collections.synchronizedList(new ArrayList<String>());

        Observable<GroupedObservable<String, String>> grouped = echoesService.echo(messages)
                .subscribeOn(Schedulers.computation())
                .groupBy(new Func1<String, String>() {
                    @Override
                    public String call(String s) {
                        return s;
                    }
                });
        grouped.subscribe(new Action1<GroupedObservable<String, String>>() {
            @Override
            public void call(final GroupedObservable<String, String> groupedObservable) {
                String key = groupedObservable.getKey();
                if (key.equals("Hello")) {
                    groupedObservable.subscribe(new Action1<String>() {
                        @Override
                        public void call(String s) {
                            helloMessages.add(s);
                        }
                    });
                } else if (key.equals("World")) {
                    groupedObservable.subscribe(new Action1<String>() {
                        @Override
                        public void call(String s) {
                            worldMessages.add(s);
                        }
                    });
                }
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                completeLatch.countDown();
            }
        }, new Action0() {
            @Override
            public void call() {
                completeLatch.countDown();
            }
        });

        completeLatch.await(500L, TimeUnit.MILLISECONDS);
        Assert.assertEquals(messages.size() / messageChunk.size(), helloMessages.size());
        Assert.assertEquals(messages.size() / messageChunk.size(), worldMessages.size());
        for (String helloMessage : helloMessages) {
            Assert.assertEquals("Hello", helloMessage);
        }
        for (String worldMessage : worldMessages) {
            Assert.assertEquals("World", worldMessage);
        }

        TestHelper.awaitForSpanDataFlush();

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.awaitTrace(event(ServiceType.ASYNC.getName(), "Asynchronous Invocation"), 20, 3000);
        verifier.printCache();

        // Skip rx java internal traces as they differ between versions and it's too much work to split the tests.
        // Instead, we can verify them indirectly by checking if user methods have been traced.
        verifier.ignoreServiceType("RX_JAVA_INTERNAL");

        Method groupByMethod = Observable.class.getDeclaredMethod("groupBy", Func1.class);
        verifier.verifyTrace(event("RX_JAVA", groupByMethod));
        Method subscribeMethod = Observable.class.getDeclaredMethod("subscribe", Action1.class, Action1.class, Action0.class);
        verifier.verifyTrace(event("RX_JAVA", subscribeMethod));
        // event - RX_JAVA_INTERNAL some form of Worker.schedule(Action0)
        verifier.verifyTrace(event(ServiceType.ASYNC.getName(), "Asynchronous Invocation"));
        // event - RX_JAVA_INTERNAL some form of Action0 implementation's call() inside OperatorSubscribeOn that gets scheduled
        Method echoMethod = EchoRepository.class.getDeclaredMethod("echo", String.class);
        Method groupedObservableSubscribeMethod = Observable.class.getDeclaredMethod("subscribe", Action1.class);
        for (int i = 0; i < numMessageChunks; i++) {
            for (int j = 0; j < messageChunk.size(); j++) {
                verifier.verifyTrace(event(ServiceType.INTERNAL_METHOD.getName(), echoMethod));
                if (i == 0) {
                    verifier.verifyTrace(event("RX_JAVA", groupedObservableSubscribeMethod));
                }
            }
        }
    }
}
