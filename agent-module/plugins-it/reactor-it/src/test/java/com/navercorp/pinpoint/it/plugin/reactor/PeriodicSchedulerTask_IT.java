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
import reactor.core.Disposable;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import test.pinpoint.plugin.reactor.Echo;
import test.pinpoint.plugin.reactor.ReactorFlow;

import java.lang.reflect.Method;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The opt-in periodic policy records every scheduler tick as a new root transaction. There is no
 * scheduling-side AsyncContext capture, so the two executions below must be reported as two
 * discrete REACTOR_SCHEDULER traces rather than continuations of one long-lived trace.
 */
@PluginTest
@PinpointAgent(AgentPath.PATH)
@PinpointConfig("pinpoint-periodic-scheduler-task.config")
@Dependency({"io.projectreactor:reactor-core:[3.6.9][3.7.19]"})
public class PeriodicSchedulerTask_IT {
    private static final long AWAIT_UNIT_MILLIS = 20L;
    private static final long AWAIT_MAX_MILLIS = 5000L;

    @Test
    public void schedulePeriodically_createsIndependentTransactionPerTick() throws Exception {
        Scheduler scheduler = Schedulers.newSingle("it-periodic-root");
        Disposable periodic = null;
        try {
            CountDownLatch latch = new CountDownLatch(2);
            periodic = scheduler.schedulePeriodically(() -> {
                try {
                    new Echo().get("periodic");
                } finally {
                    latch.countDown();
                }
            }, 0, 100, TimeUnit.MILLISECONDS);

            assertTrue(latch.await(AWAIT_MAX_MILLIS, TimeUnit.MILLISECONDS), "periodic task did not execute twice");
        } finally {
            if (periodic != null) {
                periodic.dispose();
            }
            scheduler.dispose();
        }

        verifyTwoIndependentTickRoots();
    }

    @Test
    public void schedulingInsideTrace_doesNotRetainSchedulingTransaction() throws Exception {
        Scheduler scheduler = Schedulers.newSingle("it-periodic-detached");
        AtomicReference<Disposable> periodic = new AtomicReference<>();
        try {
            CountDownLatch latch = new CountDownLatch(2);

            // The periodic task is created while ReactorFlow.execute owns a live transaction. The
            // initial delay ensures that transaction has closed before the first tick. If the task
            // captures a scheduling-side AsyncContext, these callbacks become children of the
            // scheduling transaction instead of two independent roots and verification fails.
            new ReactorFlow().execute(() -> periodic.set(scheduler.schedulePeriodically(() -> {
                try {
                    new Echo().get("periodic-detached");
                } finally {
                    latch.countDown();
                }
            }, 100, 100, TimeUnit.MILLISECONDS)));

            assertTrue(latch.await(AWAIT_MAX_MILLIS, TimeUnit.MILLISECONDS), "periodic task did not execute twice");
        } finally {
            Disposable disposable = periodic.get();
            if (disposable != null) {
                disposable.dispose();
            }
            scheduler.dispose();
        }

        verifyTwoIndependentTickRoots();
    }

    @Test
    public void concurrentPeriodicTasks_keepIndependentInvocationState() throws Exception {
        Scheduler scheduler = Schedulers.newParallel("it-periodic-concurrent", 2);
        AtomicReference<Disposable> first = new AtomicReference<>();
        AtomicReference<Disposable> second = new AtomicReference<>();
        CountDownLatch entered = new CountDownLatch(2);
        CountDownLatch release = new CountDownLatch(1);
        CountDownLatch finished = new CountDownLatch(2);
        Runnable tick = () -> {
            entered.countDown();
            try {
                if (release.await(AWAIT_MAX_MILLIS, TimeUnit.MILLISECONDS)) {
                    new Echo().get("periodic-concurrent");
                }
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            } finally {
                finished.countDown();
            }
        };

        try {
            // A long period keeps this test to one invocation per task. Both first invocations
            // are held open together, proving that their interceptor state is invocation-local
            // rather than shared by the periodic task type or scheduler.
            first.set(scheduler.schedulePeriodically(tick, 0, 1, TimeUnit.DAYS));
            second.set(scheduler.schedulePeriodically(tick, 0, 1, TimeUnit.DAYS));

            assertTrue(entered.await(AWAIT_MAX_MILLIS, TimeUnit.MILLISECONDS),
                    "periodic tasks did not execute concurrently");
            release.countDown();
            assertTrue(finished.await(AWAIT_MAX_MILLIS, TimeUnit.MILLISECONDS),
                    "periodic tasks did not finish");
        } finally {
            release.countDown();
            Disposable firstTask = first.get();
            if (firstTask != null) {
                firstTask.dispose();
            }
            Disposable secondTask = second.get();
            if (secondTask != null) {
                secondTask.dispose();
            }
            scheduler.dispose();
        }

        verifyTwoIndependentTickRoots();
    }

    private void verifyTwoIndependentTickRoots() throws Exception {
        Method periodicRun = Class.forName("reactor.core.scheduler.PeriodicSchedulerTask").getDeclaredMethod("run");
        Method echoGet = Echo.class.getDeclaredMethod("get", String.class);
        ExpectedTrace root = Expectations.root(
                "REACTOR_SCHEDULER",
                periodicRun,
                "Reactor periodic scheduler task",
                "LOCAL",
                "LOCAL");
        ExpectedTrace callback = Expectations.event("INTERNAL_METHOD", echoGet);

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.awaitTrace(root, AWAIT_UNIT_MILLIS, AWAIT_MAX_MILLIS);
        verifier.verifyDiscreteTrace(root, callback);
        verifier.awaitTrace(root, AWAIT_UNIT_MILLIS, AWAIT_MAX_MILLIS);
        verifier.verifyDiscreteTrace(root, callback);
    }
}
