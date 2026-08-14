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
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.common.trace.ServiceType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Gate arithmetic of the carrier-first, current-trace-fallback capture policy. The interceptor runs
 * in the constructor's after() — before() is {@code @IgnoreMethod} — so every case drives after().
 */
public class SchedulerTaskConstructorInterceptorTest {

    private TraceContext traceContext;
    private MethodDescriptor methodDescriptor;
    private ServiceType serviceType;
    private SchedulerTaskConstructorInterceptor interceptor;

    @BeforeEach
    public void setUp() {
        traceContext = mock(TraceContext.class);
        methodDescriptor = mock(MethodDescriptor.class);
        serviceType = mock(ServiceType.class);
        interceptor = new SchedulerTaskConstructorInterceptor(traceContext, methodDescriptor, serviceType);
    }

    @Test
    public void failedConstructor_capturesNothing() {
        MockAsyncContextAccessor target = new MockAsyncContextAccessor();
        MockAsyncContextAccessor carrier = new MockAsyncContextAccessor();
        carrier._$PINPOINT$_setAsyncContext(mock(AsyncContext.class));

        interceptor.after(target, new Object[]{carrier}, null, new RuntimeException("ctor failed"));

        assertNull(target._$PINPOINT$_getAsyncContext());
        verifyNoInteractions(traceContext);
    }

    @Test
    public void existingContext_kept() {
        AsyncContext existing = mock(AsyncContext.class);
        MockAsyncContextAccessor target = new MockAsyncContextAccessor();
        target._$PINPOINT$_setAsyncContext(existing);
        MockAsyncContextAccessor carrier = new MockAsyncContextAccessor();
        carrier._$PINPOINT$_setAsyncContext(mock(AsyncContext.class));

        interceptor.after(target, new Object[]{carrier}, null, null);

        assertSame(existing, target._$PINPOINT$_getAsyncContext());
        verifyNoInteractions(traceContext);
    }

    @Test
    public void carrierCopied_withoutTouchingCurrentTrace() {
        AsyncContext inherited = mock(AsyncContext.class);
        MockAsyncContextAccessor target = new MockAsyncContextAccessor();
        MockAsyncContextAccessor carrier = new MockAsyncContextAccessor();
        carrier._$PINPOINT$_setAsyncContext(inherited);

        interceptor.after(target, new Object[]{carrier, mock(Object.class)}, null, null);

        assertSame(inherited, target._$PINPOINT$_getAsyncContext());
        // carrier-first: the fallback must not run, no sibling boundary context is minted.
        verifyNoInteractions(traceContext);
    }

    @Test
    public void conflictingCarriers_fallBackToCurrentTrace() {
        MockAsyncContextAccessor target = new MockAsyncContextAccessor();
        MockAsyncContextAccessor first = new MockAsyncContextAccessor();
        MockAsyncContextAccessor second = new MockAsyncContextAccessor();
        first._$PINPOINT$_setAsyncContext(mock(AsyncContext.class));
        second._$PINPOINT$_setAsyncContext(mock(AsyncContext.class));

        Trace trace = mock(Trace.class);
        SpanEventRecorder recorder = mock(SpanEventRecorder.class);
        AsyncContext boundary = mock(AsyncContext.class);
        when(traceContext.currentTraceObject()).thenReturn(trace);
        when(trace.traceBlockBegin()).thenReturn(recorder);
        when(recorder.recordNextAsyncContext()).thenReturn(boundary);

        interceptor.after(target, new Object[]{first, second}, null, null);

        // neither conflicting context is chosen arbitrarily - a fresh boundary context is recorded.
        assertSame(boundary, target._$PINPOINT$_getAsyncContext());
        verify(trace).traceBlockEnd();
    }

    @Test
    public void noCarrier_recordsBoundarySpanEvent() {
        MockAsyncContextAccessor target = new MockAsyncContextAccessor();

        Trace trace = mock(Trace.class);
        SpanEventRecorder recorder = mock(SpanEventRecorder.class);
        AsyncContext boundary = mock(AsyncContext.class);
        when(traceContext.currentTraceObject()).thenReturn(trace);
        when(trace.traceBlockBegin()).thenReturn(recorder);
        when(recorder.recordNextAsyncContext()).thenReturn(boundary);

        interceptor.after(target, new Object[]{mock(Runnable.class)}, null, null);

        assertSame(boundary, target._$PINPOINT$_getAsyncContext());
        verify(recorder).recordServiceType(serviceType);
        verify(recorder).recordApi(methodDescriptor);
        verify(trace).traceBlockEnd();
    }

    @Test
    public void noCarrierAndNoCurrentTrace_capturesNothing() {
        MockAsyncContextAccessor target = new MockAsyncContextAccessor();
        when(traceContext.currentTraceObject()).thenReturn(null);

        interceptor.after(target, new Object[]{mock(Runnable.class)}, null, null);

        assertNull(target._$PINPOINT$_getAsyncContext());
    }

    @Test
    public void recorderFailure_stillEndsTraceBlock_andDoesNotPropagate() {
        MockAsyncContextAccessor target = new MockAsyncContextAccessor();

        Trace trace = mock(Trace.class);
        SpanEventRecorder recorder = mock(SpanEventRecorder.class);
        when(traceContext.currentTraceObject()).thenReturn(trace);
        when(trace.traceBlockBegin()).thenReturn(recorder);
        when(recorder.recordNextAsyncContext()).thenThrow(new IllegalStateException("recorder failed"));

        interceptor.after(target, new Object[]{mock(Runnable.class)}, null, null);

        assertNull(target._$PINPOINT$_getAsyncContext());
        verify(trace).traceBlockEnd();
    }

    @Test
    public void nonAccessorTarget_isHarmless() {
        Trace trace = mock(Trace.class);
        SpanEventRecorder recorder = mock(SpanEventRecorder.class);
        when(traceContext.currentTraceObject()).thenReturn(trace);
        when(trace.traceBlockBegin()).thenReturn(recorder);
        when(recorder.recordNextAsyncContext()).thenReturn(mock(AsyncContext.class));

        // a target without the injected field (mispaired transform) must not break the app.
        interceptor.after(new Object(), new Object[]{mock(Runnable.class)}, null, null);

        verify(trace).traceBlockEnd();
        verify(trace, never()).close();
    }
}
