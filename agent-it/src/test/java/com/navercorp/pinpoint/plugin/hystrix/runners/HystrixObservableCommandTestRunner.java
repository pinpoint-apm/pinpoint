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
import com.navercorp.pinpoint.plugin.hystrix.commands.SayHelloObservableCommand;
import com.netflix.hystrix.HystrixObservableCommand;
import org.junit.Assert;

/**
 * @author HyunGil Jeong
 */
public class HystrixObservableCommandTestRunner {

    private final String commandGroup;

    public HystrixObservableCommandTestRunner(String commandGroup) {
        this.commandGroup = commandGroup;
    }

    public void observe() throws Exception {
        final String name = "Pinpoint";
        final String expectedMessage = HystrixTestHelper.sayHello(name);
        HystrixObservableCommand<String> helloObservableCommand = SayHelloObservableCommand.create(commandGroup, name);
        String actualMessage = helloObservableCommand.observe()
                .toBlocking()
                .single();
        Assert.assertEquals(expectedMessage, actualMessage);

        HystrixTestHelper.waitForSpanDataFlush();
    }

    public void observeWithException(Exception expectedException) throws Exception {
        final String name = "Pinpoint";
        final String expectedFallbackMessage = HystrixTestHelper.fallbackHello(name);
        HystrixObservableCommand<String> helloObservableCommand = SayHelloObservableCommand.createForException(commandGroup, name, expectedException);
        String actualMessage = helloObservableCommand.observe()
                .toBlocking()
                .single();
        Assert.assertEquals(expectedFallbackMessage, actualMessage);

        HystrixTestHelper.waitForSpanDataFlush();
    }

    public void observeWithTimeout() throws Exception {
        // reducing this too much will make the actual task to not be scheduled at all
        final int timeoutMs = 100;
        final String name = "Pinpoint";
        final String expectedFallbackMessage = HystrixTestHelper.fallbackHello(name);
        HystrixObservableCommand<String> helloObservableCommand = SayHelloObservableCommand.createForTimeout(commandGroup, name, timeoutMs);
        String actualMessage = helloObservableCommand.observe()
                .toBlocking()
                .single();
        Assert.assertEquals(expectedFallbackMessage, actualMessage);

        HystrixTestHelper.waitForSpanDataFlush(timeoutMs);
    }

    public void observeWithShortCircuit() throws Exception {
        final String name = "Pinpoint";
        final String expectedFallbackMessage = HystrixTestHelper.fallbackHello(name);
        HystrixObservableCommand<String> helloObservableCommand = SayHelloObservableCommand.createForShortCircuit(commandGroup, name);
        String actualMessage = helloObservableCommand.observe()
                .toBlocking()
                .single();
        Assert.assertEquals(expectedFallbackMessage, actualMessage);

        HystrixTestHelper.waitForSpanDataFlush();
    }

    public void toObservable() throws Exception {
        final String name = "Pinpoint";
        final String expectedMessage = HystrixTestHelper.sayHello(name);
        HystrixObservableCommand<String> helloObservableCommand = SayHelloObservableCommand.create(commandGroup, name);
        String actualMessage = helloObservableCommand.toObservable()
                .toBlocking()
                .single();
        Assert.assertEquals(expectedMessage, actualMessage);

        HystrixTestHelper.waitForSpanDataFlush();
    }

    public void toObservableWithException(Exception expectedException) throws Exception {
        final String name = "Pinpoint";
        final String expectedFallbackMessage = HystrixTestHelper.fallbackHello(name);
        HystrixObservableCommand<String> helloObservableCommand = SayHelloObservableCommand.createForException(commandGroup, name, expectedException);
        String actualMessage = helloObservableCommand.toObservable()
                .toBlocking()
                .single();
        Assert.assertEquals(expectedFallbackMessage, actualMessage);

        HystrixTestHelper.waitForSpanDataFlush();
    }

    public void toObservableWithTimeout() throws Exception {
        // reducing this too much will make the actual task to not be scheduled at all
        final int timeoutMs = 100;
        final String name = "Pinpoint";
        final String expectedFallbackMessage = HystrixTestHelper.fallbackHello(name);
        HystrixObservableCommand<String> helloObservableCommand = SayHelloObservableCommand.createForTimeout(commandGroup, name, timeoutMs);
        String actualMessage = helloObservableCommand.toObservable()
                .toBlocking()
                .single();
        Assert.assertEquals(expectedFallbackMessage, actualMessage);

        HystrixTestHelper.waitForSpanDataFlush(timeoutMs);
    }

    public void toObservableWithShortCircuit() throws Exception {
        final String name = "Pinpoint";
        final String expectedFallbackMessage = HystrixTestHelper.fallbackHello(name);
        HystrixObservableCommand<String> helloObservableCommand = SayHelloObservableCommand.createForShortCircuit(commandGroup, name);
        String actualMessage = helloObservableCommand.toObservable()
                .toBlocking()
                .single();
        Assert.assertEquals(expectedFallbackMessage, actualMessage);

        HystrixTestHelper.waitForSpanDataFlush();
    }
}
