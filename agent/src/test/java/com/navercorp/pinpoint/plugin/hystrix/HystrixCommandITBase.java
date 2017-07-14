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

import com.navercorp.pinpoint.plugin.hystrix.commands.InvokeSayHelloCommand;
import com.navercorp.pinpoint.plugin.hystrix.commands.SayHelloCommand;
import com.navercorp.pinpoint.plugin.hystrix.commands.ThrowExceptionCommand;
import com.navercorp.pinpoint.plugin.hystrix.commands.ThrowExceptionCommandWithFallback;
import com.netflix.hystrix.Hystrix;
import org.junit.After;
import org.junit.Assert;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author HyunGil Jeong
 */
public class HystrixCommandITBase {

    private static final long SPANEVENT_GENERATION_WAIT_TIME = 1000L;

    @After
    public void teardown() {
        Hystrix.reset(60, TimeUnit.SECONDS);
    }

    protected String executeSayHelloCommand(String name) {
        SayHelloCommand cmd = new SayHelloCommand(name);
        String result = cmd.execute();

        /* when get the result from HystrixCommand, the com.netflix.hystrix.HystrixCommand$1.call()
         * intercepted may not done yet, so wait 2 sec for the spanEvent to be collected.
         * More info ref to HystrixObservableCallInterceptor.
         */
        try {
            Thread.sleep(SPANEVENT_GENERATION_WAIT_TIME);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return result;
    }

    protected String queueAndGetSayHelloCommand(String name) throws InterruptedException, ExecutionException, TimeoutException {
        SayHelloCommand cmd = new SayHelloCommand(name);
        Future<String> future = cmd.queue();
        String result = future.get(6000, TimeUnit.MILLISECONDS);

        Assert.assertEquals("Hello " + name + "!", result);

        // see comments above in executeSayHelloCommand()
        try {
            Thread.sleep(SPANEVENT_GENERATION_WAIT_TIME);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return result;
    }

    protected void executeThrowExceptionCommand(Exception expectedException) {
        ThrowExceptionCommand cmd = new ThrowExceptionCommand(expectedException);
        try {
            cmd.execute();
            Assert.fail("should not reach here as no fallback is available");
        } catch (Exception e) {
        }

        // see comments above in executeSayHelloCommand()
        try {
            Thread.sleep(SPANEVENT_GENERATION_WAIT_TIME);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    protected String executeThrowExceptionWithFallbackCommand(Exception expectedException, String fallbackMessage) {
        ThrowExceptionCommandWithFallback cmd = new ThrowExceptionCommandWithFallback(expectedException, fallbackMessage);
        String result = cmd.execute();

        Assert.assertEquals(fallbackMessage, result);

        // see comments above in executeSayHelloCommand()
        try {
            Thread.sleep(SPANEVENT_GENERATION_WAIT_TIME);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return result;
    }

    protected String executeInvokeSayHelloCommand(String name) {
        InvokeSayHelloCommand cmd = new InvokeSayHelloCommand(name);
        String result = cmd.execute();

        Assert.assertEquals("Hello " + name + "!", result);

        // see comments above in executeSayHelloCommand()
        try {
            Thread.sleep(SPANEVENT_GENERATION_WAIT_TIME);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return result;
    }
}
