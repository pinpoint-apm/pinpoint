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
import com.navercorp.pinpoint.bootstrap.logging.PluginLogManager;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogger;
import com.navercorp.pinpoint.bootstrap.util.ScopeUtils;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.Fuseable;
import reactor.util.context.Context;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Sits between a wrapped publisher and the application subscriber ({@code actual}) so that every
 * signal is delivered inside a trace window bound to the seam's {@link AsyncContext} — whichever
 * thread delivers it. The window is the same restore-only flow the reactor plugin's
 * {@code CoreSubscriberOnNextInterceptor} uses ({@code continueAsyncTraceObject(false)} + async
 * trace scope, no per-signal span event).
 * <p>
 * Fusion suppressor (same shape as reactor's {@code FluxHide.SuppressFuseableSubscriber}): this
 * class implements {@link Fuseable.QueueSubscription}, passes itself downstream in
 * {@link #onSubscribe}, and answers every {@link #requestFusion} with {@link Fuseable#NONE}. The
 * downstream can never enter fused mode and this class never requests fusion upstream, so the
 * pull-only (poll) path that would bypass the {@link #onNext} window is cut in both directions.
 * Being a {@code QueueSubscription} also keeps {@code Operators.lift} from inserting its own
 * suppressor, and covers reactor 3.1.x where lift has no automatic suppressor at all.
 */
public class TracedSubscriber<T> implements CoreSubscriber<T>, Fuseable.QueueSubscription<T> {
    private final PluginLogger logger = PluginLogManager.getLogger(getClass());

    private final CoreSubscriber<? super T> actual;
    private final AsyncContext asyncContext;
    // package-private for tests: also the value handed downstream in onSubscribe is `this`.
    Subscription subscription;
    // reactive streams defense: signals after a terminal one are dropped (serial delivery, so a
    // plain flag is enough). cancel() intentionally does not set this - racing signals after
    // cancel are legal and still delivered.
    private boolean done;
    // last (trace, scope) pair; on high-cardinality streams the per-signal trace.getScope()
    // hash lookup was the window's dominant cost. Signals are delivered serially (reactive
    // streams spec) and the holder's final fields keep the pair consistent even under a data
    // race, so a plain reference is enough - a stale read just falls back to a fresh lookup.
    private ScopeCache scopeCache;
    // the async trace this subscription owns. Created by the first owned window and REBOUND per
    // signal instead of the create/close cycle per signal (the dominant high-cardinality cost:
    // LocalAsyncId + ChildTrace + recorders + scope registration each time). The thread binding
    // stays signal-scoped - only the trace object's lifetime is subscription-scoped. Closed at
    // the terminal signal, or on cancel by whoever wins the heldTraceState handoff below.
    // volatile: the cancel thread closes it after winning the CAS.
    private volatile Trace heldTrace;
    // cancel() may race an in-flight signal on the delivery thread (reactive streams 2.7
    // serializes request/cancel only with each other), so the held trace has an explicit owner
    // state instead of a GC fallback:
    //   IDLE   - no delivery window is using the held trace
    //   WINDOW - a delivery frame owns it (bound to the delivery thread)
    //   DONE   - the trace has been closed; late (racing) signals get a frame-local trace
    // Delivery claims IDLE|DONE -> WINDOW before touching heldTrace, so cancel can never close a
    // trace mid-rebind. Every transition to DONE is a CAS or happens while cancel is fenced out
    // (state==WINDOW), so the trace is closed exactly once.
    private static final int IDLE = 0;
    private static final int WINDOW = 1;
    private static final int DONE = 2;
    private final AtomicInteger heldTraceState = new AtomicInteger(IDLE);
    // Dekker pair with heldTraceState: cancel writes cancelled then CASes IDLE->DONE; the
    // delivery side releases WINDOW->IDLE then rechecks cancelled - at least one side sees the
    // other, so a cancel arriving during a window is never lost.
    private volatile boolean cancelled;

    public TracedSubscriber(CoreSubscriber<? super T> actual, AsyncContext asyncContext) {
        this.actual = Objects.requireNonNull(actual, "actual");
        this.asyncContext = asyncContext;
    }

    @Override
    public void onSubscribe(Subscription s) {
        if (this.subscription != null) {
            // reactive streams 2.5: cancel the duplicate; downstream already has `this`.
            s.cancel();
            if (logger.isWarnEnabled()) {
                logger.warn("Duplicate onSubscribe cancelled. subscription={}", s);
            }
            return;
        }
        this.subscription = s;
        final Trace trace = openWindow();
        try {
            actual.onSubscribe(this);
        } finally {
            closeWindow(trace, false);
        }
    }

    @Override
    public void onNext(T t) {
        if (done) {
            droppedSignal("onNext", t);
            return;
        }
        final Trace trace = openWindow();
        try {
            actual.onNext(t);
        } finally {
            closeWindow(trace, false);
        }
    }

    @Override
    public void onError(Throwable throwable) {
        if (done) {
            droppedSignal("onError", throwable);
            return;
        }
        this.done = true;
        final Trace trace = openWindow();
        try {
            actual.onError(throwable);
        } finally {
            closeWindow(trace, true);
        }
    }

    @Override
    public void onComplete() {
        if (done) {
            droppedSignal("onComplete", null);
            return;
        }
        this.done = true;
        final Trace trace = openWindow();
        try {
            actual.onComplete();
        } finally {
            closeWindow(trace, true);
        }
    }

    boolean isDone() {
        return done;
    }

    void droppedSignal(final String signal, final Object value) {
        // a signal after a terminal one violates the spec; downstream reactor operators guard
        // against this themselves, so dropping here matches what they would do anyway.
        if (logger.isDebugEnabled()) {
            logger.debug("Dropped {} after a terminal signal. value={}", signal, value);
        }
    }

    @Override
    public Context currentContext() {
        // mandatory delegation: the application's Reactor Context must flow through unchanged.
        return actual.currentContext();
    }

    @Override
    public void request(long n) {
        final ControlWindow window = openControlWindow();
        try {
            subscription.request(n);
        } finally {
            closeControlWindow(window);
        }
    }

    @Override
    public void cancel() {
        this.cancelled = true;
        // no delivery window is using the held trace -> close it here; a common cancel (e.g.
        // take(1)) arrives inside the onNext frame instead, and that frame's closeWindow sees
        // cancelled and closes. Either way the trace no longer waits for a signal that may never
        // come.
        closeHeldTraceIfIdle();
        final ControlWindow window = openControlWindow();
        try {
            subscription.cancel();
        } finally {
            closeControlWindow(window);
        }
    }

    /**
     * Window for request()/cancel(): upstream may run synchronous work in these frames (e.g. a
     * driver submitting the query on demand), so they deserve a trace window too. Unlike delivery
     * signals they may run CONCURRENTLY with them (reactive streams 2.7 serializes request/cancel
     * only with each other), so the subscription-held trace must never be bound here - a trace
     * already on this thread is borrowed, otherwise a frame-local trace is created and fully
     * closed at the frame end.
     */
    private ControlWindow openControlWindow() {
        final AsyncContext asyncContext = this.asyncContext;
        if (asyncContext == null) {
            return null;
        }
        boolean created = false;
        Trace trace = null;
        try {
            trace = asyncContext.currentAsyncTraceObject();
            if (trace == null) {
                trace = asyncContext.continueAsyncTraceObject(false);
                if (trace == null) {
                    return null;
                }
                created = true;
            }
            ScopeUtils.entryAsyncTraceScope(trace);
            return new ControlWindow(trace, created);
        } catch (Throwable th) {
            if (logger.isWarnEnabled()) {
                logger.warn("Failed to open control window. Caused:{}", th.getMessage(), th);
            }
            if (created) {
                try {
                    asyncContext.close();
                } catch (Throwable ignored) {
                }
                // this frame owns the trace it just created - no later frame will ever see it,
                // so it must be closed here or it stays open until GC.
                try {
                    trace.close();
                } catch (Throwable ignored) {
                }
            }
            return null;
        }
    }

    private void closeControlWindow(final ControlWindow window) {
        if (window == null) {
            return;
        }
        final Trace trace = window.trace;
        try {
            if (!ScopeUtils.leaveAsyncTraceScope(trace)) {
                if (logger.isWarnEnabled()) {
                    logger.warn("Failed to leave scope of control trace {}.", trace);
                }
                endControlWindow(window);
                return;
            }
            if (ScopeUtils.isAsyncTraceEndScope(trace)) {
                endControlWindow(window);
            }
        } catch (Throwable th) {
            if (logger.isWarnEnabled()) {
                logger.warn("Failed to close control window. Caused:{}", th.getMessage(), th);
            }
        }
    }

    private void endControlWindow(final ControlWindow window) {
        asyncContext.close();
        if (window.created) {
            // frame-local trace: fully closed here, the held delivery trace is untouched.
            window.trace.close();
        }
    }

    @Override
    public int requestFusion(int requestedMode) {
        return Fuseable.NONE;
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

    Trace openWindow() {
        final AsyncContext asyncContext = this.asyncContext;
        if (asyncContext == null) {
            return null;
        }
        boolean boundHere = false;
        boolean claimed = false;
        int priorState = IDLE;
        Trace createdHere = null;
        try {
            Trace trace = asyncContext.currentAsyncTraceObject();
            if (trace == null) {
                // no trace on this thread: bind ours. Only a trace this subscription created is
                // held for reuse - a borrowed (nested) trace above belongs to another activation.
                // Claim the held trace BEFORE reading it, so a concurrent cancel cannot close it
                // between the read and the rebind. After DONE (cancel already closed it) the
                // held reference is null and this frame gets a fresh trace, which the frame end
                // closes again because cancelled is set.
                priorState = claimHeldTrace();
                claimed = true;
                final Trace held = this.heldTrace;
                if (held == null) {
                    trace = asyncContext.continueAsyncTraceObject(false);
                    if (trace == null) {
                        releaseClaim(priorState);
                        return null;
                    }
                    this.heldTrace = trace;
                    createdHere = trace;
                } else {
                    trace = asyncContext.continueAsyncTraceObject(held);
                    if (trace == null) {
                        releaseClaim(priorState);
                        return null;
                    }
                }
                boundHere = true;
            }
            // same flow as ScopeUtils.entryAsyncTraceScope, with the scope lookup cached.
            final TraceScope scope = scopeOf(trace);
            if (scope != null) {
                scope.tryEnter();
            }
            return trace;
        } catch (Throwable th) {
            // failure isolation: signal delivery must not depend on the window.
            if (logger.isWarnEnabled()) {
                logger.warn("Failed to open trace window. Caused:{}", th.getMessage(), th);
            }
            if (boundHere) {
                // undo the thread binding this frame installed - an agent failure must not
                // leave the trace bound to a shared delivery thread.
                try {
                    asyncContext.close();
                } catch (Throwable ignored) {
                }
            }
            if (createdHere != null) {
                // undo the hold too: if this signal was terminal no later frame exists, so a
                // trace left in the slot could never be closed again (GC-dependent leak).
                this.heldTrace = null;
                try {
                    createdHere.close();
                } catch (Throwable ignored) {
                }
            }
            if (claimed) {
                releaseClaim(priorState);
            }
            return null;
        }
    }

    /**
     * Claims the held trace for this delivery frame: {@code IDLE|DONE -> WINDOW}. While in
     * {@code WINDOW}, {@link #closeHeldTraceIfIdle()} (the cancel side) can never win its CAS.
     * Signals are delivered serially, so only one frame ever claims at a time.
     */
    private int claimHeldTrace() {
        for (; ; ) {
            final int state = heldTraceState.get();
            if (heldTraceState.compareAndSet(state, WINDOW)) {
                return state;
            }
        }
    }

    /**
     * Reverts a claim whose window never materialized (bind failure): restores the prior state
     * and, like the normal release in {@link #endWindow}, rechecks a cancel that arrived while
     * the claim fenced it out.
     */
    private void releaseClaim(final int priorState) {
        if (priorState == DONE) {
            heldTraceState.set(DONE);
            return;
        }
        heldTraceState.set(IDLE);
        if (cancelled) {
            closeHeldTraceIfIdle();
        }
    }

    /**
     * The single place the held trace is closed off the delivery frame: transitions
     * {@code IDLE -> DONE} and closes on success. Losing the CAS means either a delivery frame
     * owns the trace right now (its window end will observe {@code cancelled} and close) or the
     * trace is already {@code DONE} - in both cases closing here would be a double close.
     */
    private void closeHeldTraceIfIdle() {
        if (heldTraceState.compareAndSet(IDLE, DONE)) {
            final Trace held = this.heldTrace;
            this.heldTrace = null;
            if (held != null) {
                try {
                    held.close();
                } catch (Throwable th) {
                    // cleanup failure must never leak into the caller: cancel() still has to
                    // cancel the upstream subscription after this returns.
                    if (logger.isWarnEnabled()) {
                        logger.warn("Failed to close held trace. Caused:{}", th.getMessage(), th);
                    }
                }
            }
        }
    }

    void closeWindow(final Trace trace, final boolean terminal) {
        if (trace == null) {
            return;
        }
        try {
            // same flow as ScopeUtils.leaveAsyncTraceScope/isAsyncTraceEndScope, scope lookup cached.
            final TraceScope scope = scopeOf(trace);
            if (scope != null) {
                if (scope.canLeave()) {
                    scope.leave();
                } else {
                    if (logger.isWarnEnabled()) {
                        logger.warn("Failed to leave scope of async trace {}.", trace);
                    }
                    endWindow(trace, true);
                    return;
                }
            }
            if (scope != null && !scope.isActive() && trace.isAsync()) {
                // `done` matters when the terminal signal was delivered NESTED inside an outer
                // frame (synchronous sources emit during request within onSubscribe): the nested
                // terminal frame skips endWindow because the scope is still active, so the
                // outermost frame - whose own `terminal` flag is false - must close on its
                // behalf or the held trace would wait for a signal that never comes.
                endWindow(trace, terminal || cancelled || done);
            }
        } catch (Throwable th) {
            if (logger.isWarnEnabled()) {
                logger.warn("Failed to close trace window. Caused:{}", th.getMessage(), th);
            }
        }
    }

    /**
     * Ends the outermost window on this thread: always unbinds the thread, but closes the trace
     * object only at a terminal signal (or after cancel) - between signals the held trace stays
     * open for rebinding.
     */
    private void endWindow(final Trace trace, final boolean closeTrace) {
        asyncContext.close();
        if (closeTrace) {
            // drop the reference so a late (spec-violating) signal creates a fresh trace
            // instead of rebinding a closed one. cancel is fenced out (state is WINDOW), so
            // this close cannot race the cancel-side close.
            this.heldTrace = null;
            heldTraceState.set(DONE);
            trace.close();
        } else {
            // release, then recheck: a cancel that arrived during this window lost its CAS and
            // is waiting for us - without this the subscription could end (no further signals)
            // with the trace still open.
            heldTraceState.set(IDLE);
            if (cancelled) {
                closeHeldTraceIfIdle();
            }
        }
    }

    private TraceScope scopeOf(final Trace trace) {
        final ScopeCache cached = this.scopeCache;
        if (cached != null && cached.trace == trace) {
            return cached.scope;
        }
        final TraceScope scope = trace.getScope(ScopeUtils.ASYNC_TRACE_SCOPE);
        if (scope != null) {
            // a null scope is not cached: it may be added to the trace later.
            this.scopeCache = new ScopeCache(trace, scope);
        }
        return scope;
    }

    /**
     * Variant chosen when the downstream is a {@link Fuseable.ConditionalSubscriber}
     * (filter/distinct style chains): implementing it here preserves the upstream's
     * {@code tryOnNext} optimization - a plain middle subscriber would force every conditional
     * source down the request-refill path.
     */
    static final class Conditional<T> extends TracedSubscriber<T> implements Fuseable.ConditionalSubscriber<T> {

        private final Fuseable.ConditionalSubscriber<? super T> actualConditional;

        Conditional(Fuseable.ConditionalSubscriber<? super T> actual, AsyncContext asyncContext) {
            super(actual, asyncContext);
            this.actualConditional = actual;
        }

        @Override
        public boolean tryOnNext(T t) {
            if (isDone()) {
                droppedSignal("tryOnNext", t);
                // consumed: the stream is over, no replenish is wanted.
                return true;
            }
            final Trace trace = openWindow();
            try {
                return actualConditional.tryOnNext(t);
            } finally {
                closeWindow(trace, false);
            }
        }
    }

    private static final class ControlWindow {
        final Trace trace;
        final boolean created;

        ControlWindow(Trace trace, boolean created) {
            this.trace = trace;
            this.created = created;
        }
    }

    private static final class ScopeCache {
        final Trace trace;
        final TraceScope scope;

        ScopeCache(Trace trace, TraceScope scope) {
            this.trace = trace;
            this.scope = scope;
        }
    }
}
