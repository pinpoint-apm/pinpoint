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

package com.navercorp.pinpoint.it.plugin.hystrix;

import com.navercorp.pinpoint.bootstrap.plugin.test.ExpectedAnnotation;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifierHolder;
import com.navercorp.pinpoint.it.plugin.hystrix.commands.SayHelloObservableCommand;
import com.navercorp.pinpoint.it.plugin.hystrix.runners.HystrixObservableCommandTestRunner;
import com.navercorp.pinpoint.it.plugin.utils.AgentPath;
import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.PinpointAgent;
import com.navercorp.pinpoint.test.plugin.PinpointConfig;
import com.navercorp.pinpoint.test.plugin.PluginTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import test.repository.HelloRepository;

import java.lang.reflect.Method;
import java.util.concurrent.TimeoutException;

import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.annotation;
import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.event;

/**
 * Integration test for hystrix-core 1.5.3+ traces the following :
 * <p>
 * <tt>HystrixObservableCommand.observe()</tt>
 * <tt>HystrixObservableCommand.toObservable()</tt>
 * <p>
 * Additionally, the cause of fallback should be traced via AbstractCommand.getFallbackOrThrowException() invocation.
 *
 * @author Jiaqi Feng
 * @author HyunGil Jeong
 */
@PluginTest
@PinpointAgent(AgentPath.PATH)
// rxjava, hystrix plugin enabled + custom trace method config
@PinpointConfig("hystrix/pinpoint-hystrix.config")
@Dependency({"com.netflix.hystrix:hystrix-core:[1.5.3,)","com.netflix.hystrix:hystrix-metrics-event-stream:1.1.2"})
public class HystrixObservableCommand_1_5_3_to_1_5_x_IT {

    private static final String COMMAND_GROUP = "HelloServiceGroup";
    private static final String ASYNC = "ASYNC";
    private static final String INTERNAL_METHOD = "INTERNAL_METHOD";

    private final HystrixObservableCommandTestRunner hystrixObservableCommandTestRunner = new HystrixObservableCommandTestRunner(COMMAND_GROUP);

    @AfterEach
    public void cleanUp() {
        HystrixTestHelper.reset();
    }

    @Test
    public void hystrixObservableCommand_observe() throws Exception {
        hystrixObservableCommandTestRunner.observe();

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.printCache();
        verifier.ignoreServiceType("RX_JAVA", "RX_JAVA_INTERNAL");

        HystrixTestHelper.verifyHystrixMetricsInitialization(verifier, SayHelloObservableCommand.class.getSimpleName(), COMMAND_GROUP);

        verifier.verifyTrace(event("HYSTRIX_COMMAND", "com.netflix.hystrix.AbstractCommand.observe()", annotation("hystrix.command", SayHelloObservableCommand.class.getSimpleName())));
        verifier.verifyTrace(event("HYSTRIX_COMMAND_INTERNAL", "com.netflix.hystrix.AbstractCommand$HystrixObservableTimeoutOperator.call(rx.Subscriber)"));
        verifier.verifyTrace(event("HYSTRIX_COMMAND_INTERNAL", "com.netflix.hystrix.HystrixObservableCommand.getExecutionObservable()"));

        // execution
        verifier.verifyTrace(event(ASYNC, "Asynchronous Invocation"));
        Method helloMethod = HelloRepository.class.getDeclaredMethod("hello", String.class);
        verifier.verifyTrace(event(INTERNAL_METHOD, helloMethod));

        verifier.verifyTraceCount(0);
    }

    @Test
    public void hystrixObservableCommand_observe_exception() throws Exception {
        final Exception expectedException = new RuntimeException("expected");
        hystrixObservableCommandTestRunner.observeWithException(expectedException);

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.printCache();
        verifier.ignoreServiceType("RX_JAVA", "RX_JAVA_INTERNAL");

        HystrixTestHelper.verifyHystrixMetricsInitialization(verifier, SayHelloObservableCommand.class.getSimpleName(), COMMAND_GROUP);

        verifier.verifyTrace(event("HYSTRIX_COMMAND", "com.netflix.hystrix.AbstractCommand.observe()", annotation("hystrix.command", SayHelloObservableCommand.class.getSimpleName())));
        verifier.verifyTrace(event("HYSTRIX_COMMAND_INTERNAL", "com.netflix.hystrix.AbstractCommand$HystrixObservableTimeoutOperator.call(rx.Subscriber)"));
        verifier.verifyTrace(event("HYSTRIX_COMMAND_INTERNAL", "com.netflix.hystrix.HystrixObservableCommand.getExecutionObservable()"));

        // execution
        verifier.verifyTrace(event(ASYNC, "Asynchronous Invocation"));
        Method helloMethod = HelloRepository.class.getDeclaredMethod("hello", String.class, Exception.class);
        verifier.verifyTrace(event(INTERNAL_METHOD, helloMethod, expectedException));

        // fallback due to exception
        ExpectedAnnotation fallbackCauseAnnotation = annotation("hystrix.command.fallback.cause", "failed");
        ExpectedAnnotation exceptionAnnotation = annotation("hystrix.command.exception", expectedException.toString());
        verifier.verifyTrace(event(
                "HYSTRIX_COMMAND_INTERNAL",
                "com.netflix.hystrix.AbstractCommand.getFallbackOrThrowException(com.netflix.hystrix.AbstractCommand, com.netflix.hystrix.HystrixEventType, com.netflix.hystrix.exception.HystrixRuntimeException$FailureType, java.lang.String, java.lang.Exception)",
                fallbackCauseAnnotation, exceptionAnnotation));
        verifier.verifyTrace(event("HYSTRIX_COMMAND_INTERNAL", "com.netflix.hystrix.HystrixObservableCommand.getFallbackObservable()"));

        verifier.verifyTraceCount(0);
    }

    @Test
    public void hystrixObservableCommand_observe_timeout() throws Exception {
        hystrixObservableCommandTestRunner.observeWithTimeout();

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.printCache();
        verifier.ignoreServiceType("RX_JAVA", "RX_JAVA_INTERNAL");

        HystrixTestHelper.verifyHystrixMetricsInitialization(verifier, SayHelloObservableCommand.class.getSimpleName(), COMMAND_GROUP);

        verifier.verifyTrace(event("HYSTRIX_COMMAND", "com.netflix.hystrix.AbstractCommand.observe()", annotation("hystrix.command", SayHelloObservableCommand.class.getSimpleName())));
        verifier.verifyTrace(event("HYSTRIX_COMMAND_INTERNAL", "com.netflix.hystrix.AbstractCommand$HystrixObservableTimeoutOperator.call(rx.Subscriber)"));
        verifier.verifyTrace(event("HYSTRIX_COMMAND_INTERNAL", "com.netflix.hystrix.HystrixObservableCommand.getExecutionObservable()"));

        // fallback due to timeout
        verifier.verifyTrace(event(ASYNC, "Asynchronous Invocation"));
        verifier.verifyTrace(event("HYSTRIX_COMMAND_INTERNAL", "Hystrix Command Timeout tick"));
        ExpectedAnnotation fallbackCauseAnnotation = annotation("hystrix.command.fallback.cause", "timed-out");
        ExpectedAnnotation exceptionAnnotation = annotation("hystrix.command.exception", new TimeoutException().toString());
        verifier.verifyTrace(event(
                "HYSTRIX_COMMAND_INTERNAL",
                "com.netflix.hystrix.AbstractCommand.getFallbackOrThrowException(com.netflix.hystrix.AbstractCommand, com.netflix.hystrix.HystrixEventType, com.netflix.hystrix.exception.HystrixRuntimeException$FailureType, java.lang.String, java.lang.Exception)",
                fallbackCauseAnnotation, exceptionAnnotation));
        verifier.verifyTrace(event("HYSTRIX_COMMAND_INTERNAL", "com.netflix.hystrix.HystrixObservableCommand.getFallbackObservable()"));

        // execution
        verifier.awaitTraceCount(2, 20, 3000);
        verifier.verifyTrace(event(ASYNC, "Asynchronous Invocation"));
        Method helloMethod = HelloRepository.class.getDeclaredMethod("hello", String.class, long.class);
        verifier.verifyTrace(event(INTERNAL_METHOD, helloMethod, HystrixTestHelper.INTERRUPTED_EXCEPTION_DUE_TO_TIMEOUT));

        verifier.verifyTraceCount(0);
    }

    @Test
    public void hystrixObservableCommand_observe_shortCircuit() throws Exception {
        hystrixObservableCommandTestRunner.observeWithShortCircuit();

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.printCache();
        verifier.ignoreServiceType("RX_JAVA", "RX_JAVA_INTERNAL");

        HystrixTestHelper.verifyHystrixMetricsInitialization(verifier, SayHelloObservableCommand.class.getSimpleName(), COMMAND_GROUP);

        verifier.verifyTrace(event("HYSTRIX_COMMAND", "com.netflix.hystrix.AbstractCommand.observe()", annotation("hystrix.command", SayHelloObservableCommand.class.getSimpleName())));

        // fallback due to short circuit
        ExpectedAnnotation fallbackCauseAnnotation = annotation("hystrix.command.fallback.cause", "short-circuited");
        ExpectedAnnotation exceptionAnnotation = annotation("hystrix.command.exception", HystrixTestHelper.SHORT_CIRCUIT_EXCEPTION.toString());
        verifier.verifyTrace(event(
                "HYSTRIX_COMMAND_INTERNAL",
                "com.netflix.hystrix.AbstractCommand.getFallbackOrThrowException(com.netflix.hystrix.AbstractCommand, com.netflix.hystrix.HystrixEventType, com.netflix.hystrix.exception.HystrixRuntimeException$FailureType, java.lang.String, java.lang.Exception)",
                fallbackCauseAnnotation, exceptionAnnotation));
        verifier.verifyTrace(event("HYSTRIX_COMMAND_INTERNAL", "com.netflix.hystrix.HystrixObservableCommand.getFallbackObservable()"));

        verifier.verifyTraceCount(0);
    }

    @Test
    public void hystrixObservableCommand_toObservable() throws Exception {
        hystrixObservableCommandTestRunner.toObservable();

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.printCache();
        verifier.ignoreServiceType("RX_JAVA", "RX_JAVA_INTERNAL");

        HystrixTestHelper.verifyHystrixMetricsInitialization(verifier, SayHelloObservableCommand.class.getSimpleName(), COMMAND_GROUP);

        verifier.verifyTrace(event("HYSTRIX_COMMAND", "com.netflix.hystrix.AbstractCommand.toObservable()", annotation("hystrix.command", SayHelloObservableCommand.class.getSimpleName())));
        verifier.verifyTrace(event("HYSTRIX_COMMAND_INTERNAL", "com.netflix.hystrix.AbstractCommand$HystrixObservableTimeoutOperator.call(rx.Subscriber)"));
        verifier.verifyTrace(event("HYSTRIX_COMMAND_INTERNAL", "com.netflix.hystrix.HystrixObservableCommand.getExecutionObservable()"));

        // execution
        verifier.verifyTrace(event(ASYNC, "Asynchronous Invocation"));
        Method helloMethod = HelloRepository.class.getDeclaredMethod("hello", String.class);
        verifier.verifyTrace(event(INTERNAL_METHOD, helloMethod));

        verifier.verifyTraceCount(0);
    }

    @Test
    public void hystrixObservableCommand_toObservable_exception() throws Exception {
        final Exception expectedException = new RuntimeException("expected");
        hystrixObservableCommandTestRunner.toObservableWithException(expectedException);

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.printCache();
        verifier.ignoreServiceType("RX_JAVA", "RX_JAVA_INTERNAL");

        HystrixTestHelper.verifyHystrixMetricsInitialization(verifier, SayHelloObservableCommand.class.getSimpleName(), COMMAND_GROUP);

        verifier.verifyTrace(event("HYSTRIX_COMMAND", "com.netflix.hystrix.AbstractCommand.toObservable()", annotation("hystrix.command", SayHelloObservableCommand.class.getSimpleName())));
        verifier.verifyTrace(event("HYSTRIX_COMMAND_INTERNAL", "com.netflix.hystrix.AbstractCommand$HystrixObservableTimeoutOperator.call(rx.Subscriber)"));
        verifier.verifyTrace(event("HYSTRIX_COMMAND_INTERNAL", "com.netflix.hystrix.HystrixObservableCommand.getExecutionObservable()"));

        // execution
        verifier.verifyTrace(event(ASYNC, "Asynchronous Invocation"));
        Method helloMethod = HelloRepository.class.getDeclaredMethod("hello", String.class, Exception.class);
        verifier.verifyTrace(event(INTERNAL_METHOD, helloMethod, expectedException));

        // fallback due to exception
        ExpectedAnnotation fallbackCauseAnnotation = annotation("hystrix.command.fallback.cause", "failed");
        ExpectedAnnotation exceptionAnnotation = annotation("hystrix.command.exception", expectedException.toString());
        verifier.verifyTrace(event(
                "HYSTRIX_COMMAND_INTERNAL",
                "com.netflix.hystrix.AbstractCommand.getFallbackOrThrowException(com.netflix.hystrix.AbstractCommand, com.netflix.hystrix.HystrixEventType, com.netflix.hystrix.exception.HystrixRuntimeException$FailureType, java.lang.String, java.lang.Exception)",
                fallbackCauseAnnotation, exceptionAnnotation));
        verifier.verifyTrace(event("HYSTRIX_COMMAND_INTERNAL", "com.netflix.hystrix.HystrixObservableCommand.getFallbackObservable()"));

        verifier.verifyTraceCount(0);
    }

    @Test
    public void hystrixObservableCommand_toObservable_timeout() throws Exception {
        hystrixObservableCommandTestRunner.toObservableWithTimeout();

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.printCache();
        verifier.ignoreServiceType("RX_JAVA", "RX_JAVA_INTERNAL");

        HystrixTestHelper.verifyHystrixMetricsInitialization(verifier, SayHelloObservableCommand.class.getSimpleName(), COMMAND_GROUP);

        verifier.verifyTrace(event("HYSTRIX_COMMAND", "com.netflix.hystrix.AbstractCommand.toObservable()", annotation("hystrix.command", SayHelloObservableCommand.class.getSimpleName())));
        verifier.verifyTrace(event("HYSTRIX_COMMAND_INTERNAL", "com.netflix.hystrix.AbstractCommand$HystrixObservableTimeoutOperator.call(rx.Subscriber)"));
        verifier.verifyTrace(event("HYSTRIX_COMMAND_INTERNAL", "com.netflix.hystrix.HystrixObservableCommand.getExecutionObservable()"));

        // fallback due to timeout
        verifier.verifyTrace(event(ASYNC, "Asynchronous Invocation"));
        verifier.verifyTrace(event("HYSTRIX_COMMAND_INTERNAL", "Hystrix Command Timeout tick"));
        ExpectedAnnotation fallbackCauseAnnotation = annotation("hystrix.command.fallback.cause", "timed-out");
        ExpectedAnnotation exceptionAnnotation = annotation("hystrix.command.exception", new TimeoutException().toString());
        verifier.verifyTrace(event(
                "HYSTRIX_COMMAND_INTERNAL",
                "com.netflix.hystrix.AbstractCommand.getFallbackOrThrowException(com.netflix.hystrix.AbstractCommand, com.netflix.hystrix.HystrixEventType, com.netflix.hystrix.exception.HystrixRuntimeException$FailureType, java.lang.String, java.lang.Exception)",
                fallbackCauseAnnotation, exceptionAnnotation));
        verifier.verifyTrace(event("HYSTRIX_COMMAND_INTERNAL", "com.netflix.hystrix.HystrixObservableCommand.getFallbackObservable()"));

        verifier.awaitTraceCount(2, 10, 3000);

        // execution
        verifier.verifyTrace(event(ASYNC, "Asynchronous Invocation"));
        Method helloMethod = HelloRepository.class.getDeclaredMethod("hello", String.class, long.class);
        verifier.verifyTrace(event(INTERNAL_METHOD, helloMethod, HystrixTestHelper.INTERRUPTED_EXCEPTION_DUE_TO_TIMEOUT));

        verifier.verifyTraceCount(0);
    }

    @Test
    public void hystrixObservableCommand_toObservable_shortCircuit() throws Exception {
        hystrixObservableCommandTestRunner.toObservableWithShortCircuit();

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.printCache();
        verifier.ignoreServiceType("RX_JAVA", "RX_JAVA_INTERNAL");

        HystrixTestHelper.verifyHystrixMetricsInitialization(verifier, SayHelloObservableCommand.class.getSimpleName(), COMMAND_GROUP);

        verifier.verifyTrace(event("HYSTRIX_COMMAND", "com.netflix.hystrix.AbstractCommand.toObservable()", annotation("hystrix.command", SayHelloObservableCommand.class.getSimpleName())));

        // fallback due to short circuit
        ExpectedAnnotation fallbackCauseAnnotation = annotation("hystrix.command.fallback.cause", "short-circuited");
        ExpectedAnnotation exceptionAnnotation = annotation("hystrix.command.exception", HystrixTestHelper.SHORT_CIRCUIT_EXCEPTION.toString());
        verifier.verifyTrace(event(
                "HYSTRIX_COMMAND_INTERNAL",
                "com.netflix.hystrix.AbstractCommand.getFallbackOrThrowException(com.netflix.hystrix.AbstractCommand, com.netflix.hystrix.HystrixEventType, com.netflix.hystrix.exception.HystrixRuntimeException$FailureType, java.lang.String, java.lang.Exception)",
                fallbackCauseAnnotation, exceptionAnnotation));
        verifier.verifyTrace(event("HYSTRIX_COMMAND_INTERNAL", "com.netflix.hystrix.HystrixObservableCommand.getFallbackObservable()"));

        verifier.verifyTraceCount(0);
    }
}
