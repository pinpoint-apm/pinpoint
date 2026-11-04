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
import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

@ExtendWith(MockitoExtension.class)
class DispatchInterceptorTest {
    @Mock
    private TraceContext traceContext;
    @Mock
    private MethodDescriptor methodDescriptor;
    @Mock
    private Trace trace;
    @Mock
    private SpanEventRecorder recorder;
    @Mock
    private AsyncContext asyncContext;

    private Runnable accessor;

    @BeforeEach
    void setUp() {
        accessor = mock(Runnable.class, withSettings().extraInterfaces(AsyncContextAccessor.class));
        when(traceContext.currentRawTraceObject()).thenReturn(trace);
        when(trace.traceBlockBegin()).thenReturn(recorder);
        when(trace.currentSpanEventRecorder()).thenReturn(recorder);
        when(recorder.recordNextAsyncContext()).thenReturn(asyncContext);
    }

    @Test
    void propagatesAsyncContextForSampledTrace() {
        verifyPropagation(true);
    }

    @Test
    void propagatesAsyncContextForUnsampledTrace() {
        verifyPropagation(false);
    }

    private void verifyPropagation(boolean sampled) {
        // the real TraceContext hides unsampled traces from currentTraceObject()
        lenient().when(trace.canSampled()).thenReturn(sampled);
        lenient().when(traceContext.currentTraceObject()).thenReturn(sampled ? trace : null);
        DispatchInterceptor interceptor = new DispatchInterceptor(traceContext, methodDescriptor);
        Object[] args = {accessor};

        interceptor.before(null, args);
        interceptor.after(null, args, null, null);

        InOrder inOrder = inOrder(trace, recorder, (AsyncContextAccessor) accessor);
        inOrder.verify(trace).traceBlockBegin();
        inOrder.verify(recorder).recordNextAsyncContext();
        inOrder.verify((AsyncContextAccessor) accessor)._$PINPOINT$_setAsyncContext(asyncContext);
        inOrder.verify(trace).traceBlockEnd();
        verify(traceContext, never()).currentTraceObject();
    }
}
