/**
 * Copyright 2014 NAVER Corp.
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

import java.lang.reflect.Method;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.netflix.hystrix.Hystrix;
import com.netflix.hystrix.HystrixCommand;
import junit.framework.Assert;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.navercorp.pinpoint.bootstrap.plugin.test.Expectations;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifierHolder;
import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.PinpointAgent;
import com.navercorp.pinpoint.test.plugin.PinpointPluginTestSuite;

/**
 * @see HystrixCommandIT
 * @author Jiaqi Feng
 */
@RunWith(PinpointPluginTestSuite.class)
@Dependency({"com.netflix.hystrix:hystrix-core:1.3.20","com.netflix.hystrix:hystrix-metrics-event-stream:1.1.2"})
public class HystrixCommandIT {

    @After
    public void teardown() {
        Hystrix.reset(1, TimeUnit.SECONDS);
    }

    @Test
    public void testSyncCall() throws Exception {
        String name = "Pinpoint";

        SayHelloCommand cmd = new SayHelloCommand(name);
        String result = cmd.execute();

        Assert.assertEquals("Hello Pinpoint!", result);

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.printCache();

        Method queue    = HystrixCommand.class.getMethod("queue");
        Method executeCmd = HystrixCommand.class.getDeclaredMethod("executeCommand");

        verifier.verifyTrace(Expectations.async(
                Expectations.event("HYSTRIX_COMMAND", queue),
                Expectations.event("ASYNC", "Asynchronous Invocation"),
                Expectations.event("HYSTRIX_COMMAND", executeCmd)));

        // no more traces
        verifier.verifyTraceCount(0);
    }

    @Test
    public void testAsyncCall() throws Exception {
        String name = "Pinpoint";

        SayHelloCommand cmd = new SayHelloCommand(name);
        Future<String> future = cmd.queue();
        String result = future.get(100, TimeUnit.MILLISECONDS);

        Assert.assertEquals("Hello Pinpoint!", result);

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.printCache();

        Method queue    = HystrixCommand.class.getMethod("queue");
        Method executeCmd = HystrixCommand.class.getDeclaredMethod("executeCommand");

        verifier.verifyTrace(Expectations.async(
                Expectations.event("HYSTRIX_COMMAND", queue),
                Expectations.event("ASYNC", "Asynchronous Invocation"),
                Expectations.event("HYSTRIX_COMMAND", executeCmd)));

        // no more traces
        verifier.verifyTraceCount(0);
    }
}
