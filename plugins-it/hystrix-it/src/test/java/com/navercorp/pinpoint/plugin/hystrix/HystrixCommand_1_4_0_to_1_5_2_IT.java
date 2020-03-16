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
import com.navercorp.pinpoint.plugin.hystrix.commands.SayHelloCommand;
import com.navercorp.pinpoint.test.plugin.ImportPlugin;
import com.navercorp.test.pinpoint.plugin.hystrix.repository.HelloRepository;
import com.navercorp.pinpoint.plugin.hystrix.runners.HystrixCommandTestRunner;
import com.navercorp.pinpoint.pluginit.utils.AgentPath;
import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.JvmVersion;
import com.navercorp.pinpoint.test.plugin.PinpointAgent;
import com.navercorp.pinpoint.test.plugin.PinpointConfig;
import com.navercorp.pinpoint.test.plugin.PinpointPluginTestSuite;
import com.netflix.hystrix.HystrixCommand;
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
 * <tt>HystrixCommand.execute()</tt><br/>
 * <tt>HystrixCommand.observe()</tt>
 * <tt>HystrixCommand.toObservable()</tt>
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
public class HystrixCommand_1_4_0_to_1_5_2_IT {

    private static final String COMMAND_GROUP = "HelloServiceGroup";

    private final HystrixCommandTestRunner hystrixCommandTestRunner = new HystrixCommandTestRunner(COMMAND_GROUP);

    @After
    public void cleanUp() {
        HystrixTestHelper.reset();
    }

    @Test
    public void hystrixCommand_execute() throws Exception {
        hystrixCommandTestRunner.execute();

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.printCache();
        verifier.ignoreServiceType("RX_JAVA", "RX_JAVA_INTERNAL");

        HystrixTestHelper.verifyHystrixMetricsInitialization(verifier, SayHelloCommand.class.getSimpleName(), COMMAND_GROUP);

        Method executeMethod = HystrixCommand.class.getDeclaredMethod("execute");
        verifier.verifyTrace(event("HYSTRIX_COMMAND", executeMethod, annotation("hystrix.command", SayHelloCommand.class.getSimpleName())));
        verifier.verifyTrace(event("HYSTRIX_COMMAND_INTERNAL", "com.netflix.hystrix.AbstractCommand$HystrixObservableTimeoutOperator.call(rx.Subscriber)"));
        verifier.verifyTrace(event("HYSTRIX_COMMAND_INTERNAL", "com.netflix.hystrix.strategy.concurrency.HystrixContextScheduler$HystrixContextSchedulerWorker.schedule(rx.functions.Action0)"));

        // execution
        verifier.verifyTrace(event(ServiceType.ASYNC.getName(), "Asynchronous Invocation"));
        Method getExecutionObservable = HystrixCommand.class.getDeclaredMethod("getExecutionObservable");
        verifier.verifyTrace(event("HYSTRIX_COMMAND_INTERNAL", getExecutionObservable));
        Method helloMethod = HelloRepository.class.getDeclaredMethod("hello", String.class);
        verifier.verifyTrace(event(ServiceType.INTERNAL_METHOD.getName(), helloMethod));

        verifier.verifyTraceCount(0);
    }

    @Test
    public void hystrixCommand_execute_exception() throws Exception {
        final Exception expectedException = new RuntimeException("expected");
        hystrixCommandTestRunner.executeWithException(expectedException);

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.printCache();
        verifier.ignoreServiceType("RX_JAVA", "RX_JAVA_INTERNAL");

        HystrixTestHelper.verifyHystrixMetricsInitialization(verifier, SayHelloCommand.class.getSimpleName(), COMMAND_GROUP);

        Method executeMethod = HystrixCommand.class.getDeclaredMethod("execute");
        verifier.verifyTrace(event("HYSTRIX_COMMAND", executeMethod, annotation("hystrix.command", SayHelloCommand.class.getSimpleName())));
        verifier.verifyTrace(event("HYSTRIX_COMMAND_INTERNAL", "com.netflix.hystrix.AbstractCommand$HystrixObservableTimeoutOperator.call(rx.Subscriber)"));
        verifier.verifyTrace(event("HYSTRIX_COMMAND_INTERNAL", "com.netflix.hystrix.strategy.concurrency.HystrixContextScheduler$HystrixContextSchedulerWorker.schedule(rx.functions.Action0)"));

        // execution
        verifier.verifyTrace(event(ServiceType.ASYNC.getName(), "Asynchronous Invocation"));
        Method getExecutionObservable = HystrixCommand.class.getDeclaredMethod("getExecutionObservable");
        verifier.verifyTrace(event("HYSTRIX_COMMAND_INTERNAL", getExecutionObservable));
        Method helloMethod = HelloRepository.class.getDeclaredMethod("hello", String.class, Exception.class);
        verifier.verifyTrace(event(ServiceType.INTERNAL_METHOD.getName(), helloMethod, expectedException));

        // fallback due to exception
        ExpectedAnnotation fallbackCauseAnnotation = annotation("hystrix.command.fallback.cause", "failed");
        ExpectedAnnotation exceptionAnnotation = annotation("hystrix.command.exception", expectedException.toString());
        verifier.verifyTrace(event(
                "HYSTRIX_COMMAND_INTERNAL",
                "com.netflix.hystrix.AbstractCommand.getFallbackOrThrowException(com.netflix.hystrix.HystrixEventType, com.netflix.hystrix.exception.HystrixRuntimeException$FailureType, java.lang.String, java.lang.Exception)",
                fallbackCauseAnnotation, exceptionAnnotation));
        verifier.verifyTrace(event("HYSTRIX_COMMAND_INTERNAL", "com.netflix.hystrix.HystrixCommand.getFallbackObservable()"));

        verifier.verifyTraceCount(0);
    }

    @Test
    public void hystrixCommand_execute_timeout() throws Exception {
        hystrixCommandTestRunner.executeWithTimeout();

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.printCache();
        verifier.ignoreServiceType("RX_JAVA", "RX_JAVA_INTERNAL");

        HystrixTestHelper.verifyHystrixMetricsInitialization(verifier, SayHelloCommand.class.getSimpleName(), COMMAND_GROUP);

        Method executeMethod = HystrixCommand.class.getDeclaredMethod("execute");
        verifier.verifyTrace(event("HYSTRIX_COMMAND", executeMethod, annotation("hystrix.command", SayHelloCommand.class.getSimpleName())));
        verifier.verifyTrace(event("HYSTRIX_COMMAND_INTERNAL", "com.netflix.hystrix.AbstractCommand$HystrixObservableTimeoutOperator.call(rx.Subscriber)"));
        verifier.verifyTrace(event("HYSTRIX_COMMAND_INTERNAL", "com.netflix.hystrix.strategy.concurrency.HystrixContextScheduler$HystrixContextSchedulerWorker.schedule(rx.functions.Action0)"));

        // fallback due to timeout
        verifier.verifyTrace(event(ServiceType.ASYNC.getName(), "Asynchronous Invocation"));
        verifier.verifyTrace(event("HYSTRIX_COMMAND_INTERNAL", "Hystrix Command Timeout tick"));
        ExpectedAnnotation fallbackCauseAnnotation = annotation("hystrix.command.fallback.cause", "timed-out");
        ExpectedAnnotation exceptionAnnotation = annotation("hystrix.command.exception", new TimeoutException().toString());
        verifier.verifyTrace(event(
                "HYSTRIX_COMMAND_INTERNAL",
                "com.netflix.hystrix.AbstractCommand.getFallbackOrThrowException(com.netflix.hystrix.HystrixEventType, com.netflix.hystrix.exception.HystrixRuntimeException$FailureType, java.lang.String, java.lang.Exception)",
                fallbackCauseAnnotation, exceptionAnnotation));
        verifier.verifyTrace(event("HYSTRIX_COMMAND_INTERNAL", "com.netflix.hystrix.HystrixCommand.getFallbackObservable()"));

        // execution
        verifier.awaitTraceCount(3, 20, 3000);
        verifier.verifyTrace(event(ServiceType.ASYNC.getName(), "Asynchronous Invocation"));
        Method getExecutionObservable = HystrixCommand.class.getDeclaredMethod("getExecutionObservable");
        verifier.verifyTrace(event("HYSTRIX_COMMAND_INTERNAL", getExecutionObservable));
        Method helloMethod = HelloRepository.class.getDeclaredMethod("hello", String.class, long.class);
        verifier.verifyTrace(event(ServiceType.INTERNAL_METHOD.getName(), helloMethod, HystrixTestHelper.INTERRUPTED_EXCEPTION_DUE_TO_TIMEOUT));

        verifier.verifyTraceCount(0);
    }

    @Test
    public void hystrixCommand_execute_shortCircuit() throws Exception {
        hystrixCommandTestRunner.executeWithShortCircuit();

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.printCache();
        verifier.ignoreServiceType("RX_JAVA", "RX_JAVA_INTERNAL");

        HystrixTestHelper.verifyHystrixMetricsInitialization(verifier, SayHelloCommand.class.getSimpleName(), COMMAND_GROUP);

        Method executeMethod = HystrixCommand.class.getDeclaredMethod("execute");
        verifier.verifyTrace(event("HYSTRIX_COMMAND", executeMethod, annotation("hystrix.command", SayHelloCommand.class.getSimpleName())));

        // fallback due to short circuit
        ExpectedAnnotation fallbackCauseAnnotation = annotation("hystrix.command.fallback.cause", "short-circuited");
        ExpectedAnnotation exceptionAnnotation = annotation("hystrix.command.exception", HystrixTestHelper.SHORT_CIRCUIT_EXCEPTION.toString());
        verifier.verifyTrace(event(
                "HYSTRIX_COMMAND_INTERNAL",
                "com.netflix.hystrix.AbstractCommand.getFallbackOrThrowException(com.netflix.hystrix.HystrixEventType, com.netflix.hystrix.exception.HystrixRuntimeException$FailureType, java.lang.String, java.lang.Exception)",
                fallbackCauseAnnotation, exceptionAnnotation));
        verifier.verifyTrace(event("HYSTRIX_COMMAND_INTERNAL", "com.netflix.hystrix.HystrixCommand.getFallbackObservable()"));

        verifier.verifyTraceCount(0);
    }

    @Test
    public void hystrixCommand_observe() throws Exception {
        hystrixCommandTestRunner.observe();

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.printCache();
        verifier.ignoreServiceType("RX_JAVA", "RX_JAVA_INTERNAL");

        HystrixTestHelper.verifyHystrixMetricsInitialization(verifier, SayHelloCommand.class.getSimpleName(), COMMAND_GROUP);

        verifier.verifyTrace(event("HYSTRIX_COMMAND", "com.netflix.hystrix.AbstractCommand.observe()", annotation("hystrix.command", SayHelloCommand.class.getSimpleName())));
        verifier.verifyTrace(event("HYSTRIX_COMMAND_INTERNAL", "com.netflix.hystrix.AbstractCommand$HystrixObservableTimeoutOperator.call(rx.Subscriber)"));
        verifier.verifyTrace(event("HYSTRIX_COMMAND_INTERNAL", "com.netflix.hystrix.strategy.concurrency.HystrixContextScheduler$HystrixContextSchedulerWorker.schedule(rx.functions.Action0)"));

        // execution
        verifier.verifyTrace(event(ServiceType.ASYNC.getName(), "Asynchronous Invocation"));
        Method getExecutionObservable = HystrixCommand.class.getDeclaredMethod("getExecutionObservable");
        verifier.verifyTrace(event("HYSTRIX_COMMAND_INTERNAL", getExecutionObservable));
        Method helloMethod = HelloRepository.class.getDeclaredMethod("hello", String.class);
        verifier.verifyTrace(event(ServiceType.INTERNAL_METHOD.getName(), helloMethod));

        verifier.verifyTraceCount(0);
    }

    @Test
    public void hystrixCommand_toObservable() throws Exception {
        hystrixCommandTestRunner.toObservable();

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.printCache();
        verifier.ignoreServiceType("RX_JAVA", "RX_JAVA_INTERNAL");

        HystrixTestHelper.verifyHystrixMetricsInitialization(verifier, SayHelloCommand.class.getSimpleName(), COMMAND_GROUP);

        verifier.verifyTrace(event("HYSTRIX_COMMAND", "com.netflix.hystrix.AbstractCommand.toObservable()", annotation("hystrix.command", SayHelloCommand.class.getSimpleName())));
        verifier.verifyTrace(event("HYSTRIX_COMMAND_INTERNAL", "com.netflix.hystrix.AbstractCommand$HystrixObservableTimeoutOperator.call(rx.Subscriber)"));
        verifier.verifyTrace(event("HYSTRIX_COMMAND_INTERNAL", "com.netflix.hystrix.strategy.concurrency.HystrixContextScheduler$HystrixContextSchedulerWorker.schedule(rx.functions.Action0)"));

        // execution
        verifier.verifyTrace(event(ServiceType.ASYNC.getName(), "Asynchronous Invocation"));
        Method getExecutionObservable = HystrixCommand.class.getDeclaredMethod("getExecutionObservable");
        verifier.verifyTrace(event("HYSTRIX_COMMAND_INTERNAL", getExecutionObservable));
        Method helloMethod = HelloRepository.class.getDeclaredMethod("hello", String.class);
        verifier.verifyTrace(event(ServiceType.INTERNAL_METHOD.getName(), helloMethod));

        verifier.verifyTraceCount(0);
    }
}
