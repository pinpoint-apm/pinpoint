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

package com.navercorp.pinpoint.plugin.hystrix.runners;

import com.navercorp.pinpoint.plugin.hystrix.HystrixTestHelper;
import com.navercorp.pinpoint.plugin.hystrix.commands.SayHelloCommand;
import com.netflix.hystrix.HystrixCommand;
import org.junit.Assert;
import rx.functions.Action1;

import java.util.List;

/**
 * @author HyunGil Jeong
 */
public class HystrixCommandTestRunner {

    private final String commandGroup;

    public HystrixCommandTestRunner(String commandGroup) {
        this.commandGroup = commandGroup;
    }

    public void execute() throws Exception {
        final String name = "Pinpoint";
        final String expectedMessage = HystrixTestHelper.sayHello(name);
        HystrixCommand<String> helloCommand = SayHelloCommand.create(commandGroup, name);
        String actualMessage = helloCommand.execute();
        Assert.assertEquals(expectedMessage, actualMessage);

        HystrixTestHelper.waitForSpanDataFlush();
    }

    public void executeWithException(Exception expectedException) throws Exception {
        final String name = "Pinpoint";
        final String expectedFallbackMessage = HystrixTestHelper.fallbackHello(name);
        HystrixCommand<String> helloCommand = SayHelloCommand.createForException(commandGroup, name, expectedException);
        String actualMessage = helloCommand.execute();
        Assert.assertEquals(expectedFallbackMessage, actualMessage);

        HystrixTestHelper.waitForSpanDataFlush();
    }

    public void executeWithTimeout() throws Exception {
        // reducing this too much will make the actual task to not be scheduled at all
        final int timeoutMs = 100;
        final String name = "Pinpoint";
        final String expectedFallbackMessage = HystrixTestHelper.fallbackHello(name);
        HystrixCommand<String> helloCommand = SayHelloCommand.createForTimeout(commandGroup, name, timeoutMs);
        String actualMessage = helloCommand.execute();
        Assert.assertEquals(expectedFallbackMessage, actualMessage);

        HystrixTestHelper.waitForSpanDataFlush(timeoutMs);
    }

    public void executeWithShortCircuit() throws Exception {
        final String name = "Pinpoint";
        final String expectedFallbackMessage = HystrixTestHelper.fallbackHello(name);
        HystrixCommand<String> helloCommand = SayHelloCommand.createForShortCircuit(commandGroup, name);
        String actualMessage = helloCommand.execute();
        Assert.assertEquals(expectedFallbackMessage, actualMessage);

        HystrixTestHelper.waitForSpanDataFlush();
    }

    public void observe() throws Exception {
        final String name = "Pinpoint";
        final String expectedMessage = HystrixTestHelper.sayHello(name);
        HystrixCommand<String> helloCommand = SayHelloCommand.create(commandGroup, name);
        List<String> actualMessages = helloCommand.observe().toList().toBlocking().single();
        Assert.assertTrue(actualMessages.size() == 1);
        Assert.assertEquals(expectedMessage, actualMessages.get(0));

        HystrixTestHelper.waitForSpanDataFlush();
    }

    public void toObservable() throws Exception {
        final String name = "Pinpoint";
        final String expectedMessage = HystrixTestHelper.sayHello(name);
        HystrixCommand<String> helloCommand = SayHelloCommand.create(commandGroup, name);
        List<String> actualMessages = helloCommand.toObservable().toList().toBlocking().single();
        Assert.assertTrue(actualMessages.size() == 1);
        Assert.assertEquals(expectedMessage, actualMessages.get(0));

        HystrixTestHelper.waitForSpanDataFlush();
    }
}
