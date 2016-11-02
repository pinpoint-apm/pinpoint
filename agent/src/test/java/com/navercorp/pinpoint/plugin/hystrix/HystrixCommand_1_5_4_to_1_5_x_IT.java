package com.navercorp.pinpoint.plugin.hystrix;

import com.navercorp.pinpoint.bootstrap.plugin.test.Expectations;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifierHolder;
import com.navercorp.pinpoint.plugin.hystrix.commands.SayHelloCommand;
import com.navercorp.pinpoint.plugin.hystrix.commands.ThrowExceptionCommand;
import com.navercorp.pinpoint.plugin.hystrix.commands.ThrowExceptionCommandWithFallback;
import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.PinpointPluginTestSuite;
import com.netflix.hystrix.HystrixCommand;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.Method;

import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.annotation;

/**
 * Integration test for hystrix-core 1.5.4+ traces the following :
 * <p>
 * <tt>HystrixCommand.queue()</tt><br/>
 * <tt>HystrixCommand$2.call()</tt> - Anonymous inner class that invokes run()<br/>
 * <tt>HystrixCommand$3.call()</tt> - Anonymous inner class that invokes getFallback()<br/>
 * <p>
 * Additionally, the cause of fallback should be traced via AbstractCommand.getExecutionException() invocation.
 * <p>
 * Created by jack on 7/4/16.
 *
 * @author Jiaqi Feng
 * @author HyunGil Jeong
 */
@RunWith(PinpointPluginTestSuite.class)
@Dependency({"com.netflix.hystrix:hystrix-core:[1.5.4,)","com.netflix.hystrix:hystrix-metrics-event-stream:1.1.2"})
public class HystrixCommand_1_5_4_to_1_5_x_IT extends HystrixCommandITBase {

    private static final String EXECUTION_OBSERVABLE_INNER_CLASS = "com.netflix.hystrix.HystrixCommand$2";
    private static final String FALLBACK_OBSERVABLE_INNER_CLASS = "com.netflix.hystrix.HystrixCommand$3";

    @Test
    public void testSyncCall() throws Exception {
        String name = "Pinpoint";
        executeSayHelloCommand(name);

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.printCache();

        Method queue = HystrixCommand.class.getMethod("queue");
        Class<?> executionObservableClazz = Class.forName(EXECUTION_OBSERVABLE_INNER_CLASS);
        Method executioObservableCallCmd = executionObservableClazz.getDeclaredMethod("call");

        verifier.verifyTrace(Expectations.async(
                Expectations.event("HYSTRIX_COMMAND", queue, annotation("hystrix.command", SayHelloCommand.class.getSimpleName())),
                Expectations.event("ASYNC", "Asynchronous Invocation"),
                Expectations.event("HYSTRIX_COMMAND_INTERNAL", executioObservableCallCmd,
                        annotation("hystrix.command.execution", "run"))
        ));

        // no more traces
        verifier.verifyTraceCount(0);
    }

    @Test
    public void testAsyncCall() throws Exception {
        String name = "Pinpoint";
        queueAndGetSayHelloCommand(name);

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.printCache();

        Method queue = HystrixCommand.class.getMethod("queue");
        Class<?> executionObservableClazz = Class.forName(EXECUTION_OBSERVABLE_INNER_CLASS);
        Method executionObservableCallCmd = executionObservableClazz.getDeclaredMethod("call");

        verifier.verifyTrace(Expectations.async(
                Expectations.event("HYSTRIX_COMMAND", queue, annotation("hystrix.command", SayHelloCommand.class.getSimpleName())),
                Expectations.event("ASYNC", "Asynchronous Invocation"),
                Expectations.event("HYSTRIX_COMMAND_INTERNAL", executionObservableCallCmd,
                        annotation("hystrix.command.execution", "run"))
        ));

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
        Class<?> executionObservableClazz = Class.forName(EXECUTION_OBSERVABLE_INNER_CLASS);
        Method executionObservableCallCmd = executionObservableClazz.getDeclaredMethod("call");
        Class<?> fallbackObservableClazz = Class.forName(FALLBACK_OBSERVABLE_INNER_CLASS);
        Method fallbackObservableCallCmd = fallbackObservableClazz.getDeclaredMethod("call");

        verifier.verifyTrace(Expectations.async(
                Expectations.event("HYSTRIX_COMMAND", queue, annotation("hystrix.command", ThrowExceptionCommand.class.getSimpleName())),
                Expectations.event("ASYNC", "Asynchronous Invocation"),
                Expectations.event("HYSTRIX_COMMAND_INTERNAL", executionObservableCallCmd,
                        annotation("hystrix.command.execution", "run")),
                Expectations.event("ASYNC", "Asynchronous Invocation"),
                Expectations.event("HYSTRIX_COMMAND_INTERNAL", fallbackObservableCallCmd,
                        annotation("hystrix.command.execution", "fallback"),
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
        Class<?> executionObservableClazz = Class.forName(EXECUTION_OBSERVABLE_INNER_CLASS);
        Method executionObservableCallCmd = executionObservableClazz.getDeclaredMethod("call");
        Class<?> fallbackObservableClazz = Class.forName(FALLBACK_OBSERVABLE_INNER_CLASS);
        Method fallbackObservableCallCmd = fallbackObservableClazz.getDeclaredMethod("call");

        verifier.verifyTrace(Expectations.async(
                Expectations.event("HYSTRIX_COMMAND", queue, annotation("hystrix.command", ThrowExceptionCommandWithFallback.class.getSimpleName())),
                Expectations.event("ASYNC", "Asynchronous Invocation"),
                Expectations.event("HYSTRIX_COMMAND_INTERNAL", executionObservableCallCmd,
                        annotation("hystrix.command.execution", "run")),
                Expectations.event("ASYNC", "Asynchronous Invocation"),
                Expectations.event("HYSTRIX_COMMAND_INTERNAL", fallbackObservableCallCmd,
                        annotation("hystrix.command.execution", "fallback"),
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
