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

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.InterceptorScope;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.InterceptorScopeInvocation;
import com.navercorp.pinpoint.common.trace.ServiceType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PeriodicSchedulerTaskRunInterceptorTest {
    private static final Object[] ARGS = new Object[0];

    private TraceContext traceContext;
    private MethodDescriptor methodDescriptor;
    private ServiceType serviceType;
    private InterceptorScopeInvocation invocation;
    private PeriodicSchedulerTaskRunInterceptor interceptor;

    @BeforeEach
    public void setUp() {
        traceContext = mock(TraceContext.class);
        methodDescriptor = mock(MethodDescriptor.class);
        serviceType = mock(ServiceType.class);

        InterceptorScope scope = mock(InterceptorScope.class);
        invocation = mock(InterceptorScopeInvocation.class);
        when(scope.getCurrentInvocation()).thenReturn(invocation);

        AtomicReference<Object> attachment = new AtomicReference<>();
        when(invocation.setAttachment(any())).thenAnswer(invocation -> attachment.getAndSet(invocation.getArgument(0)));
        when(invocation.getAttachment()).thenAnswer(invocation -> attachment.get());
        when(invocation.removeAttachment()).thenAnswer(invocation -> attachment.getAndSet(null));

        interceptor = new PeriodicSchedulerTaskRunInterceptor(traceContext, methodDescriptor, serviceType, scope);
    }

    @Test
    public void eachExecutionCreatesAndClosesIndependentRoot() {
        Trace periodicTrace = sampledTrace();
        SpanRecorder recorder = periodicTrace.getSpanRecorder();
        when(traceContext.newTraceObject()).thenReturn(periodicTrace);

        RuntimeException taskFailure = new RuntimeException("tick failed");
        interceptor.before(new Object(), ARGS);
        interceptor.after(new Object(), ARGS, null, taskFailure);

        verify(traceContext).newTraceObject();
        verify(recorder).recordServiceType(serviceType);
        verify(recorder).recordApi(methodDescriptor);
        verify(recorder).recordRpcName("Reactor periodic scheduler task");
        verify(recorder).recordEndPoint("LOCAL");
        verify(recorder).recordRemoteAddress("LOCAL");
        verify(recorder).recordException(taskFailure);
        verify(traceContext).removeTraceObject(false);
        verify(periodicTrace).close();
    }

    @Test
    public void ambientTraceIsSuspendedWithoutCloseAndRestored() {
        Trace ambientTrace = mock(Trace.class);
        Trace periodicTrace = sampledTrace();
        when(traceContext.currentRawTraceObject()).thenReturn(ambientTrace);
        when(traceContext.removeTraceObject(false)).thenReturn(ambientTrace, periodicTrace);
        when(traceContext.newTraceObject()).thenReturn(periodicTrace);

        interceptor.before(new Object(), ARGS);
        interceptor.after(new Object(), ARGS, null, null);

        verify(traceContext, times(2)).removeTraceObject(false);
        verify(ambientTrace, never()).close();
        verify(periodicTrace).close();
        verify(traceContext).continueTraceObject(ambientTrace);
    }

    @Test
    public void unsampledPeriodicTraceStillCloses() {
        Trace periodicTrace = mock(Trace.class);
        when(periodicTrace.canSampled()).thenReturn(false);
        when(traceContext.newTraceObject()).thenReturn(periodicTrace);

        interceptor.before(new Object(), ARGS);
        interceptor.after(new Object(), ARGS, null, null);

        verify(periodicTrace, never()).getSpanRecorder();
        verify(periodicTrace).close();
    }

    @Test
    public void rootRecordingFailureClosesPeriodicTraceAndRestoresAmbient() {
        Trace ambientTrace = mock(Trace.class);
        Trace periodicTrace = sampledTrace();
        SpanRecorder recorder = periodicTrace.getSpanRecorder();
        when(traceContext.currentRawTraceObject()).thenReturn(ambientTrace);
        when(traceContext.removeTraceObject(false)).thenReturn(ambientTrace, periodicTrace);
        when(traceContext.newTraceObject()).thenReturn(periodicTrace);
        doThrow(new IllegalStateException("record failed")).when(recorder).recordApi(methodDescriptor);

        interceptor.before(new Object(), ARGS);
        interceptor.after(new Object(), ARGS, null, null);

        verify(periodicTrace).close();
        verify(traceContext).continueTraceObject(ambientTrace);
        verify(recorder, never()).recordException(any());
    }

    @Test
    public void afterRecordingFailureStillClosesAndRestores() {
        Trace ambientTrace = mock(Trace.class);
        Trace periodicTrace = sampledTrace();
        SpanRecorder recorder = periodicTrace.getSpanRecorder();
        when(traceContext.currentRawTraceObject()).thenReturn(ambientTrace);
        when(traceContext.removeTraceObject(false)).thenReturn(ambientTrace, periodicTrace);
        when(traceContext.newTraceObject()).thenReturn(periodicTrace);
        doThrow(new IllegalStateException("record failed")).when(recorder).recordException(any());

        interceptor.before(new Object(), ARGS);
        interceptor.after(new Object(), ARGS, null, new RuntimeException("tick failed"));

        verify(periodicTrace).close();
        verify(traceContext).continueTraceObject(ambientTrace);
    }

    @Test
    public void closeFailureStillRestoresAmbient() {
        Trace ambientTrace = mock(Trace.class);
        Trace periodicTrace = sampledTrace();
        when(traceContext.currentRawTraceObject()).thenReturn(ambientTrace);
        when(traceContext.removeTraceObject(false)).thenReturn(ambientTrace, periodicTrace);
        when(traceContext.newTraceObject()).thenReturn(periodicTrace);
        doThrow(new IllegalStateException("close failed")).when(periodicTrace).close();

        interceptor.before(new Object(), ARGS);
        interceptor.after(new Object(), ARGS, null, null);

        verify(traceContext).continueTraceObject(ambientTrace);
    }

    @Test
    public void traceCreationFailureStillRestoresAmbient() {
        Trace ambientTrace = mock(Trace.class);
        when(traceContext.currentRawTraceObject()).thenReturn(ambientTrace);
        when(traceContext.removeTraceObject(false)).thenReturn(ambientTrace);
        when(traceContext.newTraceObject()).thenThrow(new IllegalStateException("create failed"));

        assertDoesNotThrow(() -> interceptor.before(new Object(), ARGS));
        assertDoesNotThrow(() -> interceptor.after(new Object(), ARGS, null, null));

        verify(traceContext).continueTraceObject(ambientTrace);
    }

    @Test
    public void unbindFailureStillClosesAndAttemptsRestore() {
        Trace ambientTrace = mock(Trace.class);
        Trace periodicTrace = sampledTrace();
        when(traceContext.currentRawTraceObject()).thenReturn(ambientTrace);
        when(traceContext.removeTraceObject(false))
                .thenReturn(ambientTrace)
                .thenThrow(new IllegalStateException("unbind failed"));
        when(traceContext.newTraceObject()).thenReturn(periodicTrace);

        assertDoesNotThrow(() -> interceptor.before(new Object(), ARGS));
        assertDoesNotThrow(() -> interceptor.after(new Object(), ARGS, null, null));

        verify(periodicTrace).close();
        verify(traceContext).continueTraceObject(ambientTrace);
    }

    @Test
    public void restoreFailureDoesNotEscapeTaskCompletion() {
        Trace ambientTrace = mock(Trace.class);
        Trace periodicTrace = sampledTrace();
        when(traceContext.currentRawTraceObject()).thenReturn(ambientTrace);
        when(traceContext.removeTraceObject(false)).thenReturn(ambientTrace, periodicTrace);
        when(traceContext.newTraceObject()).thenReturn(periodicTrace);
        when(traceContext.continueTraceObject(ambientTrace)).thenThrow(new IllegalStateException("restore failed"));

        interceptor.before(new Object(), ARGS);

        assertDoesNotThrow(() -> interceptor.after(new Object(), ARGS, null, null));
        verify(periodicTrace).close();
    }

    private Trace sampledTrace() {
        Trace trace = mock(Trace.class);
        SpanRecorder recorder = mock(SpanRecorder.class);
        when(trace.canSampled()).thenReturn(true);
        when(trace.getSpanRecorder()).thenReturn(recorder);
        return trace;
    }
}
