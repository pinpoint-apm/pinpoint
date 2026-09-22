/*
 * Copyright 2026 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.bootstrap.interceptor;

import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceBlock;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.context.scope.TraceScope;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * The Block bases must close the block that before() opened even when after() cannot see the
 * AsyncContext any more (a 4-arg getAsyncContext override answering differently from the 2-arg one).
 * Otherwise the frame pushed by before() leaks and the async scope never leaves.
 */
@ExtendWith(MockitoExtension.class)
class AsyncContextSpanEventBlockInterceptorAfterWithoutContextTest {

    private static final int API_ID = 7;
    private static final Object[] ARGS = new Object[0];

    @Mock
    private TraceContext traceContext;
    @Mock
    private MethodDescriptor methodDescriptor;
    @Mock
    private Trace trace;
    @Mock
    private TraceBlock traceBlock;
    @Mock
    private TraceScope traceScope;
    @Mock
    private AsyncContext asyncContext;

    private final Object target = new Object();

    @BeforeEach
    void setUp() {
        lenient().when(asyncContext.continueAsyncTraceObject(true)).thenReturn(trace);
        lenient().when(trace.getScope(AsyncContext.ASYNC_TRACE_SCOPE)).thenReturn(traceScope);
        lenient().when(trace.getTraceBlock()).thenReturn(traceBlock);
        lenient().when(trace.isAsync()).thenReturn(true);
        lenient().when(traceBlock.getTrace()).thenReturn(trace);
        lenient().when(traceBlock.isBegin()).thenReturn(true);
        lenient().when(traceScope.canLeave()).thenReturn(true);
        lenient().when(traceScope.isActive()).thenReturn(true);
    }

    // ---- ApiIdAware ---------------------------------------------------------------------------

    @Test
    void apiIdAware_afterWithoutContext_closesTheBlockAndLeavesTheScope_hooksSkipped() {
        ApiIdAwareStub interceptor = new ApiIdAwareStub(traceContext, asyncContext, null);

        TraceBlock block = interceptor.before(target, API_ID, ARGS);
        assertThat(block).isSameAs(traceBlock);
        interceptor.after(block, target, API_ID, ARGS, null, null);

        InOrder inOrder = inOrder(traceScope, traceBlock);
        inOrder.verify(traceScope).tryEnter();
        inOrder.verify(traceBlock).begin();
        inOrder.verify(traceScope).leave();
        inOrder.verify(traceBlock).close();

        assertThat(interceptor.afterHooks.get()).as("context-bound after hooks are skipped").isZero();
        verify(asyncContext, never()).close();
        verify(trace, never()).close();
    }

    @Test
    void apiIdAware_afterWithoutContext_endOfAsyncScope_closesTheTrace() {
        when(traceScope.isActive()).thenReturn(false);
        ApiIdAwareStub interceptor = new ApiIdAwareStub(traceContext, asyncContext, null);

        TraceBlock block = interceptor.before(target, API_ID, ARGS);
        interceptor.after(block, target, API_ID, ARGS, null, null);

        verify(traceBlock).close();
        verify(trace).close();
        verify(asyncContext, never()).close();
    }

    @Test
    void apiIdAware_afterWithoutContext_scopeCannotLeave_closesTheUnstableTrace() {
        when(traceScope.canLeave()).thenReturn(false);
        ApiIdAwareStub interceptor = new ApiIdAwareStub(traceContext, asyncContext, null);

        TraceBlock block = interceptor.before(target, API_ID, ARGS);
        interceptor.after(block, target, API_ID, ARGS, null, null);

        verify(trace).close();
        verify(traceBlock, never()).close();
        verify(asyncContext, never()).close();
    }

    @Test
    void apiIdAware_noBlock_afterIsANoOpAndDoesNotLookTheContextUp() {
        ApiIdAwareStub interceptor = new ApiIdAwareStub(traceContext, null, asyncContext);

        TraceBlock block = interceptor.before(target, API_ID, ARGS);
        assertThat(block).isNull();
        interceptor.after(block, target, API_ID, ARGS, null, null);

        assertThat(interceptor.afterLookups.get()).as("no block, no context lookup").isZero();
        verifyNoInteractions(trace, traceBlock, traceScope);
    }

    @Test
    void apiIdAware_withContext_behaviourUnchanged() {
        ApiIdAwareStub interceptor = new ApiIdAwareStub(traceContext, asyncContext, asyncContext);

        TraceBlock block = interceptor.before(target, API_ID, ARGS);
        interceptor.after(block, target, API_ID, ARGS, null, null);

        verify(traceScope).leave();
        verify(traceBlock).close();
        assertThat(interceptor.afterHooks.get()).as("doInAfterTrace + afterAction both run with a context").isEqualTo(2);
        verify(trace, never()).close();
        verify(asyncContext, never()).close();
    }

    @Test
    void apiIdAware_withContext_endOfAsyncScope_closesTraceAndContext() {
        when(traceScope.isActive()).thenReturn(false);
        ApiIdAwareStub interceptor = new ApiIdAwareStub(traceContext, asyncContext, asyncContext);

        TraceBlock block = interceptor.before(target, API_ID, ARGS);
        interceptor.after(block, target, API_ID, ARGS, null, null);

        InOrder inOrder = inOrder(traceBlock, trace, asyncContext);
        inOrder.verify(traceBlock).close();
        inOrder.verify(trace).close();
        inOrder.verify(asyncContext).close();
    }

    @Test
    void apiIdAware_withContext_scopeCannotLeave_closesTraceAndContext() {
        when(traceScope.canLeave()).thenReturn(false);
        ApiIdAwareStub interceptor = new ApiIdAwareStub(traceContext, asyncContext, asyncContext);

        TraceBlock block = interceptor.before(target, API_ID, ARGS);
        interceptor.after(block, target, API_ID, ARGS, null, null);

        verify(trace).close();
        verify(asyncContext).close();
        verify(traceBlock, never()).close();
        assertThat(interceptor.afterHooks.get()).isZero();
    }

    // ---- Simple -------------------------------------------------------------------------------

    @Test
    void simple_afterWithoutContext_closesTheBlockAndLeavesTheScope_hooksSkipped() {
        SimpleStub interceptor = new SimpleStub(traceContext, methodDescriptor, asyncContext, null);

        TraceBlock block = interceptor.before(target, ARGS);
        assertThat(block).isSameAs(traceBlock);
        interceptor.after(block, target, ARGS, null, null);

        verify(traceScope).leave();
        verify(traceBlock).close();
        assertThat(interceptor.afterHooks.get()).isZero();
        verify(asyncContext, never()).close();
    }

    // ---- ResultReplace ------------------------------------------------------------------------

    @Test
    void resultReplace_afterWithoutContext_closesTheBlockAndReturnsTheOriginalResult() {
        ResultReplaceStub interceptor = new ResultReplaceStub(traceContext, methodDescriptor, asyncContext, null);
        Object result = new Object();

        TraceBlock block = interceptor.before(target, Object.class, ARGS);
        assertThat(block).isSameAs(traceBlock);
        Object returned = interceptor.after(block, target, Object.class, ARGS, result, null);

        assertThat(returned).isSameAs(result);
        verify(traceScope).leave();
        verify(traceBlock).close();
        assertThat(interceptor.afterHooks.get()).isZero();
        assertThat(interceptor.replaces.get()).as("replaceResult is a context-bound hook too").isZero();
        verify(asyncContext, never()).close();
    }

    @Test
    void resultReplace_withContext_replacesTheResult() {
        when(traceScope.isActive()).thenReturn(false);
        ResultReplaceStub interceptor = new ResultReplaceStub(traceContext, methodDescriptor, asyncContext, asyncContext);
        Object result = new Object();

        TraceBlock block = interceptor.before(target, Object.class, ARGS);
        Object returned = interceptor.after(block, target, Object.class, ARGS, result, null);

        assertThat(returned).as("replaceResult runs when the context is present").isNotSameAs(result);
        assertThat(interceptor.replaces.get()).isEqualTo(1);
        assertThat(interceptor.afterHooks.get()).isEqualTo(1);
        verify(traceBlock).close();
        verify(trace).close();
        verify(asyncContext).close();
    }

    // ---- stubs: before sees beforeContext, after sees afterContext ---------------------------

    static class ApiIdAwareStub extends AsyncContextSpanEventBlockApiIdAwareAroundInterceptor {
        final AtomicInteger afterHooks = new AtomicInteger();
        final AtomicInteger afterLookups = new AtomicInteger();
        private final AsyncContext beforeContext;
        private final AsyncContext afterContext;

        ApiIdAwareStub(TraceContext traceContext, AsyncContext beforeContext, AsyncContext afterContext) {
            super(traceContext, true);
            this.beforeContext = beforeContext;
            this.afterContext = afterContext;
        }

        @Override
        protected AsyncContext getAsyncContext(Object target, Object[] args) {
            return beforeContext;
        }

        @Override
        protected AsyncContext getAsyncContext(Object target, Object[] args, Object result, Throwable throwable) {
            afterLookups.incrementAndGet();
            return afterContext;
        }

        @Override
        protected void doInBeforeTrace(SpanEventRecorder recorder, AsyncContext asyncContext, Object target, int apiId, Object[] args) {
        }

        @Override
        protected void doInAfterTrace(SpanEventRecorder recorder, Object target, int apiId, Object[] args, Object result, Throwable throwable) {
            afterHooks.incrementAndGet();
        }

        @Override
        protected void afterAction(AsyncContext asyncContext, Trace trace, Object target, int apiId, Object[] args, Object result, Throwable throwable) {
            afterHooks.incrementAndGet();
        }
    }

    static class SimpleStub extends AsyncContextSpanEventBlockSimpleAroundInterceptor {
        final AtomicInteger afterHooks = new AtomicInteger();
        private final AsyncContext beforeContext;
        private final AsyncContext afterContext;

        SimpleStub(TraceContext traceContext, MethodDescriptor methodDescriptor, AsyncContext beforeContext, AsyncContext afterContext) {
            super(traceContext, methodDescriptor, true);
            this.beforeContext = beforeContext;
            this.afterContext = afterContext;
        }

        @Override
        protected AsyncContext getAsyncContext(Object target, Object[] args) {
            return beforeContext;
        }

        @Override
        protected AsyncContext getAsyncContext(Object target, Object[] args, Object result, Throwable throwable) {
            return afterContext;
        }

        @Override
        protected void doInBeforeTrace(SpanEventRecorder recorder, AsyncContext asyncContext, Object target, Object[] args) {
        }

        @Override
        protected void doInAfterTrace(SpanEventRecorder recorder, Object target, Object[] args, Object result, Throwable throwable) {
            afterHooks.incrementAndGet();
        }
    }

    static class ResultReplaceStub extends AsyncContextSpanEventResultReplaceBlockSimpleAroundInterceptor {
        final AtomicInteger afterHooks = new AtomicInteger();
        final AtomicInteger replaces = new AtomicInteger();
        private final AsyncContext beforeContext;
        private final AsyncContext afterContext;

        ResultReplaceStub(TraceContext traceContext, MethodDescriptor methodDescriptor, AsyncContext beforeContext, AsyncContext afterContext) {
            super(traceContext, methodDescriptor, true);
            this.beforeContext = beforeContext;
            this.afterContext = afterContext;
        }

        @Override
        protected AsyncContext getAsyncContext(Object target, Object[] args) {
            return beforeContext;
        }

        @Override
        protected AsyncContext getAsyncContext(Object target, Object[] args, Object result, Throwable throwable) {
            return afterContext;
        }

        @Override
        protected void doInBeforeTrace(SpanEventRecorder recorder, AsyncContext asyncContext, Object target, Object[] args) {
        }

        @Override
        protected void doInAfterTrace(SpanEventRecorder recorder, Object target, Object[] args, Object result, Throwable throwable) {
            afterHooks.incrementAndGet();
        }

        @Override
        protected Object replaceResult(SpanEventRecorder recorder, AsyncContext asyncContext, Object target, Class<?> returnType, Object[] args, Object result, Throwable throwable) {
            replaces.incrementAndGet();
            return new Object();
        }
    }
}
