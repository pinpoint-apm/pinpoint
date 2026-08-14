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
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import test.pinpoint.plugin.reactor.Echo;
import test.pinpoint.plugin.reactor.ReactorFlow;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Trace propagation assertions for the reactor plugin.
 *
 * <p>{@code Reactor_IT} is a smoke test - every case only calls {@code verifier.printCache()},
 * so it proves the woven bytecode runs without VerifyError/ClassCastException but says nothing
 * about whether the trace survived the operator chain and the scheduler hop. This class adds the
 * missing part.
 *
 * <h2>Why the assertion must be an async-link assertion</h2>
 * {@code Echo.get} is a {@code profiler.entrypoint}. It is tempting to assert that it shows up as
 * an {@code INTERNAL_METHOD} event, but {@code UserIncludeMethodInterceptor.before} records that
 * event in <em>both</em> branches - when it joins an active trace and when it creates its own
 * {@code STAND_ALONE} root. Such an assertion therefore cannot fail and proves nothing.
 *
 * <p>What actually distinguishes the two cases is the async link. When the trace propagates, the
 * scheduler-hop callback is recorded inside a {@code DefaultAsyncSpanChunk} whose {@code localAsyncId}
 * matches the {@code asyncIdObject} of the REACTOR event that created the AsyncContext:
 *
 * <pre>
 * seq=1 depth=2 REACTOR          Mono.subscribeOn(Scheduler)
 *               asyncIdObject=DefaultAsyncId{asyncId=1, sequence=1}
 * DefaultAsyncSpanChunk{localAsyncId=DefaultLocalAsyncId{asyncId=1, sequence=1}}
 *       depth=3 INTERNAL_METHOD  Echo.get(String)
 * </pre>
 *
 * {@link Expectations#async(ExpectedTrace, ExpectedTrace...)} asserts exactly that relation, so it
 * fails when propagation is lost and the callback ends up in a trace of its own.
 *
 * <p>Note that the reactor plugin does not emit an {@code "Asynchronous Invocation"} (ASYNC) event -
 * {@code CoreSubscriberRunInterceptor} is built with no async trace block - so the async chunk holds
 * the callback event directly.
 *
 * <p>{@code verifyDiscreteTrace} scans and skips non-matching items, so these assertions do not
 * depend on how many REACTOR operator events the plugin happens to emit.
 *
 * <p>These assertions run through failsafe and fail the module build when propagation regresses.
 */
@PluginTest
@PinpointAgent(AgentPath.PATH)
@PinpointConfig("pinpoint-propagation.config")
@Dependency({"io.projectreactor:reactor-core:[3.6.9][3.7.19]"})
public class ReactorPropagation_IT {

    private static final String REACTOR = "REACTOR";
    private static final String INTERNAL_METHOD = "INTERNAL_METHOD";

    // API descriptors as recorded by the plugin (taken from the printCache API dictionary).
    private static final String FLUX_PUBLISH_ON =
            "reactor.core.publisher.Flux.publishOn(reactor.core.scheduler.Scheduler, boolean, int, int)";
    private static final String FLUX_SUBSCRIBE_ON =
            "reactor.core.publisher.Flux.subscribeOn(reactor.core.scheduler.Scheduler, boolean)";
    private static final String MONO_PUBLISH_ON =
            "reactor.core.publisher.Mono.publishOn(reactor.core.scheduler.Scheduler)";
    private static final String MONO_SUBSCRIBE_ON =
            "reactor.core.publisher.Mono.subscribeOn(reactor.core.scheduler.Scheduler)";
    private static final String MONO_DELAY_ELEMENT =
            "reactor.core.publisher.Mono.delayElement(java.time.Duration, reactor.core.scheduler.Scheduler)";
    private static final String PARALLEL_FLUX_RUN_ON =
            "reactor.core.publisher.ParallelFlux.runOn(reactor.core.scheduler.Scheduler, int)";

    private static final long AWAIT_UNIT_MILLIS = 20L;
    private static final long AWAIT_MAX_MILLIS = 5000L;

    private static Method echoGet() throws NoSuchMethodException {
        return Echo.class.getDeclaredMethod("get", String.class);
    }

    // ------------------------------------------------------------------
    // scheduler hop - the cases the plugin exists for
    // ------------------------------------------------------------------

    @Test
    public void fluxPublishOn_propagatesAcrossThread() throws Exception {
        Scheduler scheduler = Schedulers.newParallel("it-flux-publishOn", 2);
        try {
            runFlowAndVerifyAsyncLink(FLUX_PUBLISH_ON, (latch, threads) ->
                    Flux.range(1, 1)
                            .publishOn(scheduler)
                            .map(v -> echoOnce(latch, threads, v))
                            .subscribe());
        } finally {
            scheduler.dispose();
        }
    }

    @Test
    public void fluxSubscribeOn_propagatesAcrossThread() throws Exception {
        Scheduler scheduler = Schedulers.newParallel("it-flux-subscribeOn", 2);
        try {
            runFlowAndVerifyAsyncLink(FLUX_SUBSCRIBE_ON, (latch, threads) ->
                    Flux.range(1, 1)
                            .map(i -> 10 + i)
                            .subscribeOn(scheduler)
                            .map(v -> echoOnce(latch, threads, v))
                            .subscribe());
        } finally {
            scheduler.dispose();
        }
    }

    @Test
    public void monoPublishOn_propagatesAcrossThread() throws Exception {
        Scheduler scheduler = Schedulers.newParallel("it-mono-publishOn", 2);
        try {
            runFlowAndVerifyAsyncLink(MONO_PUBLISH_ON, (latch, threads) ->
                    Mono.just("test")
                            .publishOn(scheduler)
                            .map(v -> echoOnce(latch, threads, v))
                            .subscribe());
        } finally {
            scheduler.dispose();
        }
    }

    /**
     * Also covers the area of the {@code Mono.subscribeOn} wiring defect: MonoMethodTransform
     * attaches {@code FluxAndMonoPublishOnInterceptor} instead of
     * {@code FluxAndMonoSubscribeOnInterceptor}. Propagation still works under the default config,
     * so this test does not fail on it - it is here so a fix has coverage.
     */
    @Test
    public void monoSubscribeOn_propagatesAcrossThread() throws Exception {
        Scheduler scheduler = Schedulers.newParallel("it-mono-subscribeOn", 2);
        try {
            runFlowAndVerifyAsyncLink(MONO_SUBSCRIBE_ON, (latch, threads) ->
                    Mono.fromCallable(() -> Collections.singletonList(1))
                            .subscribeOn(scheduler)
                            .map(v -> echoOnce(latch, threads, v))
                            .subscribe());
        } finally {
            scheduler.dispose();
        }
    }

    @Test
    public void monoDelayElement_propagatesAcrossTimerThread() throws Exception {
        runFlowAndVerifyAsyncLink(MONO_DELAY_ELEMENT, (latch, threads) ->
                Mono.just("Hello")
                        .delayElement(Duration.ofMillis(50L))
                        .map(v -> echoOnce(latch, threads, v))
                        .subscribe());
    }

    /**
     * concatMap creates its inner subscriber inside the enclosing subscriber's constructor
     * ({@code new ConcatMapInner(this)}). What makes the inner find a carrier at all is that
     * {@code CoreSubscriberConstructorInterceptor} copies it in {@code before()} - woven in right after
     * the {@code super()} call - so the enclosing subscriber is already seeded when the body runs.
     */
    @Test
    public void concatMapInner_propagatesAcrossThread() throws Exception {
        Scheduler scheduler = Schedulers.newParallel("it-concatmap-inner", 2);
        try {
            runFlowAndVerifyAsyncLink(FLUX_PUBLISH_ON, (latch, threads) ->
                    Flux.range(1, 1)
                            .publishOn(scheduler)
                            .concatMap(v -> Mono.just(v).map(x -> echoOnce(latch, threads, x)))
                            .subscribe());
        } finally {
            scheduler.dispose();
        }
    }

    @Test
    public void parallelFluxRunOn_propagatesAcrossThread() throws Exception {
        Scheduler scheduler = Schedulers.newParallel("it-parallel-runOn", 2);
        try {
            runFlowAndVerifyAsyncLink(PARALLEL_FLUX_RUN_ON, (latch, threads) ->
                    Flux.range(1, 1)
                            .parallel(2)
                            .runOn(scheduler)
                            .map(v -> echoOnce(latch, threads, v))
                            .subscribe());
        } finally {
            scheduler.dispose();
        }
    }

    // ------------------------------------------------------------------
    // assembled under a trace, subscribed without one
    // ------------------------------------------------------------------

    /**
     * The shape a WebFlux handler produces: the chain is assembled while a trace is active, the
     * Publisher is returned, and the framework subscribes to it later - possibly with no trace bound
     * to the subscribing thread. Propagation then has to come from the AsyncContext that
     * FluxAndMonoPublishOnInterceptor stored on the Publisher at assembly time, because
     * FluxAndMonoSubscribeMethodInterceptor bails out when there is no current trace.
     */
    @Test
    public void assembledUnderTrace_subscribedWithoutTrace() throws Exception {
        Scheduler scheduler = Schedulers.newParallel("it-subscribe-untraced", 2);
        try {
            final CountDownLatch latch = new CountDownLatch(1);
            final List<String> callbackThreads = new CopyOnWriteArrayList<>();
            final String mainThread = Thread.currentThread().getName();
            final Flux<?>[] assembled = new Flux<?>[1];

            // assembly under an active trace
            new ReactorFlow().execute(() -> assembled[0] = Flux.range(1, 1)
                    .publishOn(scheduler)
                    .map(v -> echoOnce(latch, callbackThreads, v)));

            // subscription with no trace bound to this thread
            assembled[0].subscribe();

            assertTrue(latch.await(AWAIT_MAX_MILLIS, TimeUnit.MILLISECONDS), "callback was not invoked");
            assertEquals(1, callbackThreads.size(), "callback ran an unexpected number of times");
            assertNotEquals(mainThread, callbackThreads.get(0), "no scheduler hop");

            final ExpectedTrace callback = Expectations.event(INTERNAL_METHOD, echoGet());
            final ExpectedTrace asyncLink = Expectations.async(
                    Expectations.event(REACTOR, FLUX_PUBLISH_ON), callback);

            PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
            verifier.awaitTrace(callback, AWAIT_UNIT_MILLIS, AWAIT_MAX_MILLIS);
            verifier.printCache();
            verifier.verifyDiscreteTrace(asyncLink);
        } finally {
            scheduler.dispose();
        }
    }

    // ------------------------------------------------------------------
    // same thread - sanity only, NOT propagation tests
    // ------------------------------------------------------------------

    /**
     * No scheduler hop, so the ThreadLocal trace simply stays bound and reactor propagation is not
     * exercised at all. Kept as a sanity check that the operator chain does not corrupt or drop the
     * ambient trace; it cannot detect a propagation regression.
     */
    @Test
    public void fluxMap_sameThreadSanity() throws Exception {
        new ReactorFlow().execute(() ->
                Flux.range(1, 2)
                        .map(v -> new Echo().get("Hello" + v))
                        .subscribe());

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.printCache();
        verifier.verifyDiscreteTrace(Expectations.event(INTERNAL_METHOD, echoGet()));
    }

    // ------------------------------------------------------------------
    // known gaps - kept disabled so the expected behaviour is recorded
    // ------------------------------------------------------------------

    /**
     * Publisher reuse across traces. {@code FluxAndMonoPublishOnInterceptor.after} stores the
     * assembly-time AsyncContext on the returned Publisher, and
     * {@code FluxAndMonoSubscribeOrReturnInterceptor.before} then overwrites the subscriber carrier
     * with it unconditionally. A cached Publisher therefore attributes later subscriptions to the
     * trace that assembled it. Reactor's Context is subscription scoped, so this is a structural
     * mismatch rather than a simple bug.
     */
    @Test
    @Disabled("known gap: assembly-time AsyncContext on the Publisher leaks across subscriptions")
    public void publisherReuse_isolatesSubscriptions() throws Exception {
        Scheduler scheduler = Schedulers.newParallel("it-publisher-reuse", 2);
        try {
            final Flux<String> shared = Flux.range(1, 1)
                    .publishOn(scheduler)
                    .map(v -> new Echo().get("Hello" + v))
                    .cache();

            new ReactorFlow().execute(shared::subscribe);   // trace A assembles and subscribes
            new ReactorFlow().execute(shared::subscribe);   // trace B must not be attributed to A

            PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
            verifier.printCache();
            verifier.verifyDiscreteTrace(Expectations.async(
                    Expectations.event(REACTOR, FLUX_PUBLISH_ON),
                    Expectations.event(INTERNAL_METHOD, echoGet())));
        } finally {
            scheduler.dispose();
        }
    }

    /**
     * Terminal-only sequences. {@code CoreSubscriberTransform} instruments constructors,
     * {@code onSubscribe}, {@code onNext} and {@code run} - not {@code onComplete} or
     * {@code onError} - so a sequence that never emits a value has no general activation point.
     */
    @Test
    @Disabled("known gap: general onComplete/onError are not instrumented")
    public void terminalOnly_activatesTrace() throws Exception {
        Scheduler scheduler = Schedulers.newParallel("it-terminal-only", 2);
        try {
            final CountDownLatch latch = new CountDownLatch(1);
            new ReactorFlow().execute(() ->
                    Mono.<String>empty()
                            .publishOn(scheduler)
                            .doOnSuccess(v -> {
                                new Echo().get("completed");
                                latch.countDown();
                            })
                            .subscribe());

            assertTrue(latch.await(AWAIT_MAX_MILLIS, TimeUnit.MILLISECONDS), "callback not invoked");

            PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
            verifier.printCache();
            verifier.verifyDiscreteTrace(Expectations.async(
                    Expectations.event(REACTOR, MONO_PUBLISH_ON),
                    Expectations.event(INTERNAL_METHOD, echoGet())));
        } finally {
            scheduler.dispose();
        }
    }

    // ------------------------------------------------------------------
    // helpers
    // ------------------------------------------------------------------

    private String echoOnce(CountDownLatch latch, List<String> threads, Object value) {
        threads.add(Thread.currentThread().getName());
        try {
            return new Echo().get("Hello" + value);
        } finally {
            latch.countDown();
        }
    }

    /**
     * Runs the flow inside the traced entrypoint, waits for the callback, asserts the callback ran
     * on another thread (otherwise the async assertion would be vacuous), then asserts the async
     * link between the initiating REACTOR event and the callback.
     */
    private void runFlowAndVerifyAsyncLink(String initiatorApiDescriptor, ThreadHopFlow flow) throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        final List<String> callbackThreads = new CopyOnWriteArrayList<>();
        final String mainThread = Thread.currentThread().getName();

        new ReactorFlow().execute(() -> flow.run(latch, callbackThreads));

        assertTrue(latch.await(AWAIT_MAX_MILLIS, TimeUnit.MILLISECONDS), "callback was not invoked");
        assertEquals(1, callbackThreads.size(), "callback ran an unexpected number of times");
        assertNotEquals(mainThread, callbackThreads.get(0),
                "callback ran on the subscribing thread - no scheduler hop, the assertion would be vacuous");

        final ExpectedTrace callback = Expectations.event(INTERNAL_METHOD, echoGet());
        final ExpectedTrace asyncLink = Expectations.async(
                Expectations.event(REACTOR, initiatorApiDescriptor), callback);

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        // read-only wait until the callback event is recorded; awaitTrace does nothing on timeout,
        // so the assertion below is what actually decides the result.
        verifier.awaitTrace(callback, AWAIT_UNIT_MILLIS, AWAIT_MAX_MILLIS);
        verifier.printCache();
        verifier.verifyDiscreteTrace(asyncLink);
    }

    private interface ThreadHopFlow {
        void run(CountDownLatch latch, List<String> callbackThreads);
    }
}
