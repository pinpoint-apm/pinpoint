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

import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class KtorClientParentTraceStateTest {

    @Test
    void finishRecordsThenClosesTrace() {
        Trace trace = mock(Trace.class);
        SpanEventRecorder recorder = mock(SpanEventRecorder.class);
        KtorClientTraceHolder holder = mock(KtorClientTraceHolder.class);
        when(trace.currentSpanEventRecorder()).thenReturn(recorder);

        KtorClientParentTraceState state = new KtorClientParentTraceState(trace, holder);
        state.finish(null);

        verify(trace, times(1)).currentSpanEventRecorder();
        verify(holder, times(1)).record(recorder, null);
        verify(trace, times(1)).traceBlockEnd();
    }

    @Test
    void secondFinishIsNoOp() {
        Trace trace = mock(Trace.class);
        SpanEventRecorder recorder = mock(SpanEventRecorder.class);
        KtorClientTraceHolder holder = mock(KtorClientTraceHolder.class);
        when(trace.currentSpanEventRecorder()).thenReturn(recorder);

        KtorClientParentTraceState state = new KtorClientParentTraceState(trace, holder);
        state.finish(null);
        state.finish(null);

        verify(trace, times(1)).traceBlockEnd();
    }

    @Test
    void recordFailureStillClosesTraceBlock() {
        Trace trace = mock(Trace.class);
        SpanEventRecorder recorder = mock(SpanEventRecorder.class);
        KtorClientTraceHolder holder = mock(KtorClientTraceHolder.class);
        when(trace.currentSpanEventRecorder()).thenReturn(recorder);
        doThrow(new IllegalStateException("record boom")).when(holder).record(recorder, null);

        KtorClientParentTraceState state = new KtorClientParentTraceState(trace, holder);
        state.finish(null);

        verify(trace, times(1)).traceBlockEnd();
    }

    @Test
    void closeTraceBlockSwallowsEndFailure() {
        Trace trace = mock(Trace.class);
        doThrow(new IllegalStateException("traceBlockEnd boom")).when(trace).traceBlockEnd();
        KtorClientTraceHolder holder = mock(KtorClientTraceHolder.class);

        KtorClientParentTraceState state = new KtorClientParentTraceState(trace, holder);

        state.closeTraceBlock();

        verify(trace, times(1)).traceBlockEnd();
        verify(holder, never()).record(null, null);
    }
}
