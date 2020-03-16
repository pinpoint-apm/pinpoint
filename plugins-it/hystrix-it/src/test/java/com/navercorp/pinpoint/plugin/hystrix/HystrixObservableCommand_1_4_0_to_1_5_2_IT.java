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

package com.navercorp.pinpoint.plugin.hystrix;

import com.navercorp.pinpoint.bootstrap.plugin.test.ExpectedAnnotation;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifierHolder;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.plugin.hystrix.commands.SayHelloObservableCommand;
import com.navercorp.pinpoint.test.plugin.ImportPlugin;
import com.navercorp.test.pinpoint.plugin.hystrix.repository.HelloRepository;
import com.navercorp.pinpoint.plugin.hystrix.runners.HystrixObservableCommandTestRunner;
import com.navercorp.pinpoint.pluginit.utils.AgentPath;
import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.JvmVersion;
import com.navercorp.pinpoint.test.plugin.PinpointAgent;
import com.navercorp.pinpoint.test.plugin.PinpointConfig;
import com.navercorp.pinpoint.test.plugin.PinpointPluginTestSuite;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.Method;
import java.util.concurrent.TimeoutException;

import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.annotation;
import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.event;

/**
 * Integration test for hystrix-core 1.4.0 to 1.5.2 traces the following :
 * <p>
 * <tt>HystrixObservableCommand.observe()</tt>
 * <tt>HystrixObservableCommand.toObservable()</tt>
 * <p>
 * Additionally, the cause of fallback should be traced via AbstractCommand.getFallbackOrThrowException() invocation.
 *
 * @author Jiaqi Feng
 * @author HyunGil Jeong
 */
@RunWith(PinpointPluginTestSuite.class)
@PinpointAgent(AgentPath.PATH)
// Hystrix 1.4.0 - 1.4.2 requires Java 7
@JvmVersion(7)
// rxjava, hystrix plugin enabled + custom trace method config
@PinpointConfig("hystrix/pinpoint-hystrix.config")
@ImportPlugin({"com.navercorp.pinpoint:pinpoint-hystrix-plugin", "com.navercorp.pinpoint:pinpoint-rxjava-plugin", "com.navercorp.pinpoint:pinpoint-user-plugin"})
@Dependency({"com.netflix.hystrix:hystrix-core:[1.4.0,1.5.2]","com.netflix.hystrix:hystrix-metrics-event-stream:1.1.2"})
public class HystrixObservableCommand_1_4_0_to_1_5_2_IT {

    private static final String COMMAND_GROUP = "HelloServiceGroup";

    private final HystrixObservableCommandTestRunner hystrixObservableCommandTestRunner = new HystrixObservableCommandTestRunner(COMMAND_GROUP);

    @After
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
        verifier.verifyTrace(event("HYSTRIX_COMMAND_INTERNAL", "com.netflix.hystrix.HystrixObservableCommand.getExecutionObservable()"));
        verifier.verifyTrace(event("HYSTRIX_COMMAND_INTERNAL", "com.netflix.hystrix.AbstractCommand$HystrixObservableTimeoutOperator.call(rx.Subscriber)"));

        // execution
        verifier.verifyTrace(event(ServiceType.ASYNC.getName(), "Asynchronous Invocation"));
        Method helloMethod = HelloRepository.class.getDeclaredMethod("hello", String.class);
        verifier.verifyTrace(event(ServiceType.INTERNAL_METHOD.getName(), helloMethod));

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
        verifier.verifyTrace(event("HYSTRIX_COMMAND_INTERNAL", "com.netflix.hystrix.HystrixObservableCommand.getExecutionObservable()"));
        verifier.verifyTrace(event("HYSTRIX_COMMAND_INTERNAL", "com.netflix.hystrix.AbstractCommand$HystrixObservableTimeoutOperator.call(rx.Subscriber)"));

        // execution
        verifier.verifyTrace(event(ServiceType.ASYNC.getName(), "Asynchronous Invocation"));
        Method helloMethod = HelloRepository.class.getDeclaredMethod("hello", String.class, Exception.class);
        verifier.verifyTrace(event(ServiceType.INTERNAL_METHOD.getName(), helloMethod, expectedException));

        // fallback due to exception
        ExpectedAnnotation fallbackCauseAnnotation = annotation("hystrix.command.fallback.cause", "failed");
        ExpectedAnnotation exceptionAnnotation = annotation("hystrix.command.exception", expectedException.toString());
        verifier.verifyTrace(event(
                "HYSTRIX_COMMAND_INTERNAL",
                "com.netflix.hystrix.AbstractCommand.getFallbackOrThrowException(com.netflix.hystrix.HystrixEventType, com.netflix.hystrix.exception.HystrixRuntimeException$FailureType, java.lang.String, java.lang.Exception)",
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
        verifier.verifyTrace(event("HYSTRIX_COMMAND_INTERNAL", "com.netflix.hystrix.HystrixObservableCommand.getExecutionObservable()"));
        verifier.verifyTrace(event("HYSTRIX_COMMAND_INTERNAL", "com.netflix.hystrix.AbstractCommand$HystrixObservableTimeoutOperator.call(rx.Subscriber)"));

        // fallback due to timeout
        verifier.verifyTrace(event(ServiceType.ASYNC.getName(), "Asynchronous Invocation"));
        verifier.verifyTrace(event("HYSTRIX_COMMAND_INTERNAL", "Hystrix Command Timeout tick"));
        ExpectedAnnotation fallbackCauseAnnotation = annotation("hystrix.command.fallback.cause", "timed-out");
        ExpectedAnnotation exceptionAnnotation = annotation("hystrix.command.exception", new TimeoutException().toString());
        verifier.verifyTrace(event(
                "HYSTRIX_COMMAND_INTERNAL",
                "com.netflix.hystrix.AbstractCommand.getFallbackOrThrowException(com.netflix.hystrix.HystrixEventType, com.netflix.hystrix.exception.HystrixRuntimeException$FailureType, java.lang.String, java.lang.Exception)",
                fallbackCauseAnnotation, exceptionAnnotation));
        verifier.verifyTrace(event("HYSTRIX_COMMAND_INTERNAL", "com.netflix.hystrix.HystrixObservableCommand.getFallbackObservable()"));

        // execution
        verifier.awaitTraceCount(2, 20, 3000);
        verifier.verifyTrace(event(ServiceType.ASYNC.getName(), "Asynchronous Invocation"));
        Method helloMethod = HelloRepository.class.getDeclaredMethod("hello", String.class, long.class);
        verifier.verifyTrace(event(ServiceType.INTERNAL_METHOD.getName(), helloMethod, HystrixTestHelper.INTERRUPTED_EXCEPTION_DUE_TO_TIMEOUT));

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
                "com.netflix.hystrix.AbstractCommand.getFallbackOrThrowException(com.netflix.hystrix.HystrixEventType, com.netflix.hystrix.exception.HystrixRuntimeException$FailureType, java.lang.String, java.lang.Exception)",
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
        verifier.verifyTrace(event("HYSTRIX_COMMAND_INTERNAL", "com.netflix.hystrix.HystrixObservableCommand.getExecutionObservable()"));
        verifier.verifyTrace(event("HYSTRIX_COMMAND_INTERNAL", "com.netflix.hystrix.AbstractCommand$HystrixObservableTimeoutOperator.call(rx.Subscriber)"));

        // execution
        verifier.verifyTrace(event(ServiceType.ASYNC.getName(), "Asynchronous Invocation"));
        Method helloMethod = HelloRepository.class.getDeclaredMethod("hello", String.class);
        verifier.verifyTrace(event(ServiceType.INTERNAL_METHOD.getName(), helloMethod));

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
        verifier.verifyTrace(event("HYSTRIX_COMMAND_INTERNAL", "com.netflix.hystrix.HystrixObservableCommand.getExecutionObservable()"));
        verifier.verifyTrace(event("HYSTRIX_COMMAND_INTERNAL", "com.netflix.hystrix.AbstractCommand$HystrixObservableTimeoutOperator.call(rx.Subscriber)"));

        // execution
        verifier.verifyTrace(event(ServiceType.ASYNC.getName(), "Asynchronous Invocation"));
        Method helloMethod = HelloRepository.class.getDeclaredMethod("hello", String.class, Exception.class);
        verifier.verifyTrace(event(ServiceType.INTERNAL_METHOD.getName(), helloMethod, expectedException));

        // fallback due to exception
        ExpectedAnnotation fallbackCauseAnnotation = annotation("hystrix.command.fallback.cause", "failed");
        ExpectedAnnotation exceptionAnnotation = annotation("hystrix.command.exception", expectedException.toString());
        verifier.verifyTrace(event(
                "HYSTRIX_COMMAND_INTERNAL",
                "com.netflix.hystrix.AbstractCommand.getFallbackOrThrowException(com.netflix.hystrix.HystrixEventType, com.netflix.hystrix.exception.HystrixRuntimeException$FailureType, java.lang.String, java.lang.Exception)",
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
        verifier.verifyTrace(event("HYSTRIX_COMMAND_INTERNAL", "com.netflix.hystrix.HystrixObservableCommand.getExecutionObservable()"));
        verifier.verifyTrace(event("HYSTRIX_COMMAND_INTERNAL", "com.netflix.hystrix.AbstractCommand$HystrixObservableTimeoutOperator.call(rx.Subscriber)"));

        // fallback due to timeout
        verifier.verifyTrace(event(ServiceType.ASYNC.getName(), "Asynchronous Invocation"));
        verifier.verifyTrace(event("HYSTRIX_COMMAND_INTERNAL", "Hystrix Command Timeout tick"));
        ExpectedAnnotation fallbackCauseAnnotation = annotation("hystrix.command.fallback.cause", "timed-out");
        ExpectedAnnotation exceptionAnnotation = annotation("hystrix.command.exception", new TimeoutException().toString());
        verifier.verifyTrace(event(
                "HYSTRIX_COMMAND_INTERNAL",
                "com.netflix.hystrix.AbstractCommand.getFallbackOrThrowException(com.netflix.hystrix.HystrixEventType, com.netflix.hystrix.exception.HystrixRuntimeException$FailureType, java.lang.String, java.lang.Exception)",
                fallbackCauseAnnotation, exceptionAnnotation));
        verifier.verifyTrace(event("HYSTRIX_COMMAND_INTERNAL", "com.netflix.hystrix.HystrixObservableCommand.getFallbackObservable()"));

        // execution
        verifier.verifyTrace(event(ServiceType.ASYNC.getName(), "Asynchronous Invocation"));
        Method helloMethod = HelloRepository.class.getDeclaredMethod("hello", String.class, long.class);
        verifier.verifyTrace(event(ServiceType.INTERNAL_METHOD.getName(), helloMethod, HystrixTestHelper.INTERRUPTED_EXCEPTION_DUE_TO_TIMEOUT));

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
                "com.netflix.hystrix.AbstractCommand.getFallbackOrThrowException(com.netflix.hystrix.HystrixEventType, com.netflix.hystrix.exception.HystrixRuntimeException$FailureType, java.lang.String, java.lang.Exception)",
                fallbackCauseAnnotation, exceptionAnnotation));
        verifier.verifyTrace(event("HYSTRIX_COMMAND_INTERNAL", "com.netflix.hystrix.HystrixObservableCommand.getFallbackObservable()"));

        verifier.verifyTraceCount(0);
    }
}
