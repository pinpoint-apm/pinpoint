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

package com.navercorp.pinpoint.plugin.kotlinx.coroutines.interceptor;

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessor;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceBlock;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.context.scope.TraceScope;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.BlockAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.Interceptor;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.CoroutineContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

@ExtendWith(MockitoExtension.class)
class ResumeWithInterceptorTest {
    @Mock
    private TraceContext traceContext;
    @Mock
    private ProfilerConfig profilerConfig;
    @Mock
    private MethodDescriptor methodDescriptor;
    @Mock
    private Continuation<?> continuation;
    @Mock
    private AsyncContext asyncContext;
    @Mock
    private Trace trace;
    @Mock
    private TraceBlock traceBlock;
    @Mock
    private TraceScope traceScope;

    private CoroutineContext coroutineContext;

    @BeforeEach
    void setUp() {
        coroutineContext = mock(CoroutineContext.class, withSettings().extraInterfaces(AsyncContextAccessor.class));

        when(traceContext.getProfilerConfig()).thenReturn(profilerConfig);
        when(continuation.getContext()).thenReturn(coroutineContext);
        when(((AsyncContextAccessor) coroutineContext)._$PINPOINT$_getAsyncContext()).thenReturn(asyncContext);
        when(asyncContext.continueAsyncTraceObject(true)).thenReturn(trace);
        when(trace.getScope(AsyncContext.ASYNC_TRACE_SCOPE)).thenReturn(traceScope);
        when(trace.getTraceBlock()).thenReturn(traceBlock);
        when(traceBlock.getTrace()).thenReturn(trace);
        when(traceBlock.isBegin()).thenReturn(true);
        when(traceScope.canLeave()).thenReturn(true);
    }

    @Test
    void closesStartedTraceBlockAfterNestedReactorCallbackClearsCurrentTrace() {
        AtomicReference<Trace> currentTrace = new AtomicReference<>(trace);
        lenient().when(asyncContext.currentAsyncTraceObject()).thenAnswer(invocation -> currentTrace.get());
        lenient().when(trace.traceBlockBegin()).thenReturn(traceBlock);

        // Keep the pre-fix AroundInterceptor path executable so this test fails on behavior.
        Interceptor interceptor = new ResumeWithInterceptor(traceContext, methodDescriptor);
        TraceBlock block = beforeResume(interceptor);

        // Simulate a nested Reactor callback clearing the current async trace between interceptor hooks.
        currentTrace.set(null);
        afterResume(interceptor, block);

        assertSame(traceBlock, block);
        verify(asyncContext, never()).currentAsyncTraceObject();
        verify(traceBlock).recordApi(methodDescriptor);

        InOrder inOrder = inOrder(traceScope, traceBlock);
        inOrder.verify(traceScope).tryEnter();
        inOrder.verify(traceBlock).begin();
        inOrder.verify(traceScope).leave();
        inOrder.verify(traceBlock).close();
    }

    private TraceBlock beforeResume(Interceptor interceptor) {
        if (interceptor instanceof BlockAroundInterceptor) {
            return ((BlockAroundInterceptor) interceptor).before(continuation, null);
        }

        ((AroundInterceptor) interceptor).before(continuation, null);
        return traceBlock;
    }

    private void afterResume(Interceptor interceptor, TraceBlock block) {
        if (interceptor instanceof BlockAroundInterceptor) {
            ((BlockAroundInterceptor) interceptor).after(block, continuation, null, null, null);
            return;
        }

        ((AroundInterceptor) interceptor).after(continuation, null, null, null);
    }
}
