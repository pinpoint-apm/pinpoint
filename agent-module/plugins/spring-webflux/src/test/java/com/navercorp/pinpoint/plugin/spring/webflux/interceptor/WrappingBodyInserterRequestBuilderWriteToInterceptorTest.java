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
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceBlock;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.context.scope.TraceScope;
import com.navercorp.pinpoint.bootstrap.util.ScopeUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Weaver-carried block pairing of the wrapping BodyInserter interceptor: before() hands its
 * conditionally-begun TraceBlock to the weaver and after() unwinds exactly the trace that block
 * carries, so a cross-invocation mispair is structurally impossible (this replaces the frame
 * stack the ResultReplace-only variant needed). The replace hook wraps the returned publisher
 * only when the paired block was begun.
 */
public class WrappingBodyInserterRequestBuilderWriteToInterceptorTest {

    private static final Object[] NO_REQUEST_ARGS = new Object[]{null};

    private WrappingBodyInserterRequestBuilderWriteToInterceptor interceptor;
    private AsyncContext outerContext;
    private Trace outerTrace;
    private TraceScope outerScope;
    private TraceBlock outerBlock;

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
        outerBlock = mock(TraceBlock.class);
        when(outerContext.continueAsyncTraceObject(true)).thenReturn(outerTrace);
        when(outerTrace.getScope(ScopeUtils.ASYNC_TRACE_SCOPE)).thenReturn(outerScope);
        when(outerTrace.isAsync()).thenReturn(true);
        when(outerTrace.getTraceBlock()).thenReturn(outerBlock);
        when(outerBlock.getTrace()).thenReturn(outerTrace);
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
    public void before_returnsTraceBlockForTheWeaverChannel() {
        final Object target = accessorTarget(outerContext);

        final TraceBlock block = interceptor.before(target, Object.class, NO_REQUEST_ARGS);

        assertSame(outerBlock, block);
        // no request argument: the block travels un-begun.
        verify(outerBlock, never()).begin();
    }

    @Test
    public void beforeWithoutContext_returnsNull() {
        final TraceBlock block = interceptor.before(new MockAccessor(), Object.class, NO_REQUEST_ARGS);

        assertNull(block);
        verify(outerTrace, never()).getTraceBlock();
    }

    @Test
    public void nestedInvocations_afterUnwindsExactlyItsOwnBlock() {
        final AsyncContext innerContext = mock(AsyncContext.class);
        final Trace innerTrace = mock(Trace.class);
        final TraceScope innerScope = mock(TraceScope.class);
        final TraceBlock innerBlock = mock(TraceBlock.class);
        when(innerContext.continueAsyncTraceObject(true)).thenReturn(innerTrace);
        when(innerTrace.getScope(ScopeUtils.ASYNC_TRACE_SCOPE)).thenReturn(innerScope);
        when(innerTrace.isAsync()).thenReturn(true);
        when(innerTrace.getTraceBlock()).thenReturn(innerBlock);
        when(innerBlock.getTrace()).thenReturn(innerTrace);
        when(innerScope.tryEnter()).thenReturn(true);
        when(innerScope.canLeave()).thenReturn(true);
        when(innerScope.isActive()).thenReturn(false);

        final Object outerTarget = accessorTarget(outerContext);
        final Object innerTarget = accessorTarget(innerContext);

        final TraceBlock outer = interceptor.before(outerTarget, Object.class, NO_REQUEST_ARGS);
        final TraceBlock inner = interceptor.before(innerTarget, Object.class, NO_REQUEST_ARGS);

        interceptor.after(inner, innerTarget, Object.class, NO_REQUEST_ARGS, null, null);
        // the inner after() unwound only the trace its own block carries.
        verify(innerTrace, times(1)).close();
        verify(outerTrace, never()).close();

        interceptor.after(outer, outerTarget, Object.class, NO_REQUEST_ARGS, null, null);
        verify(outerScope, times(1)).leave();
        verify(outerTrace, times(1)).close();
        verify(outerContext, times(1)).close();
    }

    @Test
    public void afterWithNullBlock_keepsResultAndTouchesNothing() {
        final Object target = accessorTarget(outerContext);
        final Object result = new Object();

        final Object returned = interceptor.after(null, target, Object.class, NO_REQUEST_ARGS, result, null);

        assertSame(result, returned);
        verify(outerTrace, never()).close();
        verify(outerContext, never()).close();
    }

    @Test
    public void afterNotBegun_keepsResult() {
        final Object target = accessorTarget(outerContext);
        final Object result = new Object();

        final TraceBlock block = interceptor.before(target, Object.class, NO_REQUEST_ARGS);
        final Object returned = interceptor.after(block, target, Object.class, NO_REQUEST_ARGS, result, null);

        assertSame(result, returned);
    }

    @Test
    public void unbalancedScope_deletesUnstableTraceFromTheBlock() {
        when(outerScope.canLeave()).thenReturn(false);
        final Object target = accessorTarget(outerContext);

        final TraceBlock block = interceptor.before(target, Object.class, NO_REQUEST_ARGS);
        final Object result = new Object();
        final Object returned = interceptor.after(block, target, Object.class, NO_REQUEST_ARGS, result, null);

        assertSame(result, returned);
        verify(outerTrace, times(1)).close();
        verify(outerContext, times(1)).close();
    }

    @Test
    public void replaceResult_wrapsWrappablePublisher() {
        final SpanEventRecorder recorder = mock(SpanEventRecorder.class);
        final AsyncContext nextContext = mock(AsyncContext.class);
        when(recorder.recordNextAsyncContext()).thenReturn(nextContext);
        final Mono<String> source = Mono.defer(() -> Mono.just("body"));

        final Object returned = interceptor.replaceResult(recorder, outerContext, new Object(), Object.class, NO_REQUEST_ARGS, source, null);

        assertNotSame(source, returned);
        assertTrue(returned instanceof Mono);
        verify(recorder, times(1)).recordNextAsyncContext();
    }

    @Test
    public void replaceResult_keepsScalarPublisher_noDanglingAsyncLink() {
        final SpanEventRecorder recorder = mock(SpanEventRecorder.class);
        // Mono.just is Fuseable.ScalarCallable: wrapping would destroy the scalar fast path,
        // so the wrapper skips it and no async link may be minted for it.
        final Mono<String> source = Mono.just("body");

        final Object returned = interceptor.replaceResult(recorder, outerContext, new Object(), Object.class, NO_REQUEST_ARGS, source, null);

        assertSame(source, returned);
        verify(recorder, never()).recordNextAsyncContext();
    }

    @Test
    public void replaceResult_keepsResultOnThrowable() {
        final SpanEventRecorder recorder = mock(SpanEventRecorder.class);
        final Mono<String> source = Mono.just("body");

        final Object returned = interceptor.replaceResult(recorder, outerContext, new Object(), Object.class, NO_REQUEST_ARGS, source, new RuntimeException("fail"));

        assertSame(source, returned);
        verify(recorder, never()).recordNextAsyncContext();
    }

    @Test
    public void replaceResult_keepsNonWrappableResult() {
        final SpanEventRecorder recorder = mock(SpanEventRecorder.class);
        final Object source = new Object();

        final Object returned = interceptor.replaceResult(recorder, outerContext, new Object(), Object.class, NO_REQUEST_ARGS, source, null);

        assertSame(source, returned);
        verify(recorder, never()).recordNextAsyncContext();
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
