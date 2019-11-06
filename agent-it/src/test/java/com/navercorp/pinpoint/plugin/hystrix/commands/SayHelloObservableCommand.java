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
import com.navercorp.test.pinpoint.plugin.hystrix.repository.HelloObservableRepository;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.hystrix.HystrixObservableCommand;
import rx.Observable;

/**
 * @author HyunGil Jeong
 */
public class SayHelloObservableCommand extends HystrixObservableCommand<String> {

    private static final HelloObservableRepository helloObservableRepository = new HelloObservableRepository();

    private final HystrixTestHelper.ExecutionOption executionOption;
    private final String name;
    private final Exception expectedException;
    private final long delayMs;

    private SayHelloObservableCommand(HystrixTestHelper.ExecutionOption executionOption, String commandGroup, String name, HystrixCommandProperties.Setter setter) {
        this(executionOption, commandGroup, name, setter, 0, null);
    }

    private SayHelloObservableCommand(HystrixTestHelper.ExecutionOption executionOption, String commandGroup, String name, HystrixCommandProperties.Setter setter, long delayMs) {
        this(executionOption, commandGroup, name, setter, delayMs, null);
    }

    private SayHelloObservableCommand(HystrixTestHelper.ExecutionOption executionOption, String commandGroup, String name, HystrixCommandProperties.Setter setter, long delayMs, Exception expectedException) {
        super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey(commandGroup)).andCommandPropertiesDefaults(setter));
        this.executionOption = executionOption;
        this.name = name;
        this.expectedException = expectedException;
        this.delayMs = delayMs;
    }

    public static SayHelloObservableCommand create(String commandGroup, String name) {
        return new SayHelloObservableCommand(HystrixTestHelper.ExecutionOption.NORMAL, commandGroup, name, HystrixCommandProperties.Setter());
    }

    public static SayHelloObservableCommand createForException(String commandGroup, String name, Exception expectedException) {
        return new SayHelloObservableCommand(HystrixTestHelper.ExecutionOption.EXCEPTION, commandGroup, name, HystrixCommandProperties.Setter(), 0, expectedException);
    }

    public static SayHelloObservableCommand createForTimeout(String commandGroup, String name, int timeoutMs) {
        return new SayHelloObservableCommand(HystrixTestHelper.ExecutionOption.TIMEOUT, commandGroup, name, HystrixCommandProperties.Setter().withExecutionTimeoutInMilliseconds(timeoutMs), timeoutMs * 2);
    }

    public static SayHelloObservableCommand createForShortCircuit(String commandGroup, String name) {
        return new SayHelloObservableCommand(HystrixTestHelper.ExecutionOption.SHORT_CIRCUIT, commandGroup, name, HystrixCommandProperties.Setter().withCircuitBreakerForceOpen(true));
    }

    @Override
    protected Observable<String> construct() {
        Observable<String> message;
        switch (executionOption) {
            case EXCEPTION:
                message = helloObservableRepository.hello(name, expectedException);
                break;
            case TIMEOUT:
                message = helloObservableRepository.hello(name, delayMs);
                break;
            case NORMAL:
            case SHORT_CIRCUIT:
            default:
                message = helloObservableRepository.hello(name);
                break;
        }
        return message;
    }

    @Override
    protected Observable<String> resumeWithFallback() {
        return Observable.just(HystrixTestHelper.fallbackHello(name));
    }
}
