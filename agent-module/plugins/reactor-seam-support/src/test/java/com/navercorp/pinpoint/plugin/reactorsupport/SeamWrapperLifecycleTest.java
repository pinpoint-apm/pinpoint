/*
 * Copyright 2026 NAVER Corp.
 *
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
package com.navercorp.pinpoint.plugin.reactorsupport;

import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.scope.TraceScope;
import com.navercorp.pinpoint.bootstrap.util.ScopeUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Flux;
import reactor.util.context.Context;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Lifecycle-attribution pins for the wrapped publisher (W7 / doc 18 §4.5): what happens when the
 * application holds on to a wrapped publisher longer than one delivery.
 *
 * <ul>
 * <li><b>Cached publisher (announced limit P5)</b>: the wrapper owns the AsyncContext it was
 * created with — every later subscription, from whatever request, is attributed to the FIRST
 * caller's context. Each subscription still gets its own subscription-held trace, created and
 * closed independently: caching shares attribution, never trace state.</li>
 * <li><b>retry resubscription</b>: the retry machinery resubscribes from its subscription-stack
 * drain loop, outside any window frame — each attempt is an independent subscription with an
 * independently created and closed held trace; a failed attempt cannot leak into the next.</li>
 * <li><b>hot/never publisher</b>: with no terminal signal the held trace stays open for the
 * subscription's whole lifetime BY DESIGN; cancel() is the bounded cleanup (the W1 state machine
 * closes it exactly once).</li>
 * </ul>
 *
 * Instead of positional stubs, {@link StubAsyncContext} and {@link DepthScope} faithfully
 * simulate the single-threaded thread-local binding and scope-depth contracts — the observed
 * close/creation counts are emergent, not choreographed.
 */
public class SeamWrapperLifecycleTest {

    /** the one delivery thread of these tests: at most one bound trace at a time. */
    private final Trace[] boundSlot = new Trace[1];

    private Trace firstTrace;
    private Trace secondTrace;
    private StubAsyncContext seamContext;

    @BeforeEach
    public void setUp() {
        firstTrace = newTrace();
        secondTrace = newTrace();
        seamContext = new StubAsyncContext(boundSlot, firstTrace, secondTrace);
    }

    private static Trace newTrace() {
        final Trace trace = mock(Trace.class);
        when(trace.getScope(ScopeUtils.ASYNC_TRACE_SCOPE)).thenReturn(new DepthScope());
        when(trace.isAsync()).thenReturn(true);
        return trace;
    }

    @SuppressWarnings("unchecked")
    private <T> Flux<T> wrapped(Flux<T> source) {
        return (Flux<T>) SeamPublisherWrapper.wrap(source, seamContext);
    }

    @Test
    public void cachedPublisher_everySubscriptionAttributedToFirstCallersContext() {
        // the app caches the wrapped publisher across requests; a later subscription has no way
        // to carry its own context here - announced limit P5.
        final Flux<Integer> cached = wrapped(Flux.range(1, 2).hide());

        final TestFusionSubscriber<Integer> requestA = new TestFusionSubscriber<Integer>();
        final TestFusionSubscriber<Integer> requestB = new TestFusionSubscriber<Integer>();
        cached.subscribe(requestA);
        cached.subscribe(requestB);

        assertTrue(requestA.completed);
        assertTrue(requestB.completed);
        // both subscriptions drew their trace from the seam's (first caller's) context...
        assertEquals(2, seamContext.creations);
        // ...but as independent subscription-held traces, each closed exactly once at its own
        // terminal - caching shares attribution, never trace state.
        verify(firstTrace, times(1)).close();
        verify(secondTrace, times(1)).close();
    }

    @Test
    public void retryResubscription_independentHeldTracePerAttempt_eachClosedOnce() {
        final AtomicInteger attempts = new AtomicInteger();
        final Flux<Integer> failsOnce = Flux.defer(() ->
                attempts.incrementAndGet() == 1
                        ? Flux.<Integer>error(new IllegalStateException("first attempt fails"))
                        : Flux.just(42).hide());

        final TestFusionSubscriber<Integer> downstream = new TestFusionSubscriber<Integer>();
        wrapped(failsOnce).retry(1).subscribe(downstream);

        assertEquals(2, attempts.get());
        assertTrue(downstream.completed);
        assertEquals(Arrays.asList(42), downstream.received);
        // the retry machinery resubscribes from its subscription-stack drain loop - OUTSIDE any
        // window frame - so each attempt is an independent subscription with its own held trace,
        // created once and closed exactly once at its own terminal. A failed attempt leaks
        // nothing into the next one.
        assertEquals(2, seamContext.creations);
        verify(firstTrace, times(1)).close();
        verify(secondTrace, times(1)).close();
    }

    @Test
    public void neverPublisher_heldTraceLivesUntilCancel_thenClosedExactlyOnce() {
        final SubscriptionCapturingSubscriber<Integer> downstream = new SubscriptionCapturingSubscriber<Integer>();
        wrapped(Flux.<Integer>never()).subscribe(downstream);

        // no terminal signal ever arrives: the subscription-held trace stays open BY DESIGN
        // (long-lived stream semantics - doc 18 §4.5).
        verify(firstTrace, never()).close();

        // cancel is the bounded cleanup: the W1 ownership handoff closes the HELD trace exactly
        // once. (cancel's control window creates and fully closes its own frame-local trace -
        // secondTrace here - which is unrelated to the held one.)
        downstream.subscription.cancel();
        verify(firstTrace, times(1)).close();
        verify(secondTrace, times(1)).close();

        downstream.subscription.cancel();
        verify(firstTrace, times(1)).close();
    }

    private static class SubscriptionCapturingSubscriber<T> implements CoreSubscriber<T> {
        Subscription subscription;

        @Override
        public void onSubscribe(Subscription s) {
            this.subscription = s;
            // no request: keep the window accounting to the onSubscribe frame only.
        }

        @Override
        public void onNext(T value) {
        }

        @Override
        public void onError(Throwable t) {
        }

        @Override
        public void onComplete() {
        }

        @Override
        public Context currentContext() {
            return Context.empty();
        }
    }
}
