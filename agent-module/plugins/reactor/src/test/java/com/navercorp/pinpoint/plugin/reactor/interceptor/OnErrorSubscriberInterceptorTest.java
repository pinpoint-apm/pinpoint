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

package com.navercorp.pinpoint.plugin.reactor.interceptor;

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessor;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
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
import org.reactivestreams.Subscriber;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

@ExtendWith(MockitoExtension.class)
class OnErrorSubscriberInterceptorTest {

    private static final int API_ID = 42;

    @Mock
    private TraceContext traceContext;
    @Mock
    private ProfilerConfig profilerConfig;
    @Mock
    private AsyncContext asyncContext;
    @Mock
    private Trace trace;
    @Mock
    private TraceBlock traceBlock;
    @Mock
    private TraceScope traceScope;

    /** onErrorResume's ResumeSubscriber: a CoreSubscriber woven with the AsyncContext accessor. */
    private Subscriber<?> subscriber;
    private OnErrorSubscriberInterceptor interceptor;

    @BeforeEach
    void setUp() {
        subscriber = mock(Subscriber.class, withSettings().extraInterfaces(AsyncContextAccessor.class));
        when(traceContext.getProfilerConfig()).thenReturn(profilerConfig);
        when(profilerConfig.readBoolean("profiler.reactor.trace.onError", false)).thenReturn(true);
        when(profilerConfig.readBoolean("profiler.reactor.mark.error.onError", false)).thenReturn(false);
        interceptor = new OnErrorSubscriberInterceptor(traceContext);
    }

    /**
     * The customer case: the subscriber has no AsyncContext when onError starts, gets one while the
     * fallback publisher is subscribed inside onError, and then has one when after() runs. before()
     * opened nothing, so after() must touch neither the thread-bound trace nor its scope.
     */
    @Test
    void asyncContextArrivingDuringOnError_doesNotPopTheBoundTrace() {
        Throwable error = new IllegalStateException("boom");
        AsyncContextAccessor accessor = (AsyncContextAccessor) subscriber;
        when(accessor._$PINPOINT$_getAsyncContext()).thenReturn(null);

        TraceBlock block = interceptor.before(subscriber, API_ID, new Object[]{error});
        assertNull(block);

        // fallback subscribe (FluxAndMonoSubscribeMethodInterceptor / CoreSubscriberOnSubscribeInterceptor) attached a context
        lenient().when(accessor._$PINPOINT$_getAsyncContext()).thenReturn(asyncContext);
        lenient().when(asyncContext.currentAsyncTraceObject()).thenReturn(trace);

        interceptor.after(block, subscriber, API_ID, new Object[]{error}, null, null);

        // without a block there is nothing to balance, so after() does not even look the context up
        verify(accessor, times(1))._$PINPOINT$_getAsyncContext();
        verifyNoInteractions(trace, traceScope, traceBlock);
        verify(asyncContext, never()).currentAsyncTraceObject();
        verify(asyncContext, never()).close();
    }

    /** The normal case is unchanged: the block opened in before() is the one closed in after(). */
    @Test
    void asyncContextPresent_opensAndClosesTheSameBlock() {
        Throwable error = new IllegalStateException("boom");
        when(((AsyncContextAccessor) subscriber)._$PINPOINT$_getAsyncContext()).thenReturn(asyncContext);
        when(asyncContext.continueAsyncTraceObject(true)).thenReturn(trace);
        when(trace.getScope(AsyncContext.ASYNC_TRACE_SCOPE)).thenReturn(traceScope);
        when(trace.getTraceBlock()).thenReturn(traceBlock);
        when(trace.canSampled()).thenReturn(true);
        when(trace.isAsync()).thenReturn(true);
        when(traceBlock.getTrace()).thenReturn(trace);
        when(traceBlock.isBegin()).thenReturn(true);
        when(traceScope.canLeave()).thenReturn(true);
        when(traceScope.isActive()).thenReturn(true);

        TraceBlock block = interceptor.before(subscriber, API_ID, new Object[]{error});
        assertSame(traceBlock, block);

        interceptor.after(block, subscriber, API_ID, new Object[]{error}, null, null);

        // the block, not a fresh lookup of the thread-bound trace, is what after() works on
        verify(asyncContext, never()).currentAsyncTraceObject();
        verify(traceBlock).recordApiId(API_ID);
        verify(traceBlock).recordException(false, error);
        InOrder inOrder = inOrder(traceScope, traceBlock);
        inOrder.verify(traceScope).tryEnter();
        inOrder.verify(traceBlock).begin();
        inOrder.verify(traceScope).leave();
        inOrder.verify(traceBlock).close();
        verify(trace, never()).traceBlockEnd();
        verify(trace, never()).close();
    }

    @Test
    void traceOnErrorDisabled_isANoOp() {
        when(profilerConfig.readBoolean("profiler.reactor.trace.onError", false)).thenReturn(false);
        OnErrorSubscriberInterceptor disabled = new OnErrorSubscriberInterceptor(traceContext);
        // the subscriber does carry a context, but the switch is off
        lenient().when(((AsyncContextAccessor) subscriber)._$PINPOINT$_getAsyncContext()).thenReturn(asyncContext);

        TraceBlock block = disabled.before(subscriber, API_ID, new Object[]{new RuntimeException()});
        disabled.after(block, subscriber, API_ID, new Object[]{new RuntimeException()}, null, null);

        assertNull(block);
        verifyNoInteractions(asyncContext, trace);
    }
}
