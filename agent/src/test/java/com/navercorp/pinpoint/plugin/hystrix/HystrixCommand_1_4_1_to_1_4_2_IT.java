/*
 * Copyright 2016 Naver Corp.
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

package com.navercorp.pinpoint.plugin.hystrix;

import com.navercorp.pinpoint.bootstrap.plugin.test.Expectations;
import com.navercorp.pinpoint.bootstrap.plugin.test.ExpectedAnnotation;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifierHolder;
import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.JvmVersion;
import com.navercorp.pinpoint.test.plugin.PinpointPluginTestSuite;
import com.netflix.hystrix.Hystrix;
import com.netflix.hystrix.HystrixCommand;
import junit.framework.Assert;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.Method;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.annotation;

/**
 * Java 6 was dropped for Hystrix 1.4.1 and 1.4.2 - https://github.com/Netflix/Hystrix/issues/702
 *
 * Created by jack on 4/22/16.
 */
@RunWith(PinpointPluginTestSuite.class)
@JvmVersion(7)
@Dependency({"com.netflix.hystrix:hystrix-core:[1.4.1,1.4.2]","com.netflix.hystrix:hystrix-metrics-event-stream:1.1.2"})
public class HystrixCommand_1_4_1_to_1_4_2_IT {
    private static final ExpectedAnnotation expectedAnnotation = annotation("hystrix.subclass", "SayHelloCommand");

    @After
    public void teardown() {
        Hystrix.reset(60, TimeUnit.SECONDS);
    }

    @Test
    public void testSyncCall() throws Exception {
        String name = "Pinpoint";

        SayHelloCommand cmd = new SayHelloCommand(name);
        String result = cmd.execute();

        Assert.assertEquals("Hello Pinpoint!", result);

        /* when get the result from HystrixCommand, the com.netflix.hystrix.HystrixCommand$1.call()
         * intercepted may not done yet, so wait 2 sec for the spanEvent to be collected.
         * More info ref to HystrixObservableCallInterceptor.
         */
        try {Thread.sleep(1000*2);} catch (InterruptedException e) {e.printStackTrace();}

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.printCache();

        Method queue    = HystrixCommand.class.getMethod("queue");
        Class<?> inner=Class.forName("com.netflix.hystrix.HystrixCommand$1");
        Class<?> rx=Class.forName("rx.Subscriber");
        Method callCmd = inner.getDeclaredMethod("call", rx);

        verifier.verifyTrace(Expectations.async(
                Expectations.event("HYSTRIX_COMMAND", queue),
                Expectations.event("ASYNC", "Asynchronous Invocation"),
                Expectations.event("HYSTRIX_COMMAND", callCmd, expectedAnnotation)));

        // no more traces
        verifier.verifyTraceCount(0);
    }

    @Test
    public void testAsyncCall() throws Exception {
        String name = "Pinpoint";

        SayHelloCommand cmd = new SayHelloCommand(name);
        Future<String> future = cmd.queue();
        String result = future.get(6000, TimeUnit.MILLISECONDS);

        Assert.assertEquals("Hello Pinpoint!", result);

        // see comments above in testSyncCall()
        try {Thread.sleep(1000*2);} catch (InterruptedException e) {e.printStackTrace();}

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.printCache();

        Method queue    = HystrixCommand.class.getMethod("queue");
        Class<?> inner=Class.forName("com.netflix.hystrix.HystrixCommand$1");
        Class<?> rx=Class.forName("rx.Subscriber");
        Method callCmd = inner.getDeclaredMethod("call", rx);

        verifier.verifyTrace(Expectations.async(
                Expectations.event("HYSTRIX_COMMAND", queue),
                Expectations.event("ASYNC", "Asynchronous Invocation"),
                Expectations.event("HYSTRIX_COMMAND", callCmd, expectedAnnotation)
        ));

        // no more traces
        verifier.verifyTraceCount(0);
    }
}
