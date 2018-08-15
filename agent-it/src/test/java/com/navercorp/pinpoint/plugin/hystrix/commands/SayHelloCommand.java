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

package com.navercorp.pinpoint.plugin.hystrix.commands;

import com.navercorp.pinpoint.plugin.hystrix.HystrixTestHelper;
import com.navercorp.test.pinpoint.plugin.hystrix.repository.HelloRepository;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandProperties;

/**
 * @author Jiaqi Feng
 * @author HyunGil Jeong
 */
public class SayHelloCommand extends HystrixCommand<String> {

    private static final HelloRepository helloRepository = new HelloRepository();

    private final HystrixTestHelper.ExecutionOption executionOption;
    private final String name;
    private final Exception expectedException;
    private final long delayMs;

    private SayHelloCommand(HystrixTestHelper.ExecutionOption executionOption, String commandGroup, String name, HystrixCommandProperties.Setter setter) {
        this(executionOption, commandGroup, name, setter, 0, null);
    }

    private SayHelloCommand(HystrixTestHelper.ExecutionOption executionOption, String commandGroup, String name, HystrixCommandProperties.Setter setter, long delayMs) {
        this(executionOption, commandGroup, name, setter, delayMs, null);
    }

    private SayHelloCommand(HystrixTestHelper.ExecutionOption executionOption, String commandGroup, String name, HystrixCommandProperties.Setter setter, long delayMs, Exception expectedException) {
        super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey(commandGroup)).andCommandPropertiesDefaults(setter));
        this.executionOption = executionOption;
        this.name = name;
        this.expectedException = expectedException;
        this.delayMs = delayMs;
    }

    public static SayHelloCommand create(String commandGroup, String name) {
        return new SayHelloCommand(HystrixTestHelper.ExecutionOption.NORMAL, commandGroup, name, HystrixCommandProperties.Setter());
    }

    public static SayHelloCommand createForException(String commandGroup, String name, Exception expectedException) {
        return new SayHelloCommand(HystrixTestHelper.ExecutionOption.EXCEPTION, commandGroup, name, HystrixCommandProperties.Setter(), 0, expectedException);
    }

    public static SayHelloCommand createForTimeout(String commandGroup, String name, int timeoutMs) {
        return new SayHelloCommand(HystrixTestHelper.ExecutionOption.TIMEOUT, commandGroup, name, HystrixCommandProperties.Setter().withExecutionTimeoutInMilliseconds(timeoutMs), timeoutMs * 2);
    }

    public static SayHelloCommand createForShortCircuit(String commandGroup, String name) {
        return new SayHelloCommand(HystrixTestHelper.ExecutionOption.TIMEOUT, commandGroup, name, HystrixCommandProperties.Setter().withCircuitBreakerForceOpen(true));
    }

    @Override
    protected String run() throws Exception {
        String message;
        switch (executionOption) {
            case EXCEPTION:
                message = helloRepository.hello(name, expectedException);
                break;
            case TIMEOUT:
                message = helloRepository.hello(name, delayMs);
                break;
            case NORMAL:
            case SHORT_CIRCUIT:
            default:
                message = helloRepository.hello(name);
                break;
        }
        return message;
    }

    @Override
    protected String getFallback() {
        return HystrixTestHelper.fallbackHello(name);
    }
}
