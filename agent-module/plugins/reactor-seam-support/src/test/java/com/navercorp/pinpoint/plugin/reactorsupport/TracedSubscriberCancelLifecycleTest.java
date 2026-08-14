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
import reactor.util.context.Context;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Held-trace lifecycle around cancel(). The contract under test: the subscription-held trace is
 * closed <b>exactly once</b> on every cancel path — including a cancel after which no signal ever
 * arrives (previously that path relied on GC) — and never while a delivery frame is using it.
 * <p>
 * Mock geography: {@code held} is the trace the first owned window creates and holds;
 * {@code ctrl} is what request/cancel control windows create in mock-land (no real thread-local),
 * kept scope-less and non-async so it never contributes to close() counts.
 */
public class TracedSubscriberCancelLifecycleTest {

    private AsyncContext asyncContext;
    private Trace held;
    private TraceScope heldScope;
    private Trace ctrl;
    private Subscription subscription;

    @BeforeEach
    public void setUp() {
        asyncContext = mock(AsyncContext.class);
        held = mock(Trace.class);
        heldScope = mock(TraceScope.class);
        ctrl = mock(Trace.class);
        subscription = mock(Subscription.class);

        // first owned window creates the held trace; control windows get the inert ctrl trace.
        when(asyncContext.continueAsyncTraceObject(false)).thenReturn(held, ctrl, ctrl, ctrl);
        when(asyncContext.continueAsyncTraceObject(held)).thenReturn(held);
        when(held.getScope(ScopeUtils.ASYNC_TRACE_SCOPE)).thenReturn(heldScope);
        when(held.isAsync()).thenReturn(true);
        when(heldScope.tryEnter()).thenReturn(true);
        when(heldScope.canLeave()).thenReturn(true);
        // every window is outermost: it unbinds at the frame end.
        when(heldScope.isActive()).thenReturn(false);
        // ctrl stays scope-less and non-async: control windows never close it.
        when(ctrl.getScope(ScopeUtils.ASYNC_TRACE_SCOPE)).thenReturn(null);
        when(ctrl.isAsync()).thenReturn(false);
    }

    private TracedSubscriber<Integer> traced(CoreSubscriber<Integer> actual) {
        return new TracedSubscriber<Integer>(actual, asyncContext);
    }

    @Test
    public void cancelWithNoSignalInFlight_closesHeldTraceExactlyOnce() {
        TracedSubscriber<Integer> traced = traced(new NoopSubscriber());

        traced.onSubscribe(subscription);
        traced.onNext(1);
        verify(held, never()).close();

        traced.cancel();
        verify(held, times(1)).close();
        verify(subscription).cancel();

        // second cancel: the state is DONE, nothing closes twice.
        traced.cancel();
        verify(held, times(1)).close();
        verify(ctrl, never()).close();
    }

    @Test
    public void cancelRightAfterOnSubscribe_closesHeldTraceExactlyOnce() {
        TracedSubscriber<Integer> traced = traced(new NoopSubscriber());

        traced.onSubscribe(subscription);
        verify(held, never()).close();

        traced.cancel();
        verify(held, times(1)).close();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void cancelInsideDeliveryFrame_closedAtFrameEnd_notByCancel() {
        // take(1) shape: the downstream cancels synchronously inside onNext. The frame owns the
        // held trace (state WINDOW), so cancel's own close must lose and the frame end closes.
        final TracedSubscriber<Integer>[] ref = new TracedSubscriber[1];
        TracedSubscriber<Integer> traced = traced(new NoopSubscriber() {
            @Override
            public void onNext(Integer value) {
                ref[0].cancel();
                // still inside the frame: cancel must not have closed the trace in use.
                verify(held, never()).close();
            }
        });
        ref[0] = traced;
        // cancel's control window borrows the delivery window's bound trace here.
        when(asyncContext.currentAsyncTraceObject()).thenReturn(null, null, held);
        // onSubscribe close -> outermost(false); control window close -> still active(true);
        // onNext close -> outermost(false).
        when(heldScope.isActive()).thenReturn(false, true, false);

        traced.onSubscribe(subscription);
        traced.onNext(1);

        verify(held, times(1)).close();
        verify(subscription).cancel();
    }

    @Test
    public void cancelBeforeAnySignal_lateRacingSignalIsStillCleanedUp() {
        // legal per reactive streams: a signal may still arrive after cancel. The fresh trace the
        // late frame creates must be closed at its own frame end because cancelled is set.
        when(asyncContext.continueAsyncTraceObject(false)).thenReturn(ctrl, held);
        TracedSubscriber<Integer> traced = traced(new NoopSubscriber());
        traced.subscription = subscription;

        traced.cancel();                    // nothing held yet - nothing to close
        verify(held, never()).close();
        verify(ctrl, never()).close();

        traced.onNext(42);                  // racing late signal: fresh trace, closed at frame end
        verify(held, times(1)).close();
    }

    @Test
    public void concurrentCancel_waitsForDeliveryWindowToReleaseHeldTrace() throws Exception {
        CountDownLatch deliveryEntered = new CountDownLatch(1);
        CountDownLatch releaseDelivery = new CountDownLatch(1);
        AtomicReference<Throwable> deliveryFailure = new AtomicReference<>();
        TracedSubscriber<Integer> traced = traced(new NoopSubscriber() {
            @Override
            public void onNext(Integer value) {
                deliveryEntered.countDown();
                try {
                    releaseDelivery.await(5, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
        traced.onSubscribe(subscription);

        Thread delivery = new Thread(() -> {
            try {
                traced.onNext(1);
            } catch (Throwable th) {
                deliveryFailure.set(th);
            }
        }, "traced-subscriber-delivery");
        delivery.start();

        assertTrue(deliveryEntered.await(5, TimeUnit.SECONDS), "delivery window did not open");
        traced.cancel();
        // cancel loses IDLE->DONE while the delivery frame owns WINDOW. Closing here would let
        // the downstream keep using a trace that another thread has already closed.
        verify(held, never()).close();

        releaseDelivery.countDown();
        delivery.join(5000);
        assertFalse(delivery.isAlive(), "delivery thread did not finish");
        assertNull(deliveryFailure.get());
        verify(held, times(1)).close();
        verify(subscription).cancel();
    }

    @Test
    public void cancelStillCancelsUpstreamWhenHeldTraceCloseFails() {
        // fault injection: the held trace refuses to close. The agent-side cleanup failure must
        // never block the application's reactive cancellation.
        org.mockito.Mockito.doThrow(new RuntimeException("close failure")).when(held).close();
        TracedSubscriber<Integer> traced = traced(new NoopSubscriber());

        traced.onSubscribe(subscription);
        traced.onNext(1);

        traced.cancel();
        verify(subscription, times(1)).cancel();

        // the slot is DONE: a second cancel never retries the failing close.
        traced.cancel();
        verify(held, times(1)).close();
        verify(subscription, times(2)).cancel();
    }

    @Test
    public void deliveryUnbindFailureStillClosesTerminalTraceExactlyOnce() {
        doThrow(new RuntimeException("unbind failure")).when(asyncContext).close();
        TracedSubscriber<Integer> traced = traced(new NoopSubscriber());

        assertDoesNotThrow(() -> traced.onSubscribe(subscription));
        assertDoesNotThrow(traced::onComplete);

        verify(held, times(1)).close();
        traced.cancel();
        verify(held, times(1)).close();
        verify(subscription).cancel();
    }

    @Test
    public void controlUnbindFailureStillClosesFrameLocalTrace() {
        doThrow(new RuntimeException("unbind failure")).when(asyncContext).close();
        TracedSubscriber<Integer> traced = traced(new NoopSubscriber());
        traced.subscription = subscription;

        assertDoesNotThrow(() -> traced.request(1));

        verify(subscription).request(1);
        verify(held).close();
    }

    @Test
    public void terminalWindowOpenFailure_closesCreatedTraceExactlyOnce() {
        // fault injection: the very first (and terminal) frame creates the trace, stores it in
        // the held slot, then fails on the scope lookup. The frame must take the trace back out
        // of the slot and close it - a terminal frame has no successor to do it later.
        when(held.getScope(ScopeUtils.ASYNC_TRACE_SCOPE)).thenThrow(new RuntimeException("scope failure"));
        TracedSubscriber<Integer> traced = traced(new NoopSubscriber());
        traced.subscription = subscription;

        traced.onComplete();
        verify(held, times(1)).close();
        verify(asyncContext, times(1)).close();

        // the slot was emptied: a later cancel finds nothing and never double-closes.
        traced.cancel();
        verify(held, times(1)).close();
    }

    @Test
    public void terminalThenCancel_noDoubleClose() {
        TracedSubscriber<Integer> traced = traced(new NoopSubscriber());

        traced.onSubscribe(subscription);
        traced.onNext(1);
        traced.onComplete();
        verify(held, times(1)).close();

        traced.cancel();
        verify(held, times(1)).close();
        verify(ctrl, never()).close();
    }

    private static class NoopSubscriber implements CoreSubscriber<Integer> {
        @Override
        public void onSubscribe(Subscription s) {
        }

        @Override
        public void onNext(Integer value) {
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
