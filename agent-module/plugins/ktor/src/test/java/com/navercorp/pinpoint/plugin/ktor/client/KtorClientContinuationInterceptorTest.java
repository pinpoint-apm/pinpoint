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

package com.navercorp.pinpoint.plugin.ktor.client;

import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import kotlin.coroutines.intrinsics.IntrinsicsKt;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class KtorClientContinuationInterceptorTest {

    @Test
    void resumedResultRecordsCompletionOnCurrentTraceAndClearsAccessor() {
        Trace trace = mock(Trace.class);
        TraceContext traceContext = mock(TraceContext.class);
        when(traceContext.currentRawTraceObject()).thenReturn(trace);
        KtorClientTraceHolder holder = mock(KtorClientTraceHolder.class);
        TestAccessor target = new TestAccessor(holder);

        new KtorClientContinuationInterceptor(traceContext).after(target, new Object[0], new Object(), null);

        verify(holder, times(1)).recordCompletion(trace, null);
        assertNull(target.trace);
    }

    @Test
    void nullCurrentTraceIsPassedThroughToHolder() {
        TraceContext traceContext = mock(TraceContext.class);
        when(traceContext.currentRawTraceObject()).thenReturn(null);
        KtorClientTraceHolder holder = mock(KtorClientTraceHolder.class);
        TestAccessor target = new TestAccessor(holder);

        new KtorClientContinuationInterceptor(traceContext).after(target, new Object[0], new Object(), null);

        verify(holder, times(1)).recordCompletion(null, null);
        assertNull(target.trace);
    }

    @Test
    void throwableIsForwardedToHolder() {
        Trace trace = mock(Trace.class);
        TraceContext traceContext = mock(TraceContext.class);
        when(traceContext.currentRawTraceObject()).thenReturn(trace);
        KtorClientTraceHolder holder = mock(KtorClientTraceHolder.class);
        TestAccessor target = new TestAccessor(holder);
        Throwable throwable = new IllegalStateException("boom");

        new KtorClientContinuationInterceptor(traceContext).after(target, new Object[0], new Object(), throwable);

        verify(holder, times(1)).recordCompletion(trace, throwable);
    }

    @Test
    void suspendedMarkerReturnsSkipsFinishButKeepsHolder() {
        TraceContext traceContext = mock(TraceContext.class);
        KtorClientTraceHolder holder = mock(KtorClientTraceHolder.class);
        TestAccessor target = new TestAccessor(holder);
        Object marker = IntrinsicsKt.getCOROUTINE_SUSPENDED();

        new KtorClientContinuationInterceptor(traceContext).after(target, new Object[0], marker, null);

        verifyNoInteractions(traceContext);
        verify(holder, never()).recordCompletion(any(), any());
        assertSame(holder, target.trace);
    }

    @Test
    void missingHolderIsNoOp() {
        TraceContext traceContext = mock(TraceContext.class);
        TestAccessor target = new TestAccessor(null);

        new KtorClientContinuationInterceptor(traceContext).after(target, new Object[0], new Object(), null);

        verifyNoInteractions(traceContext);
        assertNull(target.trace);
    }

    @Test
    void nonAccessorTargetIsIgnored() {
        TraceContext traceContext = mock(TraceContext.class);

        new KtorClientContinuationInterceptor(traceContext).after(new Object(), new Object[0], new Object(), null);

        verifyNoInteractions(traceContext);
    }

    @Test
    void holderThrowingDoesNotPropagateAndClearsAccessor() {
        TraceContext traceContext = mock(TraceContext.class);
        KtorClientTraceHolder holder = mock(KtorClientTraceHolder.class);
        doThrow(new IllegalStateException("recordCompletion boom")).when(holder).recordCompletion(null, null);
        TestAccessor target = new TestAccessor(holder);

        new KtorClientContinuationInterceptor(traceContext).after(target, new Object[0], new Object(), null);

        assertNull(target.trace);
    }

    @Test
    void currentTraceLookupFailureIsSwallowedAndAccessorCleared() {
        TraceContext traceContext = mock(TraceContext.class);
        when(traceContext.currentRawTraceObject()).thenThrow(new IllegalStateException("lookup boom"));
        KtorClientTraceHolder holder = mock(KtorClientTraceHolder.class);
        TestAccessor target = new TestAccessor(holder);

        new KtorClientContinuationInterceptor(traceContext).after(target, new Object[0], new Object(), null);

        verify(holder, never()).recordCompletion(any(), any());
        assertNull(target.trace);
    }

    private static class TestAccessor implements KtorClientTraceAccessor {
        KtorClientTraceHolder trace;

        TestAccessor(KtorClientTraceHolder trace) {
            this.trace = trace;
        }

        @Override
        public void _$PINPOINT$_setKtorClientTrace(KtorClientTraceHolder holder) {
            this.trace = holder;
        }

        @Override
        public KtorClientTraceHolder _$PINPOINT$_getKtorClientTrace() {
            return trace;
        }
    }
}
