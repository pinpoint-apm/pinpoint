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
import reactor.core.publisher.Flux;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Deterministic pins for NESTED seam windows — two seams wrapping the same delivery chain, the
 * shape a request produces when e.g. an r2dbc execute seam (source side) and a fetch-spec seam
 * (framework side) both wrap. The semantics under test (design policy P3: nested windows are
 * legal, the window borrows an already-bound trace):
 *
 * <ul>
 * <li><b>Same delivery thread: the seam closest to the source wins.</b> Its subscriber receives
 * every signal first, binds its trace, and the downstream seam's window finds a bound trace and
 * borrows — the downstream seam's own AsyncContext never activates on that thread (its async link
 * stays dangling, accepted policy P4).</li>
 * <li><b>Same AsyncContext wrapped twice: single activation.</b> The nested window is a scope
 * re-entry on the same trace; nothing double-binds or double-closes.</li>
 * </ul>
 *
 * The mocks simulate the thread-local contract explicitly (the outer context's
 * {@code currentAsyncTraceObject()} returns the trace the inner frame bound) — the contract
 * itself is exercised for real in the r2dbc nested-seam IT.
 */
public class NestedSeamWindowTest {

    private AsyncContext sourceSideContext;   // the seam closest to the source (e.g. execute)
    private Trace sourceSideTrace;
    private TraceScope sourceSideScope;
    private AsyncContext downstreamContext;   // the seam wrapping further down (e.g. fetch-spec)
    private Trace downstreamTrace;

    @BeforeEach
    public void setUp() {
        sourceSideContext = mock(AsyncContext.class);
        sourceSideTrace = mock(Trace.class);
        sourceSideScope = mock(TraceScope.class);
        downstreamContext = mock(AsyncContext.class);
        downstreamTrace = mock(Trace.class);

        when(sourceSideContext.continueAsyncTraceObject(false)).thenReturn(sourceSideTrace);
        when(sourceSideContext.continueAsyncTraceObject(sourceSideTrace)).thenReturn(sourceSideTrace);
        when(sourceSideTrace.getScope(ScopeUtils.ASYNC_TRACE_SCOPE)).thenReturn(sourceSideScope);
        when(sourceSideTrace.isAsync()).thenReturn(true);
        when(sourceSideScope.tryEnter()).thenReturn(true);
        when(sourceSideScope.canLeave()).thenReturn(true);
        // the delivery frames nest: only the outermost close on this trace ends the window.
        when(sourceSideScope.isActive()).thenReturn(true);

        // thread-local contract simulation: the very first window bind sees an empty thread,
        // afterwards ANY context on this thread (nested windows, control windows) sees the
        // trace the source-side frame bound.
        when(sourceSideContext.currentAsyncTraceObject()).thenReturn(null, sourceSideTrace);
        when(downstreamContext.currentAsyncTraceObject()).thenReturn(sourceSideTrace);
    }

    @SuppressWarnings("unchecked")
    private <T> Flux<T> doubleWrapped(Flux<T> source) {
        final Object inner = SeamPublisherWrapper.wrap(source, sourceSideContext);
        final Flux<T> mid = ((Flux<T>) inner).map(v -> v);
        return (Flux<T>) SeamPublisherWrapper.wrap(mid, downstreamContext);
    }

    @Test
    public void sameThreadNesting_sourceSideSeamWins_downstreamNeverActivates() {
        TestFusionSubscriber<Integer> downstream = new TestFusionSubscriber<Integer>();

        doubleWrapped(Flux.range(1, 3).hide()).subscribe(downstream);

        assertEquals(Arrays.asList(1, 2, 3), downstream.received);
        assertTrue(downstream.completed);

        // the source-side seam bound its trace for every delivery frame...
        verify(sourceSideContext, times(1)).continueAsyncTraceObject(false);
        // ...and the downstream seam only ever borrowed it: its own context never created or
        // rebound a trace on this thread — its async link intentionally stays dangling (P4).
        verify(downstreamContext, never()).continueAsyncTraceObject(any(Trace.class));
        verify(downstreamContext, never()).continueAsyncTraceObject(false);
        verify(downstreamContext, never()).close();
        verify(downstreamTrace, never()).close();
    }

    @Test
    public void sameThreadNesting_scopeArithmetic_bothFramesNestOnOneTrace() {
        TestFusionSubscriber<Integer> downstream = new TestFusionSubscriber<Integer>();

        doubleWrapped(Flux.range(1, 2).hide()).subscribe(downstream);

        // per signal both windows enter/leave the SAME trace's scope: onSubscribe + 2 x onNext +
        // onComplete = 4 signals x 2 windows = 8 enters/leaves, plus the request() control
        // windows of BOTH wrappers (each borrows the same bound trace) add 2.
        verify(sourceSideScope, times(10)).tryEnter();
        verify(sourceSideScope, times(10)).leave();
        // scope stays active in this mock (nested), so the held trace is never closed here.
        verify(sourceSideTrace, never()).close();
    }

    @Test
    public void sameContextWrappedTwice_singleActivationPerSignal() {
        // both seams got the SAME AsyncContext (e.g. a relay handed the same context to two
        // wrap points): the nested window degenerates to a scope re-entry, nothing double-binds.
        // (setUp already stubs the TL simulation: first open binds, every later open borrows.)
        final Object inner = SeamPublisherWrapper.wrap(Flux.range(1, 1).hide(), sourceSideContext);
        @SuppressWarnings("unchecked") final Flux<Integer> mid = ((Flux<Integer>) inner).map(v -> v);
        final Object outer = SeamPublisherWrapper.wrap(mid, sourceSideContext);

        TestFusionSubscriber<Integer> downstream = new TestFusionSubscriber<Integer>();
        ((Flux<Integer>) outer).subscribe(downstream);

        assertEquals(Arrays.asList(1), downstream.received);
        // one bind for the very first frame; every nested/subsequent frame borrowed.
        verify(sourceSideContext, times(1)).continueAsyncTraceObject(false);
        verify(sourceSideContext, never()).continueAsyncTraceObject(sourceSideTrace);
    }
}
