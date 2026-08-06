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

package com.navercorp.pinpoint.bootstrap.plugin.reactor;

import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.context.scope.TraceScope;
import com.navercorp.pinpoint.bootstrap.util.ScopeUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Activation arithmetic of the restore-only run/call window: the async trace scope depth decides
 * when the thread is unbound and the trace closed, so a task whose run() delegates to call()
 * (both woven) activates exactly once, and an ambient (non-async) trace is never closed here.
 */
public class SchedulerTaskRunInterceptorTest {

    private static final int API_ID = 99;
    private static final Object[] ARGS = new Object[0];

    private SchedulerTaskRunInterceptor interceptor;
    private AsyncContext asyncContext;
    private Trace trace;
    private TraceScope scope;
    private Object target;

    @BeforeEach
    public void setUp() {
        interceptor = new SchedulerTaskRunInterceptor(mock(TraceContext.class));
        asyncContext = mock(AsyncContext.class);
        trace = mock(Trace.class);
        scope = mock(TraceScope.class);
        target = new Object();

        when(asyncContext.continueAsyncTraceObject(false)).thenReturn(trace);
        when(asyncContext.currentAsyncTraceObject()).thenReturn(trace);
        when(trace.getScope(ScopeUtils.ASYNC_TRACE_SCOPE)).thenReturn(scope);
        when(trace.isAsync()).thenReturn(true);
        when(scope.canLeave()).thenReturn(true);
    }

    @Test
    public void noAsyncContext_noop() {
        interceptor.before(target, null, API_ID, ARGS);
        interceptor.after(target, null, API_ID, ARGS, null, null);
    }

    @Test
    public void singleExecution_bindsAndClosesOnce() {
        when(scope.isActive()).thenReturn(false);

        interceptor.before(target, asyncContext, API_ID, ARGS);
        interceptor.after(target, asyncContext, API_ID, ARGS, null, null);

        verify(scope).tryEnter();
        verify(scope).leave();
        verify(trace).close();
        verify(asyncContext).close();
    }

    @Test
    public void nestedRunToCall_activatesOnce() {
        // inner after sees the scope still active, outer after sees it ended.
        when(scope.isActive()).thenReturn(true, false);

        interceptor.before(target, asyncContext, API_ID, ARGS);            // run()
        interceptor.before(target, asyncContext, API_ID, ARGS);            // -> call()
        interceptor.after(target, asyncContext, API_ID, ARGS, null, null); // call() end
        interceptor.after(target, asyncContext, API_ID, ARGS, null, null); // run() end

        verify(scope, times(2)).tryEnter();
        verify(scope, times(2)).leave();
        verify(trace, times(1)).close();
        verify(asyncContext, times(1)).close();
    }

    @Test
    public void ambientTrace_isPreserved() {
        // inline executor: the thread already runs inside an entry trace - continueAsyncTraceObject
        // returns it, it has no async trace scope and is not async, so nothing is closed here.
        Trace ambient = mock(Trace.class);
        when(asyncContext.continueAsyncTraceObject(false)).thenReturn(ambient);
        when(asyncContext.currentAsyncTraceObject()).thenReturn(ambient);
        when(ambient.getScope(ScopeUtils.ASYNC_TRACE_SCOPE)).thenReturn(null);
        when(ambient.isAsync()).thenReturn(false);

        interceptor.before(target, asyncContext, API_ID, ARGS);
        interceptor.after(target, asyncContext, API_ID, ARGS, null, null);

        verify(ambient, never()).close();
        verify(asyncContext, never()).close();
    }

    @Test
    public void taskBodyException_stillUnbinds() {
        when(scope.isActive()).thenReturn(false);

        interceptor.before(target, asyncContext, API_ID, ARGS);
        interceptor.after(target, asyncContext, API_ID, ARGS, null, new RuntimeException("task failed"));

        verify(trace).close();
        verify(asyncContext).close();
    }

    @Test
    public void unbalancedScope_deletesUnstableTrace() {
        when(scope.canLeave()).thenReturn(false);

        interceptor.before(target, asyncContext, API_ID, ARGS);
        interceptor.after(target, asyncContext, API_ID, ARGS, null, null);

        // leave failed: the unstable trace is deleted instead of leaking on the worker thread.
        verify(trace).close();
        verify(asyncContext).close();
    }

    @Test
    public void continueFailure_afterWithoutBinding_noop() {
        AsyncContext deadContext = mock(AsyncContext.class);
        when(deadContext.continueAsyncTraceObject(false)).thenReturn(null);
        when(deadContext.currentAsyncTraceObject()).thenReturn(null);

        interceptor.before(target, deadContext, API_ID, ARGS);
        interceptor.after(target, deadContext, API_ID, ARGS, null, null);

        verifyNoInteractions(trace);
    }
}
