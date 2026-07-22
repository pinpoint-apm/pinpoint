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
import com.navercorp.pinpoint.bootstrap.interceptor.BlockAroundInterceptor;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.CoroutineContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.inOrder;
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
    void closesTraceBlockCreatedBeforeResumeWhenCurrentAsyncTraceIsUnavailable() {
        BlockAroundInterceptor interceptor = assertInstanceOf(
                BlockAroundInterceptor.class, new ResumeWithInterceptor(traceContext, methodDescriptor));

        TraceBlock block = interceptor.before(continuation, null);
        interceptor.after(block, continuation, null, null, null);

        assertSame(traceBlock, block);
        verify(asyncContext, never()).currentAsyncTraceObject();
        verify(traceBlock).recordApi(methodDescriptor);

        InOrder inOrder = inOrder(traceScope, traceBlock);
        inOrder.verify(traceScope).tryEnter();
        inOrder.verify(traceBlock).begin();
        inOrder.verify(traceScope).leave();
        inOrder.verify(traceBlock).close();
    }
}
