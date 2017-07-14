/*
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

import com.navercorp.pinpoint.plugin.hystrix.commands.SayHelloCommand;
import com.navercorp.pinpoint.plugin.hystrix.commands.ThrowExceptionCommand;
import com.navercorp.pinpoint.plugin.hystrix.commands.ThrowExceptionCommandWithFallback;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixEventType;
import com.netflix.hystrix.exception.HystrixRuntimeException;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.navercorp.pinpoint.bootstrap.plugin.test.Expectations;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifierHolder;
import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.PinpointPluginTestSuite;

import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.annotation;

/**
 * Integration test for hystrix-core 1.3.20 traces the following :
 * <p>
 * <tt>HystrixCommand.queue()</tt><br/>
 * <tt>HystrixCommand.executeCommand</tt><br/>
 * <tt>HystrixCommand.getFallbackOrThrowException(HystrixEventType, FailureType, String, Exception)</tt><br/>
 * <p>
 * Additionally, the cause of fallback should be traced
 *
 * @see HystrixCommand_1_3_20_IT
 * @author Jiaqi Feng
 */
@RunWith(PinpointPluginTestSuite.class)
@Dependency({"com.netflix.hystrix:hystrix-core:1.3.20","com.netflix.hystrix:hystrix-metrics-event-stream:1.1.2"})
public class HystrixCommand_1_3_20_IT extends HystrixCommandITBase {

    @Test
    public void testSyncCall() throws Exception {
        String name = "Pinpoint";
        executeSayHelloCommand(name);

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.printCache();

        Method queue    = HystrixCommand.class.getMethod("queue");
        Method executeCmd = HystrixCommand.class.getDeclaredMethod("executeCommand");

        verifier.verifyTrace(Expectations.async(
                Expectations.event("HYSTRIX_COMMAND", queue, annotation("hystrix.command", SayHelloCommand.class.getSimpleName())),
                Expectations.event("ASYNC", "Asynchronous Invocation"),
                Expectations.event("HYSTRIX_COMMAND_INTERNAL", executeCmd)));

        // no more traces
        verifier.verifyTraceCount(0);
    }

    @Test
    public void testAsyncCall() throws Exception {
        String name = "Pinpoint";
        queueAndGetSayHelloCommand(name);

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.printCache();

        Method queue    = HystrixCommand.class.getMethod("queue");
        Method executeCmd = HystrixCommand.class.getDeclaredMethod("executeCommand");

        verifier.verifyTrace(Expectations.async(
                Expectations.event("HYSTRIX_COMMAND", queue, annotation("hystrix.command", SayHelloCommand.class.getSimpleName())),
                Expectations.event("ASYNC", "Asynchronous Invocation"),
                Expectations.event("HYSTRIX_COMMAND_INTERNAL", executeCmd)));

        // no more traces
        verifier.verifyTraceCount(0);
    }

    @Test
    public void testExecutionException() throws Exception {
        Exception expectedException = new RuntimeException("expected");
        executeThrowExceptionCommand(expectedException);

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.printCache();

        Method queue = HystrixCommand.class.getMethod("queue");
        Method executeCmd = HystrixCommand.class.getDeclaredMethod("executeCommand");
        Method getFallbackOrThrowException = HystrixCommand.class.getDeclaredMethod("getFallbackOrThrowException", HystrixEventType.class, HystrixRuntimeException.FailureType.class, String.class, Exception.class);

        verifier.verifyTrace(Expectations.async(
                Expectations.event("HYSTRIX_COMMAND", queue, annotation("hystrix.command", ThrowExceptionCommand.class.getSimpleName())),
                Expectations.event("ASYNC", "Asynchronous Invocation"),
                Expectations.event("HYSTRIX_COMMAND_INTERNAL", executeCmd),
                Expectations.event("HYSTRIX_COMMAND_INTERNAL", getFallbackOrThrowException,
                        annotation("hystrix.command.fallback.cause", expectedException.toString()))
        ));

        // no more traces
        verifier.verifyTraceCount(0);
    }

    @Test
    public void testExecutionExceptionWithFallback() throws Exception {
        Exception expectedException = new RuntimeException("expected");
        String fallbackMessage = "Fallback";
        executeThrowExceptionWithFallbackCommand(expectedException, fallbackMessage);

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.printCache();

        Method queue = HystrixCommand.class.getMethod("queue");
        Method executeCmd = HystrixCommand.class.getDeclaredMethod("executeCommand");
        Method getFallbackOrThrowException = HystrixCommand.class.getDeclaredMethod("getFallbackOrThrowException", HystrixEventType.class, HystrixRuntimeException.FailureType.class, String.class, Exception.class);

        verifier.verifyTrace(Expectations.async(
                Expectations.event("HYSTRIX_COMMAND", queue, annotation("hystrix.command", ThrowExceptionCommandWithFallback.class.getSimpleName())),
                Expectations.event("ASYNC", "Asynchronous Invocation"),
                Expectations.event("HYSTRIX_COMMAND_INTERNAL", executeCmd),
                Expectations.event("HYSTRIX_COMMAND_INTERNAL", getFallbackOrThrowException,
                        annotation("hystrix.command.fallback.cause", expectedException.toString()))
        ));

        // no more traces
        verifier.verifyTraceCount(0);
    }

    @Test
    public void testTraceContinuation() throws Exception {
        String name = "Pinpoint";
        executeInvokeSayHelloCommand(name);

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.printCache();

        // If the trace is propagated properly to a HystrixCommand's run() method, there should be 6 total traces.
        // 3 for InvokeSayHelloCommand, 3 for SayHelloCommand.
        verifier.verifyTraceCount(6);
    }
}
