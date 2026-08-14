/*
 * Copyright 2026 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.it.plugin.reactor;

import com.navercorp.pinpoint.bootstrap.plugin.test.Expectations;
import com.navercorp.pinpoint.bootstrap.plugin.test.ExpectedTrace;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifierHolder;
import com.navercorp.pinpoint.it.plugin.utils.AgentPath;
import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.PinpointAgent;
import com.navercorp.pinpoint.test.plugin.PinpointConfig;
import com.navercorp.pinpoint.test.plugin.PluginTest;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import test.pinpoint.plugin.reactor.Echo;
import test.pinpoint.plugin.reactor.ReactorFlow;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Assertions for the scheduler task carrier ({@code profiler.reactor.trace.scheduler.task=true}):
 * reactor's built-in schedulers wrap every submitted Runnable in a task object
 * ({@code SchedulerTask}, {@code WorkerTask}, ...) before it crosses to a worker thread, and the
 * carrier instrumentation parks the AsyncContext on that task and re-activates it around
 * {@code run()}/{@code call()}.
 *
 * <h2>What only the carrier covers</h2>
 * A <b>plain application Runnable</b> handed to {@code Scheduler.schedule(...)} inside an active
 * trace. No operator subscriber is involved, so nothing else records or relays a context — the
 * carrier's current-trace fallback records a REACTOR boundary event on the scheduling side and the
 * task activation binds it on the worker. The async-link assertion
 * ({@link Expectations#async(ExpectedTrace, ExpectedTrace...)}, see {@code ReactorPropagation_IT}
 * for why only that assertion discriminates) links the two.
 *
 * <p><b>Discriminating probe (manual)</b>: flip {@code profiler.reactor.trace.scheduler.task=false}
 * in {@code pinpoint-scheduler-task.config} — the plain-Runnable cases must fail (no boundary
 * event, callback becomes a standalone trace), then flip it back.
 *
 * <h2>onScheduleHook decorators</h2>
 * From reactor 3.4 the scheduler passes the Runnable through {@code Schedulers.onSchedule(...)}
 * BEFORE constructing the task, so a decorator registered by the application or another agent
 * makes the constructor argument an anonymous wrapper. The carrier then cannot see a carried
 * context and must degrade to the current-trace fallback — the link survives, only the carrier
 * identity is lost. {@link #onScheduleHookDecorator_fallbackStillLinks} pins that behaviour.
 */
@PluginTest
@PinpointAgent(AgentPath.PATH)
@PinpointConfig("pinpoint-scheduler-task.config")
@Dependency({"io.projectreactor:reactor-core:[3.6.9][3.7.19]"})
public class SchedulerTaskCarrier_IT {

    private static final String REACTOR = "REACTOR";
    private static final String INTERNAL_METHOD = "INTERNAL_METHOD";

    private static final String FLUX_PUBLISH_ON =
            "reactor.core.publisher.Flux.publishOn(reactor.core.scheduler.Scheduler, boolean, int, int)";

    private static final long AWAIT_UNIT_MILLIS = 20L;
    private static final long AWAIT_MAX_MILLIS = 5000L;

    private static Method echoGet() throws NoSuchMethodException {
        return Echo.class.getDeclaredMethod("get", String.class);
    }

    /**
     * {@code Scheduler.schedule(Runnable)} goes through {@code Schedulers.directSchedule} which
     * constructs {@code SchedulerTask} — the boundary event is recorded from its constructor.
     */
    private static Constructor<?> schedulerTaskConstructor() throws Exception {
        final Class<?> schedulerTask = Class.forName("reactor.core.scheduler.SchedulerTask");
        return schedulerTask.getDeclaredConstructors()[0];
    }

    // ------------------------------------------------------------------
    // the carrier's own coverage: plain Runnable, no operator involved
    // ------------------------------------------------------------------

    @Test
    public void plainRunnable_currentTraceFallback_linksAcrossThread() throws Exception {
        final Scheduler scheduler = Schedulers.newParallel("it-plain-runnable", 2);
        try {
            runPlainScheduleAndVerifyAsyncLink(scheduler);
        } finally {
            scheduler.dispose();
        }
    }

    @Test
    public void onScheduleHookDecorator_fallbackStillLinks() throws Exception {
        // the decorator hides any carried context from the task constructor (the argument becomes
        // this anonymous wrapper) - the carrier must fall back to the current trace, not break.
        Schedulers.onScheduleHook("it-decorator", runnable -> () -> runnable.run());
        final Scheduler scheduler = Schedulers.newParallel("it-hook-decorator", 2);
        try {
            runPlainScheduleAndVerifyAsyncLink(scheduler);
        } finally {
            scheduler.dispose();
            Schedulers.resetOnScheduleHook("it-decorator");
        }
    }

    // ------------------------------------------------------------------
    // coexistence: the per-operator relay keeps working with the carrier on
    // ------------------------------------------------------------------

    /**
     * publishOn schedules its own subscriber (an instrumented carrier), so here the task copies
     * the existing context (carrier-first, no extra boundary event) and the established
     * per-operator async link must be unchanged. Also guards against double activation: the task
     * window and the subscriber's own run window nest on the same scope.
     */
    @Test
    public void publishOn_withCarrierOn_relayLinkUnchanged() throws Exception {
        final Scheduler scheduler = Schedulers.newParallel("it-carrier-publishOn", 2);
        try {
            final CountDownLatch latch = new CountDownLatch(1);
            final List<String> callbackThreads = new CopyOnWriteArrayList<>();
            final String mainThread = Thread.currentThread().getName();

            new ReactorFlow().execute(() ->
                    Flux.range(1, 1)
                            .publishOn(scheduler)
                            .map(v -> echoOnce(latch, callbackThreads, v))
                            .subscribe());

            assertTrue(latch.await(AWAIT_MAX_MILLIS, TimeUnit.MILLISECONDS), "callback was not invoked");
            assertEquals(1, callbackThreads.size(), "callback ran an unexpected number of times");
            assertNotEquals(mainThread, callbackThreads.get(0), "no scheduler hop");

            final ExpectedTrace callback = Expectations.event(INTERNAL_METHOD, echoGet());
            final ExpectedTrace asyncLink = Expectations.async(
                    Expectations.event(REACTOR, FLUX_PUBLISH_ON), callback);

            final PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
            verifier.awaitTrace(callback, AWAIT_UNIT_MILLIS, AWAIT_MAX_MILLIS);
            verifier.printCache();
            verifier.verifyDiscreteTrace(asyncLink);
        } finally {
            scheduler.dispose();
        }
    }

    // ------------------------------------------------------------------
    // helpers
    // ------------------------------------------------------------------

    private void runPlainScheduleAndVerifyAsyncLink(Scheduler scheduler) throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        final List<String> callbackThreads = new CopyOnWriteArrayList<>();
        final String mainThread = Thread.currentThread().getName();

        new ReactorFlow().execute(() ->
                scheduler.schedule(() -> echoOnce(latch, callbackThreads, "plain")));

        assertTrue(latch.await(AWAIT_MAX_MILLIS, TimeUnit.MILLISECONDS), "task was not executed");
        assertEquals(1, callbackThreads.size(), "task ran an unexpected number of times");
        assertNotEquals(mainThread, callbackThreads.get(0),
                "task ran on the scheduling thread - no hop, the assertion would be vacuous");

        final ExpectedTrace callback = Expectations.event(INTERNAL_METHOD, echoGet());
        final ExpectedTrace asyncLink = Expectations.async(
                Expectations.event(REACTOR, schedulerTaskConstructor()), callback);

        final PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.awaitTrace(callback, AWAIT_UNIT_MILLIS, AWAIT_MAX_MILLIS);
        verifier.printCache();
        verifier.verifyDiscreteTrace(asyncLink);
    }

    private String echoOnce(CountDownLatch latch, List<String> threads, Object value) {
        threads.add(Thread.currentThread().getName());
        try {
            return new Echo().get("Hello" + value);
        } finally {
            latch.countDown();
        }
    }
}
