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

package com.navercorp.pinpoint.plugin.reactor.interceptor;

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessor;
import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.context.AsyncContextTraceBlock;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceBlock;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.context.scope.TraceScope;
import com.navercorp.pinpoint.common.trace.ServiceType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.reactivestreams.Subscriber;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

/**
 * The scheduler-hop carrier: run() continues the async trace of the subscriber's context without
 * opening a span event block (asyncTraceBlock=false), and after() finishes it with the context the
 * block carries, so the subscriber is read once.
 */
@ExtendWith(MockitoExtension.class)
class CoreSubscriberRunInterceptorTest {

    private static final int API_ID = 7;
    private static final Object[] ARGS = new Object[0];

    @Mock
    private TraceContext traceContext;
    @Mock
    private AsyncContext asyncContext;
    @Mock
    private Trace trace;
    @Mock
    private AsyncContextTraceBlock traceBlock;
    @Mock
    private TraceScope traceScope;

    private Subscriber<?> subscriber;
    private CoreSubscriberRunInterceptor interceptor;

    @BeforeEach
    void setUp() {
        subscriber = mock(Subscriber.class, withSettings().extraInterfaces(AsyncContextAccessor.class));
        interceptor = new CoreSubscriberRunInterceptor(traceContext, ServiceType.ASYNC);
    }

    @Test
    void contextPresent_continuesTheTraceWithoutABlockAndClosesItWhenTheScopeEnds() {
        when(((AsyncContextAccessor) subscriber)._$PINPOINT$_getAsyncContext()).thenReturn(asyncContext);
        when(asyncContext.continueAsyncTraceObject(false)).thenReturn(trace);
        when(trace.getScope(AsyncContext.ASYNC_TRACE_SCOPE)).thenReturn(traceScope);
        when(trace.getTraceBlock(asyncContext)).thenReturn(traceBlock);
        when(trace.isAsync()).thenReturn(true);
        when(traceBlock.getTrace()).thenReturn(trace);
        when(traceBlock.getAsyncContext()).thenReturn(asyncContext);
        when(traceScope.canLeave()).thenReturn(true);
        when(traceScope.isActive()).thenReturn(false);

        TraceBlock block = interceptor.before(subscriber, API_ID, ARGS);
        assertSame(traceBlock, block);
        interceptor.after(block, subscriber, API_ID, ARGS, null, null);

        verify((AsyncContextAccessor) subscriber, times(1))._$PINPOINT$_getAsyncContext();
        verify(traceBlock, never()).begin();
        InOrder inOrder = inOrder(traceScope, traceBlock, trace, asyncContext);
        inOrder.verify(traceScope).tryEnter();
        inOrder.verify(traceScope).leave();
        inOrder.verify(traceBlock).close();
        inOrder.verify(trace).close();
        inOrder.verify(asyncContext).close();
    }

    @Test
    void contextPresent_nestedActivation_leavesTheTraceOpen() {
        when(((AsyncContextAccessor) subscriber)._$PINPOINT$_getAsyncContext()).thenReturn(asyncContext);
        when(asyncContext.continueAsyncTraceObject(false)).thenReturn(trace);
        when(trace.getScope(AsyncContext.ASYNC_TRACE_SCOPE)).thenReturn(traceScope);
        when(trace.getTraceBlock(asyncContext)).thenReturn(traceBlock);
        when(trace.isAsync()).thenReturn(true);
        when(traceBlock.getTrace()).thenReturn(trace);
        when(traceBlock.getAsyncContext()).thenReturn(asyncContext);
        when(traceScope.canLeave()).thenReturn(true);
        when(traceScope.isActive()).thenReturn(true);

        TraceBlock block = interceptor.before(subscriber, API_ID, ARGS);
        interceptor.after(block, subscriber, API_ID, ARGS, null, null);

        verify(traceBlock).close();
        verify(trace, never()).close();
        verify(asyncContext, never()).close();
    }

    @Test
    void noContext_isANoOp() {
        when(((AsyncContextAccessor) subscriber)._$PINPOINT$_getAsyncContext()).thenReturn(null);

        TraceBlock block = interceptor.before(subscriber, API_ID, ARGS);
        assertNull(block);
        interceptor.after(block, subscriber, API_ID, ARGS, null, null);

        verifyNoInteractions(asyncContext, trace, traceScope, traceBlock);
    }
}
