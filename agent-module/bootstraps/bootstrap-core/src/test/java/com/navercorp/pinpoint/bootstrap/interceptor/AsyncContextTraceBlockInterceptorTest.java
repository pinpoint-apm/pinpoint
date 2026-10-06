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

package com.navercorp.pinpoint.bootstrap.interceptor;

import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.context.AsyncContextTraceBlock;
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
 * The AsyncContextTraceBlock bases finish the async trace in after() with the AsyncContext that
 * before() opened the block under: the block carries it, so after() never looks it up on the
 * target, and the hooks, the scope and the context's thread binding are always balanced.
 */
@ExtendWith(MockitoExtension.class)
class AsyncContextTraceBlockInterceptorTest {

    private static final int API_ID = 7;
    private static final Object[] ARGS = new Object[0];

    @Mock
    private TraceContext traceContext;
    @Mock
    private MethodDescriptor methodDescriptor;
    @Mock
    private Trace trace;
    @Mock
    private AsyncContextTraceBlock carryingBlock;
    @Mock
    private TraceBlock plainBlock;
    @Mock
    private TraceScope traceScope;
    @Mock
    private AsyncContext asyncContext;

    private final Object target = new Object();

    @BeforeEach
    void setUp() {
        lenient().when(asyncContext.continueAsyncTraceObject(true)).thenReturn(trace);
        lenient().when(trace.getScope(AsyncContext.ASYNC_TRACE_SCOPE)).thenReturn(traceScope);
        lenient().when(trace.getTraceBlock(asyncContext)).thenReturn(carryingBlock);
        lenient().when(trace.isAsync()).thenReturn(true);
        lenient().when(carryingBlock.getTrace()).thenReturn(trace);
        lenient().when(carryingBlock.getAsyncContext()).thenReturn(asyncContext);
        lenient().when(carryingBlock.isBegin()).thenReturn(true);
        lenient().when(plainBlock.getTrace()).thenReturn(trace);
        lenient().when(plainBlock.isBegin()).thenReturn(true);
        lenient().when(traceScope.canLeave()).thenReturn(true);
        lenient().when(traceScope.isActive()).thenReturn(true);
    }

    // ---- Simple -------------------------------------------------------------------------------

    @Test
    void simple_carriedContext_runsTheHooksAndLeavesTheScope() {
        SimpleStub interceptor = new SimpleStub(traceContext, methodDescriptor, asyncContext);

        TraceBlock block = interceptor.before(target, ARGS);
        assertThat(block).isSameAs(carryingBlock);
        interceptor.after(block, target, ARGS, null, null);

        InOrder inOrder = inOrder(traceScope, carryingBlock);
        inOrder.verify(traceScope).tryEnter();
        inOrder.verify(carryingBlock).begin();
        inOrder.verify(traceScope).leave();
        inOrder.verify(carryingBlock).close();

        assertThat(interceptor.lookups.get()).as("the context is looked up once, in before()").isEqualTo(1);
        assertThat(interceptor.beforeHooks.get()).isEqualTo(1);
        assertThat(interceptor.afterHooks.get()).isEqualTo(1);
        assertThat(interceptor.afterActions.get()).isEqualTo(1);
        verify(trace, never()).close();
        verify(asyncContext, never()).close();
    }

    @Test
    void simple_carriedContext_endOfAsyncScope_closesTheTraceAndReleasesTheContext() {
        when(traceScope.isActive()).thenReturn(false);
        SimpleStub interceptor = new SimpleStub(traceContext, methodDescriptor, asyncContext);

        TraceBlock block = interceptor.before(target, ARGS);
        interceptor.after(block, target, ARGS, null, null);

        InOrder inOrder = inOrder(carryingBlock, trace, asyncContext);
        inOrder.verify(carryingBlock).close();
        inOrder.verify(trace).close();
        inOrder.verify(asyncContext).close();
        assertThat(interceptor.afterHooks.get()).isEqualTo(1);
    }

    @Test
    void simple_carriedContext_scopeCannotLeave_closesTheBlockInOrderThenTheTraceAndTheContext() {
        // something else already left the scope for us: the block is still closed in order and the
        // hooks still record, then the trace is closed as if its scope had ended here
        when(traceScope.canLeave()).thenReturn(false);
        SimpleStub interceptor = new SimpleStub(traceContext, methodDescriptor, asyncContext);

        TraceBlock block = interceptor.before(target, ARGS);
        interceptor.after(block, target, ARGS, null, null);

        InOrder inOrder = inOrder(traceScope, carryingBlock, trace, asyncContext);
        inOrder.verify(traceScope).canLeave();
        inOrder.verify(carryingBlock).close();
        inOrder.verify(trace).close();
        inOrder.verify(asyncContext).close();
        verify(traceScope, never()).leave();
        assertThat(interceptor.afterHooks.get()).isEqualTo(1);
    }

    @Test
    void simple_noContext_beforeOpensNothingAndAfterIsANoOp() {
        SimpleStub interceptor = new SimpleStub(traceContext, methodDescriptor, null);

        TraceBlock block = interceptor.before(target, ARGS);
        assertThat(block).isNull();
        interceptor.after(block, target, ARGS, null, null);

        verifyNoInteractions(trace, traceScope, carryingBlock);
        assertThat(interceptor.lookups.get()).isEqualTo(1);
        assertThat(interceptor.afterHooks.get()).isZero();
    }

    @Test
    void simple_noTrace_beforeOpensNothing() {
        when(asyncContext.continueAsyncTraceObject(true)).thenReturn(null);
        SimpleStub interceptor = new SimpleStub(traceContext, methodDescriptor, asyncContext);

        assertThat(interceptor.before(target, ARGS)).isNull();
        verifyNoInteractions(trace, traceScope, carryingBlock);
    }

    @Test
    void simple_plainBlock_isClosedAndTheScopeLeft_hooksAndContextReleaseSkipped() {
        // a Trace implementation that does not carry the context: same as the deprecated Block bases
        when(trace.getTraceBlock(asyncContext)).thenReturn(plainBlock);
        when(traceScope.isActive()).thenReturn(false);
        SimpleStub interceptor = new SimpleStub(traceContext, methodDescriptor, asyncContext);

        TraceBlock block = interceptor.before(target, ARGS);
        assertThat(block).isSameAs(plainBlock);
        interceptor.after(block, target, ARGS, null, null);

        InOrder inOrder = inOrder(traceScope, plainBlock, trace);
        inOrder.verify(traceScope).leave();
        inOrder.verify(plainBlock).close();
        inOrder.verify(trace).close();
        assertThat(interceptor.afterHooks.get()).isZero();
        assertThat(interceptor.afterActions.get()).isZero();
        verify(asyncContext, never()).close();
    }

    @Test
    void simple_plainBlock_scopeCannotLeave_closesTheBlockThenTheTrace_contextNotReleased() {
        when(trace.getTraceBlock(asyncContext)).thenReturn(plainBlock);
        when(traceScope.canLeave()).thenReturn(false);
        SimpleStub interceptor = new SimpleStub(traceContext, methodDescriptor, asyncContext);

        TraceBlock block = interceptor.before(target, ARGS);
        interceptor.after(block, target, ARGS, null, null);

        InOrder inOrder = inOrder(plainBlock, trace);
        inOrder.verify(plainBlock).close();
        inOrder.verify(trace).close();
        verify(asyncContext, never()).close();
        assertThat(interceptor.afterHooks.get()).isZero();
    }

    @Test
    void simple_blockNotBegun_runsOnlyTheActionHooks() {
        when(carryingBlock.isBegin()).thenReturn(false);
        SimpleStub interceptor = new SimpleStub(traceContext, methodDescriptor, asyncContext);
        interceptor.beginBlock = false;

        TraceBlock block = interceptor.before(target, ARGS);
        interceptor.after(block, target, ARGS, null, null);

        verify(carryingBlock, never()).begin();
        verify(carryingBlock).close();
        assertThat(interceptor.beforeHooks.get()).isZero();
        assertThat(interceptor.afterHooks.get()).isZero();
        assertThat(interceptor.afterActions.get()).isEqualTo(1);
    }

    @Test
    void simple_hookFailure_stillClosesTheBlockAndReleasesTheContext() {
        when(traceScope.isActive()).thenReturn(false);
        SimpleStub interceptor = new SimpleStub(traceContext, methodDescriptor, asyncContext);
        interceptor.failAfterHook = true;

        TraceBlock block = interceptor.before(target, ARGS);
        interceptor.after(block, target, ARGS, null, null);

        verify(carryingBlock).close();
        verify(trace).close();
        verify(asyncContext).close();
    }

    // ---- ApiIdAware ---------------------------------------------------------------------------

    @Test
    void apiIdAware_carriedContext_runsTheHooksAndLeavesTheScope() {
        ApiIdAwareStub interceptor = new ApiIdAwareStub(traceContext, asyncContext);

        TraceBlock block = interceptor.before(target, API_ID, ARGS);
        assertThat(block).isSameAs(carryingBlock);
        interceptor.after(block, target, API_ID, ARGS, null, null);

        InOrder inOrder = inOrder(traceScope, carryingBlock);
        inOrder.verify(traceScope).tryEnter();
        inOrder.verify(carryingBlock).begin();
        inOrder.verify(traceScope).leave();
        inOrder.verify(carryingBlock).close();

        assertThat(interceptor.lookups.get()).isEqualTo(1);
        assertThat(interceptor.afterHooks.get()).isEqualTo(1);
        assertThat(interceptor.afterApiId).isEqualTo(API_ID);
        verify(asyncContext, never()).close();
    }

    @Test
    void apiIdAware_carriedContext_endOfAsyncScope_closesTheTraceAndReleasesTheContext() {
        when(traceScope.isActive()).thenReturn(false);
        ApiIdAwareStub interceptor = new ApiIdAwareStub(traceContext, asyncContext);

        TraceBlock block = interceptor.before(target, API_ID, ARGS);
        interceptor.after(block, target, API_ID, ARGS, null, null);

        InOrder inOrder = inOrder(carryingBlock, trace, asyncContext);
        inOrder.verify(carryingBlock).close();
        inOrder.verify(trace).close();
        inOrder.verify(asyncContext).close();
    }

    @Test
    void apiIdAware_carriedContext_scopeCannotLeave_closesTheBlockInOrderThenTheTraceAndTheContext() {
        when(traceScope.canLeave()).thenReturn(false);
        ApiIdAwareStub interceptor = new ApiIdAwareStub(traceContext, asyncContext);

        TraceBlock block = interceptor.before(target, API_ID, ARGS);
        interceptor.after(block, target, API_ID, ARGS, null, null);

        InOrder inOrder = inOrder(carryingBlock, trace, asyncContext);
        inOrder.verify(carryingBlock).close();
        inOrder.verify(trace).close();
        inOrder.verify(asyncContext).close();
        verify(traceScope, never()).leave();
        assertThat(interceptor.afterHooks.get()).isEqualTo(1);
    }

    @Test
    void apiIdAware_noContext_beforeOpensNothingAndAfterIsANoOp() {
        ApiIdAwareStub interceptor = new ApiIdAwareStub(traceContext, null);

        TraceBlock block = interceptor.before(target, API_ID, ARGS);
        assertThat(block).isNull();
        interceptor.after(block, target, API_ID, ARGS, null, null);

        verifyNoInteractions(trace, traceScope, carryingBlock);
        assertThat(interceptor.afterHooks.get()).isZero();
    }

    @Test
    void apiIdAware_plainBlock_isClosedAndTheScopeLeft_hooksAndContextReleaseSkipped() {
        when(trace.getTraceBlock(asyncContext)).thenReturn(plainBlock);
        ApiIdAwareStub interceptor = new ApiIdAwareStub(traceContext, asyncContext);

        TraceBlock block = interceptor.before(target, API_ID, ARGS);
        assertThat(block).isSameAs(plainBlock);
        interceptor.after(block, target, API_ID, ARGS, null, null);

        verify(traceScope).leave();
        verify(plainBlock).close();
        assertThat(interceptor.afterHooks.get()).isZero();
        verify(asyncContext, never()).close();
    }

    // ---- stubs --------------------------------------------------------------------------------

    static class SimpleStub extends AsyncContextTraceBlockSimpleAroundInterceptor {
        final AtomicInteger lookups = new AtomicInteger();
        final AtomicInteger beforeHooks = new AtomicInteger();
        final AtomicInteger afterHooks = new AtomicInteger();
        final AtomicInteger afterActions = new AtomicInteger();
        boolean beginBlock = true;
        boolean failAfterHook;
        private final AsyncContext asyncContext;

        SimpleStub(TraceContext traceContext, MethodDescriptor methodDescriptor, AsyncContext asyncContext) {
            super(traceContext, methodDescriptor, true);
            this.asyncContext = asyncContext;
        }

        @Override
        protected AsyncContext getAsyncContext(Object target, Object[] args) {
            lookups.incrementAndGet();
            return asyncContext;
        }

        @Override
        protected boolean checkBeforeTraceBlockBegin(AsyncContext asyncContext, Trace trace, Object target, Object[] args) {
            return beginBlock;
        }

        @Override
        protected void doInBeforeTrace(SpanEventRecorder recorder, AsyncContext asyncContext, Object target, Object[] args) {
            beforeHooks.incrementAndGet();
        }

        @Override
        protected void doInAfterTrace(SpanEventRecorder recorder, Object target, Object[] args, Object result, Throwable throwable) {
            afterHooks.incrementAndGet();
            if (failAfterHook) {
                throw new IllegalStateException("after hook");
            }
        }

        @Override
        protected void afterAction(AsyncContext asyncContext, Trace trace, Object target, Object[] args, Object result, Throwable throwable) {
            afterActions.incrementAndGet();
        }
    }

    static class ApiIdAwareStub extends AsyncContextTraceBlockApiIdAwareAroundInterceptor {
        final AtomicInteger lookups = new AtomicInteger();
        final AtomicInteger afterHooks = new AtomicInteger();
        int afterApiId = -1;
        private final AsyncContext asyncContext;

        ApiIdAwareStub(TraceContext traceContext, AsyncContext asyncContext) {
            super(traceContext, true);
            this.asyncContext = asyncContext;
        }

        @Override
        protected AsyncContext getAsyncContext(Object target, Object[] args) {
            lookups.incrementAndGet();
            return asyncContext;
        }

        @Override
        protected void doInBeforeTrace(SpanEventRecorder recorder, AsyncContext asyncContext, Object target, int apiId, Object[] args) {
        }

        @Override
        protected void doInAfterTrace(SpanEventRecorder recorder, Object target, int apiId, Object[] args, Object result, Throwable throwable) {
            afterHooks.incrementAndGet();
            afterApiId = apiId;
        }
    }
}
