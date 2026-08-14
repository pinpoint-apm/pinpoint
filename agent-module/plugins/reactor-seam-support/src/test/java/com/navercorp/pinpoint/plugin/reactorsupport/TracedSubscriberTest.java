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
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.Fuseable;
import reactor.core.publisher.Flux;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockingDetails;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TracedSubscriberTest {

    private AsyncContext asyncContext;
    private Trace trace;
    private TraceScope scope;

    @BeforeEach
    public void setUp() {
        asyncContext = mock(AsyncContext.class);
        trace = mock(Trace.class);
        scope = mock(TraceScope.class);
        // first owned window creates the trace, later windows rebind the held one.
        when(asyncContext.continueAsyncTraceObject(false)).thenReturn(trace);
        when(asyncContext.continueAsyncTraceObject(trace)).thenReturn(trace);
        when(trace.getScope(ScopeUtils.ASYNC_TRACE_SCOPE)).thenReturn(scope);
        when(scope.tryEnter()).thenReturn(true);
        when(scope.canLeave()).thenReturn(true);
        // scope stays active by default: windows neither unbind nor close the trace here.
        when(scope.isActive()).thenReturn(true);
    }

    @SuppressWarnings("unchecked")
    private Flux<Integer> wrapped(Flux<Integer> source) {
        Object wrapped = SeamPublisherWrapper.wrap(source, asyncContext);
        assertNotSame(source, wrapped);
        return (Flux<Integer>) wrapped;
    }

    @Test
    public void fusedSource_downstreamNegotiationRefused_allSignalsDeliveredViaOnNext() {
        // Flux.range(..).map(..) is a SYNC-fuseable chain: without the suppressor the downstream
        // could switch to pull-only poll() and bypass onNext entirely.
        Flux<Integer> source = Flux.range(1, 5).map(i -> i);
        TestFusionSubscriber<Integer> downstream = new TestFusionSubscriber<Integer>();

        wrapped(source).subscribe(downstream);

        assertEquals(Fuseable.NONE, downstream.negotiatedFusionMode);
        assertEquals(Arrays.asList(1, 2, 3, 4, 5), downstream.received);
        assertTrue(downstream.completed);
        assertTrue(downstream.subscription instanceof TracedSubscriber);
    }

    @Test
    public void queueSubscriptionDownstream_noVendorSuppressorInserted() {
        // Because TracedSubscriber is itself a QueueSubscription, Mono/FluxLiftFuseable's automatic
        // SuppressFuseableSubscriber branch must not fire — no double wrapping.
        Flux<Integer> source = Flux.range(1, 3);
        QueueTestFusionSubscriber<Integer> downstream = new QueueTestFusionSubscriber<Integer>();

        wrapped(source).subscribe(downstream);

        assertTrue(downstream.subscription instanceof TracedSubscriber);
        TracedSubscriber<?> traced = (TracedSubscriber<?>) downstream.subscription;
        assertNotNull(traced.subscription);
        assertFalse(traced.subscription.getClass().getName().contains("SuppressFuseable"));
        assertEquals(Arrays.asList(1, 2, 3), downstream.received);
    }

    @Test
    public void upstreamFusionNeverRequested() {
        final AtomicInteger upstreamFusionCalls = new AtomicInteger();
        Publisher<Integer> raw = new Publisher<Integer>() {
            @Override
            public void subscribe(final Subscriber<? super Integer> subscriber) {
                subscriber.onSubscribe(new Fuseable.QueueSubscription<Integer>() {
                    @Override
                    public int requestFusion(int requestedMode) {
                        upstreamFusionCalls.incrementAndGet();
                        return Fuseable.SYNC;
                    }

                    @Override
                    public void request(long n) {
                        subscriber.onNext(42);
                        subscriber.onComplete();
                    }

                    @Override
                    public void cancel() {
                    }

                    @Override
                    public Integer poll() {
                        return null;
                    }

                    @Override
                    public int size() {
                        return 0;
                    }

                    @Override
                    public boolean isEmpty() {
                        return true;
                    }

                    @Override
                    public void clear() {
                    }
                });
            }
        };
        TestFusionSubscriber<Integer> downstream = new TestFusionSubscriber<Integer>();

        wrapped(Flux.from(raw)).subscribe(downstream);

        assertEquals(0, upstreamFusionCalls.get(), "TracedSubscriber must not open the fused path upstream");
        assertEquals(Arrays.asList(42), downstream.received);
        assertTrue(downstream.completed);
    }

    @Test
    public void windowArithmetic_onePerSignal() {
        TestFusionSubscriber<Integer> downstream = new TestFusionSubscriber<Integer>();

        wrapped(Flux.range(1, 5)).subscribe(downstream);

        // 7 delivery windows (onSubscribe + 5 x onNext + onComplete): the trace is created ONCE
        // and rebound for the remaining 6 - the per-signal create/close churn is gone. The
        // downstream's request() adds one control window (frame-local creation: the mock has no
        // real thread-local, so it does not observe the enclosing delivery window).
        verify(asyncContext, times(2)).continueAsyncTraceObject(false);
        verify(asyncContext, times(6)).continueAsyncTraceObject(trace);
        verify(scope, times(8)).tryEnter();
        verify(scope, times(8)).leave();
        verify(trace, never()).close();
        // delivery windows share one cached scope lookup; the control window adds its own
        // (uncached) enter/leave lookups.
        verify(trace, times(3)).getScope(ScopeUtils.ASYNC_TRACE_SCOPE);
    }

    @Test
    public void errorSignal_deliveredInsideWindow() {
        Flux<Integer> source = Flux.range(1, 3).concatWith(Flux.error(new IllegalStateException("boom")));
        TestFusionSubscriber<Integer> downstream = new TestFusionSubscriber<Integer>();

        wrapped(source).subscribe(downstream);

        assertEquals(Arrays.asList(1, 2, 3), downstream.received);
        assertNotNull(downstream.error);
        // 5 delivery windows (1 creation + 4 rebinds) + 1 control window (request)
        verify(asyncContext, times(2)).continueAsyncTraceObject(false);
        verify(asyncContext, times(4)).continueAsyncTraceObject(trace);
        verify(scope, times(6)).tryEnter();
        verify(scope, times(6)).leave();
    }

    @Test
    public void terminalCleanup_whenAsyncScopeEnds() {
        // faithful thread-local/scope-depth simulation: the synchronous source delivers every
        // signal NESTED inside the onSubscribe frame (emission happens during request), so the
        // outermost frame end is the one that unbinds and - because the stream is done - closes
        // the held trace, exactly once.
        final Trace realTrace = mock(Trace.class);
        when(realTrace.getScope(ScopeUtils.ASYNC_TRACE_SCOPE)).thenReturn(new DepthScope());
        when(realTrace.isAsync()).thenReturn(true);
        final StubAsyncContext simulatedContext = new StubAsyncContext(new Trace[1], realTrace);
        TestFusionSubscriber<Integer> downstream = new TestFusionSubscriber<Integer>();

        // note: a 1-element range collapses to FluxJust, a ScalarCallable the wrapper skips.
        @SuppressWarnings("unchecked")
        Flux<Integer> wrapped = (Flux<Integer>) SeamPublisherWrapper.wrap(Flux.range(1, 2), simulatedContext);
        wrapped.subscribe(downstream);

        assertTrue(downstream.completed);
        assertEquals(1, simulatedContext.creations);
        assertEquals(1, simulatedContext.unbinds);
        verify(realTrace, times(1)).close();
    }

    @Test
    public void cancel_closesHeldTraceImmediately_lateSignalCleanedUp() {
        when(trace.isAsync()).thenReturn(true);
        when(scope.isActive()).thenReturn(false);
        TestFusionSubscriber<Integer> downstream = new TestFusionSubscriber<Integer>();
        TracedSubscriber<Integer> traced = new TracedSubscriber<Integer>(downstream, asyncContext);

        traced.onSubscribe(mock(Subscription.class));
        // the downstream's request(MAX) inside onSubscribe opened one control window whose
        // frame-local trace (same mock instance) was closed; the held delivery trace was not.
        verify(trace, times(1)).close();

        traced.cancel();
        // no delivery frame is using the held trace, so cancel closes it right away instead of
        // waiting for a signal that may never come (+1), and its control window adds its
        // frame-local close (+1, same mock instance).
        verify(trace, times(3)).close();

        traced.onNext(1);
        // a racing late signal is legal: it gets a fresh trace which its own frame end closes
        // because cancelled is set - nothing waits for GC.
        verify(trace, times(4)).close();

        // see TracedSubscriberCancelLifecycleTest for the per-path close-once assertions.
    }

    @Test
    public void borrowedTrace_notHeldAndNotUnbound() {
        // a trace bound by another activation is used for the window but never held or unbound here.
        Trace borrowed = mock(Trace.class);
        TraceScope borrowedScope = mock(TraceScope.class);
        when(asyncContext.currentAsyncTraceObject()).thenReturn(borrowed);
        when(borrowed.getScope(ScopeUtils.ASYNC_TRACE_SCOPE)).thenReturn(borrowedScope);
        when(borrowedScope.tryEnter()).thenReturn(true);
        when(borrowedScope.canLeave()).thenReturn(true);
        when(borrowedScope.isActive()).thenReturn(true);
        TestFusionSubscriber<Integer> downstream = new TestFusionSubscriber<Integer>();

        wrapped(Flux.range(1, 2)).subscribe(downstream);

        assertEquals(Arrays.asList(1, 2), downstream.received);
        verify(asyncContext, never()).continueAsyncTraceObject(false);
        verify(asyncContext, never()).continueAsyncTraceObject(borrowed);
        verify(asyncContext, never()).close();
        verify(borrowed, never()).close();
        // 4 delivery windows + 1 control window (request), all borrowed
        verify(borrowedScope, times(5)).tryEnter();
        verify(borrowedScope, times(5)).leave();
    }

    @Test
    public void duplicateOnSubscribe_cancelsTheSecond() {
        Subscription first = mock(Subscription.class);
        Subscription second = mock(Subscription.class);
        TestFusionSubscriber<Integer> downstream = new TestFusionSubscriber<Integer>();
        TracedSubscriber<Integer> traced = new TracedSubscriber<Integer>(downstream, asyncContext);

        traced.onSubscribe(first);
        Subscription seenByDownstream = downstream.subscription;
        traced.onSubscribe(second);

        // reactive streams 2.5: the duplicate is cancelled and nothing is re-delivered downstream.
        verify(second, times(1)).cancel();
        verify(first, never()).cancel();
        assertTrue(seenByDownstream == downstream.subscription, "downstream must not be re-subscribed");
    }

    @Test
    public void signalsAfterTerminal_droppedWithoutWindow() {
        TestFusionSubscriber<Integer> downstream = new TestFusionSubscriber<Integer>() {
            @Override
            public void onSubscribe(Subscription s) {
                this.subscription = s;
                // no request: signals are pushed manually below
            }
        };
        TracedSubscriber<Integer> traced = new TracedSubscriber<Integer>(downstream, asyncContext);
        traced.onSubscribe(mock(Subscription.class));
        traced.onComplete();

        traced.onNext(1);
        traced.onError(new IllegalStateException("late"));
        traced.onComplete();

        assertTrue(downstream.completed);
        assertTrue(downstream.received.isEmpty(), "late onNext must be dropped");
        assertTrue(downstream.error == null, "late onError must be dropped");
        // windows: onSubscribe (creation) + first onComplete (rebind) only - dropped signals open none.
        verify(asyncContext, times(1)).continueAsyncTraceObject(false);
        verify(asyncContext, times(1)).continueAsyncTraceObject(trace);
    }

    @Test
    public void conditionalDownstream_getsConditionalVariantAndTryOnNextWindow() {
        // lifter selection: a conditional downstream gets the Conditional variant...
        ConditionalTestSubscriber<Integer> downstream = new ConditionalTestSubscriber<Integer>();
        Object wrapped = SeamPublisherWrapper.wrap(Flux.range(1, 5).map(i -> i), asyncContext);
        ((Flux<Integer>) wrapped).subscribe((CoreSubscriber<Integer>) downstream);
        assertTrue(downstream.subscription instanceof Fuseable.ConditionalSubscriber,
                "a conditional downstream must get the conditional traced variant");
        assertEquals(Arrays.asList(1, 2, 3, 4, 5), downstream.received);
        assertTrue(downstream.completed);

        // ...and tryOnNext is delivered inside a window.
        ConditionalTestSubscriber<Integer> direct = new ConditionalTestSubscriber<Integer>();
        TracedSubscriber.Conditional<Integer> traced = new TracedSubscriber.Conditional<Integer>(direct, asyncContext);
        traced.onSubscribe(mock(Subscription.class));
        int windowsBefore = mockingDetails(asyncContext).getInvocations().size();
        assertTrue(traced.tryOnNext(42));
        assertEquals(Arrays.asList(42), direct.received);
        assertTrue(mockingDetails(asyncContext).getInvocations().size() > windowsBefore,
                "tryOnNext must interact with the window machinery");
    }

    @Test
    public void requestOnBareThread_opensAndFullyClosesFrameLocalWindow() {
        // request may race delivery signals, so it must never bind the held trace - on a bare
        // thread it creates a frame-local trace and fully closes it at the frame end.
        when(trace.isAsync()).thenReturn(true);
        when(scope.isActive()).thenReturn(false);
        Subscription upstream = mock(Subscription.class);
        TestFusionSubscriber<Integer> downstream = new TestFusionSubscriber<Integer>();
        TracedSubscriber<Integer> traced = new TracedSubscriber<Integer>(downstream, asyncContext);
        traced.subscription = upstream;

        traced.request(7);

        verify(upstream).request(7);
        verify(asyncContext, times(1)).continueAsyncTraceObject(false);
        verify(asyncContext, times(1)).close();
        verify(trace, times(1)).close();
    }

    @Test
    public void requestInsideExistingWindow_borrowsWithoutBinding() {
        Trace borrowed = mock(Trace.class);
        TraceScope borrowedScope = mock(TraceScope.class);
        when(asyncContext.currentAsyncTraceObject()).thenReturn(borrowed);
        when(borrowed.getScope(ScopeUtils.ASYNC_TRACE_SCOPE)).thenReturn(borrowedScope);
        when(borrowedScope.canLeave()).thenReturn(true);
        Subscription upstream = mock(Subscription.class);
        TestFusionSubscriber<Integer> downstream = new TestFusionSubscriber<Integer>();
        TracedSubscriber<Integer> traced = new TracedSubscriber<Integer>(downstream, asyncContext);
        traced.subscription = upstream;

        traced.request(3);

        verify(upstream).request(3);
        verify(asyncContext, never()).continueAsyncTraceObject(false);
        verify(asyncContext, never()).close();
        verify(borrowed, never()).close();
        verify(borrowedScope, times(1)).tryEnter();
        verify(borrowedScope, times(1)).leave();
    }

    @Test
    public void windowOpenFailureAfterBind_unbindsTheThreadAndClosesCreatedTrace() {
        // if the window fails AFTER the trace was bound (e.g. the scope lookup throws), the
        // binding this frame installed must be undone - it must not leak onto the delivery
        // thread - and the trace this frame created must be closed: had the failing signal been
        // terminal, no later frame would ever close it (GC-dependent leak otherwise).
        when(trace.getScope(ScopeUtils.ASYNC_TRACE_SCOPE)).thenThrow(new RuntimeException("scope failure"));
        TestFusionSubscriber<Integer> downstream = new TestFusionSubscriber<Integer>();

        wrapped(Flux.range(1, 3)).subscribe(downstream);

        assertEquals(Arrays.asList(1, 2, 3), downstream.received);
        assertTrue(downstream.completed);
        // 5 delivery windows + 1 control window (request) each bind, fail on the scope lookup,
        // unbind, and close the trace they created.
        verify(asyncContext, times(6)).close();
        verify(trace, times(6)).close();
    }

    @Test
    public void windowFailure_doesNotAffectDelivery() {
        when(asyncContext.continueAsyncTraceObject(anyBoolean())).thenThrow(new RuntimeException("window failure"));
        TestFusionSubscriber<Integer> downstream = new TestFusionSubscriber<Integer>();

        wrapped(Flux.range(1, 3)).subscribe(downstream);

        assertEquals(Arrays.asList(1, 2, 3), downstream.received);
        assertTrue(downstream.completed);
    }

    @Test
    public void nullAsyncContext_deliversWithoutWindow() {
        TestFusionSubscriber<Integer> downstream = new TestFusionSubscriber<Integer>();
        TracedSubscriber<Integer> traced = new TracedSubscriber<Integer>(downstream, null);

        Flux.range(1, 3).subscribe((CoreSubscriber<Integer>) traced);

        assertEquals(Arrays.asList(1, 2, 3), downstream.received);
        assertTrue(downstream.completed);
    }

    @Test
    public void downstreamOnNextThrows_windowStillClosed() {
        final AtomicInteger enters = new AtomicInteger();
        final AtomicInteger leaves = new AtomicInteger();
        when(scope.tryEnter()).thenAnswer(invocation -> {
            enters.incrementAndGet();
            return true;
        });
        when(scope.canLeave()).thenAnswer(invocation -> {
            leaves.incrementAndGet();
            return true;
        });

        TestFusionSubscriber<Integer> downstream = new TestFusionSubscriber<Integer>() {
            @Override
            public void onNext(Integer value) {
                throw new IllegalStateException("downstream failure");
            }
        };

        try {
            wrapped(Flux.range(1, 3)).subscribe(downstream);
        } catch (Throwable ignored) {
        }

        assertTrue(enters.get() >= 2, "expected at least the onSubscribe and onNext windows");
        assertEquals(enters.get(), leaves.get(), "every opened window must be closed");
    }

    @Test
    public void requestAndCancel_delegatedUpstream() {
        final Subscription upstream = mock(Subscription.class);
        Publisher<Integer> raw = new Publisher<Integer>() {
            @Override
            public void subscribe(Subscriber<? super Integer> subscriber) {
                subscriber.onSubscribe(upstream);
            }
        };
        TestFusionSubscriber<Integer> downstream = new TestFusionSubscriber<Integer>() {
            @Override
            public void onSubscribe(Subscription s) {
                this.subscription = s;
                s.request(7);
                s.cancel();
            }
        };

        wrapped(Flux.from(raw)).subscribe(downstream);

        verify(upstream).request(7);
        verify(upstream).cancel();
    }

    static class ConditionalTestSubscriber<T> extends TestFusionSubscriber<T> implements Fuseable.ConditionalSubscriber<T> {
        @Override
        public boolean tryOnNext(T t) {
            received.add(t);
            return true;
        }
    }

    static class QueueTestFusionSubscriber<T> extends TestFusionSubscriber<T> implements Fuseable.QueueSubscription<T> {
        @Override
        public int requestFusion(int requestedMode) {
            return Fuseable.NONE;
        }

        @Override
        public void request(long n) {
        }

        @Override
        public void cancel() {
        }

        @Override
        public T poll() {
            return null;
        }

        @Override
        public int size() {
            return 0;
        }

        @Override
        public boolean isEmpty() {
            return true;
        }

        @Override
        public void clear() {
        }
    }
}
