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
package com.navercorp.pinpoint.plugin.spring.webflux.interceptor;

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessor;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.context.scope.TraceScope;
import com.navercorp.pinpoint.bootstrap.util.ScopeUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Frame-stack pairing of the wrapping BodyInserter interceptor (W4): every before() pushes
 * exactly one frame — early return and failure push EMPTY — and after() polls exactly one and
 * unwinds only what its own paired before() recorded. The cases that used to mispair under the
 * old boolean stack (an inner no-op invocation stealing the outer frame; a before() failure
 * leaving a stale flag) are pinned here.
 */
public class WrappingBodyInserterRequestBuilderWriteToInterceptorTest {

    private static final Object[] NO_REQUEST_ARGS = new Object[]{null};

    private WrappingBodyInserterRequestBuilderWriteToInterceptor interceptor;
    private AsyncContext outerContext;
    private Trace outerTrace;
    private TraceScope outerScope;

    @BeforeEach
    public void setUp() {
        final ProfilerConfig profilerConfig = mock(ProfilerConfig.class);
        when(profilerConfig.readBoolean(anyString(), anyBoolean())).thenAnswer(inv -> inv.getArgument(1));
        when(profilerConfig.readInt(anyString(), anyInt())).thenAnswer(inv -> inv.getArgument(1));
        when(profilerConfig.readString(anyString())).thenReturn("ALWAYS");
        when(profilerConfig.readString(anyString(), anyString())).thenAnswer(inv -> inv.getArgument(1));

        final TraceContext traceContext = mock(TraceContext.class);
        when(traceContext.getProfilerConfig()).thenReturn(profilerConfig);
        // DefaultRequestTraceWriter requires the identity fields at construction.
        when(traceContext.getApplicationName()).thenReturn("test-app");
        when(traceContext.getAgentId()).thenReturn("test-agent");
        interceptor = new WrappingBodyInserterRequestBuilderWriteToInterceptor(traceContext, mock(MethodDescriptor.class));

        outerContext = mock(AsyncContext.class);
        outerTrace = mock(Trace.class);
        outerScope = mock(TraceScope.class);
        when(outerContext.continueAsyncTraceObject(true)).thenReturn(outerTrace);
        when(outerTrace.getScope(ScopeUtils.ASYNC_TRACE_SCOPE)).thenReturn(outerScope);
        when(outerTrace.isAsync()).thenReturn(true);
        when(outerScope.tryEnter()).thenReturn(true);
        when(outerScope.canLeave()).thenReturn(true);
        when(outerScope.isActive()).thenReturn(false);
    }

    private static Object accessorTarget(AsyncContext asyncContext) {
        final MockAccessor target = new MockAccessor();
        target._$PINPOINT$_setAsyncContext(asyncContext);
        return target;
    }

    @Test
    public void innerNoopInvocation_cannotStealOuterFrame() {
        final Object outerTarget = accessorTarget(outerContext);
        final Object innerTarget = new MockAccessor(); // no context: inner before() is a no-op

        interceptor.before(outerTarget, Object.class, NO_REQUEST_ARGS);   // pushes real frame
        interceptor.before(innerTarget, Object.class, NO_REQUEST_ARGS);   // pushes EMPTY
        interceptor.after(innerTarget, Object.class, NO_REQUEST_ARGS, null, null);

        // the inner after() consumed only its own EMPTY frame - the outer trace is untouched.
        verify(outerScope, never()).leave();
        verify(outerTrace, never()).close();

        interceptor.after(outerTarget, Object.class, NO_REQUEST_ARGS, null, null);

        // the outer after() unwinds its own frame exactly once (scope leave + end-scope cleanup).
        verify(outerScope, times(1)).leave();
        verify(outerTrace, times(1)).close();
        verify(outerContext, times(1)).close();
    }

    @Test
    public void beforeFailure_pushesEmptyFrame_afterIsHarmless() {
        final AsyncContext failing = mock(AsyncContext.class);
        when(failing.continueAsyncTraceObject(true)).thenThrow(new IllegalStateException("continue failed"));
        final Object target = accessorTarget(failing);

        interceptor.before(target, Object.class, NO_REQUEST_ARGS);
        interceptor.after(target, Object.class, NO_REQUEST_ARGS, null, null);

        verify(failing, never()).close();
    }

    @Test
    public void recursion_unwindsInLifoOrder_eachFrameOnce() {
        final AsyncContext innerContext = mock(AsyncContext.class);
        final Trace innerTrace = mock(Trace.class);
        final TraceScope innerScope = mock(TraceScope.class);
        when(innerContext.continueAsyncTraceObject(true)).thenReturn(innerTrace);
        when(innerTrace.getScope(ScopeUtils.ASYNC_TRACE_SCOPE)).thenReturn(innerScope);
        when(innerTrace.isAsync()).thenReturn(true);
        when(innerScope.tryEnter()).thenReturn(true);
        when(innerScope.canLeave()).thenReturn(true);
        when(innerScope.isActive()).thenReturn(false);

        final Object outerTarget = accessorTarget(outerContext);
        final Object innerTarget = accessorTarget(innerContext);

        interceptor.before(outerTarget, Object.class, NO_REQUEST_ARGS);
        interceptor.before(innerTarget, Object.class, NO_REQUEST_ARGS);
        interceptor.after(innerTarget, Object.class, NO_REQUEST_ARGS, null, null);
        interceptor.after(outerTarget, Object.class, NO_REQUEST_ARGS, null, null);

        verify(innerTrace, times(1)).close();
        verify(outerTrace, times(1)).close();
        verify(innerContext, times(1)).close();
        verify(outerContext, times(1)).close();
    }

    @Test
    public void afterWithoutPairedBefore_isHarmless() {
        final Object target = accessorTarget(outerContext);

        interceptor.after(target, Object.class, NO_REQUEST_ARGS, null, null);

        verify(outerTrace, never()).close();
        verify(outerContext, never()).close();
    }

    @Test
    public void unbalancedScope_deletesUnstableTraceFromOwnFrame() {
        when(outerScope.canLeave()).thenReturn(false);
        final Object target = accessorTarget(outerContext);

        interceptor.before(target, Object.class, NO_REQUEST_ARGS);
        interceptor.after(target, Object.class, NO_REQUEST_ARGS, null, null);

        verify(outerTrace, times(1)).close();
        verify(outerContext, times(1)).close();
    }

    private static class MockAccessor implements AsyncContextAccessor {
        private AsyncContext asyncContext;

        @Override
        public void _$PINPOINT$_setAsyncContext(AsyncContext asyncContext) {
            this.asyncContext = asyncContext;
        }

        @Override
        public AsyncContext _$PINPOINT$_getAsyncContext() {
            return asyncContext;
        }
    }
}
