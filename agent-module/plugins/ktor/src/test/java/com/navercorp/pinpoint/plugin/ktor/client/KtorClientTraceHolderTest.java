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

import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.plugin.request.ClientRequestRecorder;
import com.navercorp.pinpoint.plugin.ktor.KtorConstants;
import com.navercorp.pinpoint.plugin.ktor.KtorPluginConfig;
import com.navercorp.pinpoint.plugin.ktor.KtorTestServiceTypes;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class KtorClientTraceHolderTest {
    @BeforeAll
    static void registerServiceTypes() {
        // registrations come from this plugin's own type-provider.yml
        KtorTestServiceTypes.register();
    }

    private final AsyncContext asyncContext = mock(AsyncContext.class);
    private final Object request = new Object();
    private final MethodDescriptor methodDescriptor = mock(MethodDescriptor.class);
    private final KtorPluginConfig config = mock(KtorPluginConfig.class);
    @SuppressWarnings("unchecked")
    private final ClientRequestRecorder<Object> requestRecorder = mock(ClientRequestRecorder.class);
    private final SpanEventRecorder recorder = mock(SpanEventRecorder.class);
    private final Trace resumeTrace = mock(Trace.class);

    private KtorClientTraceHolder newHolder() {
        return new KtorClientTraceHolder(asyncContext, request, methodDescriptor, config, requestRecorder);
    }

    @Test
    void attachedState() {
        KtorClientTraceHolder holder = newHolder();

        assertFalse(holder.isAttached());
        holder.markAttached();
        assertTrue(holder.isAttached());
    }

    @Test
    void recordWithNullRecorderSkipsRecording() {
        KtorClientTraceHolder holder = newHolder();

        holder.record(null, null);

        verifyNoInteractions(requestRecorder);
    }

    @Test
    void recordWritesRequestApiAndException() {
        when(config.isClientMarkError()).thenReturn(true);
        KtorClientTraceHolder holder = newHolder();
        Throwable throwable = new IllegalStateException("boom");

        holder.record(recorder, throwable);

        verify(requestRecorder, times(1)).record(recorder, request, throwable);
        verify(recorder, times(1)).recordApi(methodDescriptor);
        verify(recorder, times(1)).recordException(true, throwable);
    }

    @Test
    void recordHonoursMarkErrorFlag() {
        when(config.isClientMarkError()).thenReturn(false);
        KtorClientTraceHolder holder = newHolder();

        holder.record(recorder, null);

        verify(recorder, times(1)).recordException(false, null);
    }

    @Test
    void recordFailureIsSwallowed() {
        KtorClientTraceHolder holder = newHolder();
        doThrow(new IllegalStateException("record boom")).when(requestRecorder).record(recorder, request, null);

        holder.record(recorder, null);

        verify(recorder, never()).recordApi(methodDescriptor);
    }

    @Test
    void recordCompletionRecordsOnGivenTraceWithoutClosingIt() {
        startResumeTrace();
        KtorClientTraceHolder holder = newHolder();
        Throwable throwable = new IllegalStateException("boom");

        holder.recordCompletion(resumeTrace, throwable);

        verify(resumeTrace, times(1)).traceBlockBegin();
        verify(recorder, times(1)).recordServiceType(KtorConstants.KTOR_CLIENT_INTERNAL);
        verify(requestRecorder, times(1)).record(recorder, request, throwable);
        verify(recorder, times(1)).recordApi(methodDescriptor);
        verify(resumeTrace, times(1)).traceBlockEnd();
        verify(resumeTrace, never()).close();
        verify(asyncContext, times(1)).finish();
        verifyNoMoreInteractions(asyncContext);
    }

    @Test
    void recordCompletionWithoutLiveTraceFallsBackToOwnedAsyncTrace() {
        when(asyncContext.continueAsyncTraceObject(true)).thenReturn(resumeTrace);
        when(resumeTrace.traceBlockBegin()).thenReturn(recorder);
        KtorClientTraceHolder holder = newHolder();

        holder.recordCompletion(null, null);

        verify(asyncContext, times(1)).continueAsyncTraceObject(true);
        verify(recorder, times(1)).recordServiceType(KtorConstants.KTOR_CLIENT_INTERNAL);
        verify(resumeTrace, times(1)).traceBlockEnd();
        verify(resumeTrace, times(1)).close();
        verify(asyncContext, times(1)).close();
        verify(asyncContext, times(1)).finish();
    }

    @Test
    void fallbackFailureIsSwallowedAndStillClosesOwnedTrace() {
        when(asyncContext.continueAsyncTraceObject(true)).thenReturn(resumeTrace);
        when(resumeTrace.traceBlockBegin()).thenThrow(new IllegalStateException("begin boom"));
        KtorClientTraceHolder holder = newHolder();

        holder.recordCompletion(null, null);

        verify(resumeTrace, never()).traceBlockEnd();
        verify(resumeTrace, times(1)).close();
        verify(asyncContext, times(1)).close();
        verify(asyncContext, times(1)).finish();
    }

    @Test
    void fallbackWhenContinueReturnsNullOnlyFinishesState() {
        when(asyncContext.continueAsyncTraceObject(true)).thenReturn(null);
        KtorClientTraceHolder holder = newHolder();

        holder.recordCompletion(null, null);

        verifyNoInteractions(requestRecorder);
        verify(asyncContext, times(1)).finish();
        verify(asyncContext, never()).close();
    }

    @Test
    void fallbackCloseFailureStillClosesBinderAndFinishesState() {
        when(asyncContext.continueAsyncTraceObject(true)).thenReturn(resumeTrace);
        when(resumeTrace.traceBlockBegin()).thenReturn(recorder);
        doThrow(new IllegalStateException("close boom")).when(resumeTrace).close();
        KtorClientTraceHolder holder = newHolder();

        holder.recordCompletion(null, null);

        verify(asyncContext, times(1)).close();
        verify(asyncContext, times(1)).finish();
    }

    @Test
    void secondRecordCompletionIsNoOp() {
        startResumeTrace();
        KtorClientTraceHolder holder = newHolder();

        holder.recordCompletion(resumeTrace, null);
        holder.recordCompletion(resumeTrace, null);

        verify(resumeTrace, times(1)).traceBlockBegin();
        verify(asyncContext, times(1)).finish();
    }

    @Test
    void recordFailureIsSwallowedAndStillFinishesState() {
        startResumeTrace();
        doThrow(new IllegalStateException("record boom")).when(requestRecorder).record(recorder, request, null);
        KtorClientTraceHolder holder = newHolder();

        holder.recordCompletion(resumeTrace, null);

        verify(resumeTrace, times(1)).traceBlockEnd();
        verify(asyncContext, times(1)).finish();
    }

    @Test
    void blockBeginFailureStillFinishesState() {
        when(resumeTrace.traceBlockBegin()).thenThrow(new IllegalStateException("begin boom"));
        KtorClientTraceHolder holder = newHolder();

        holder.recordCompletion(resumeTrace, null);

        verify(resumeTrace, never()).traceBlockEnd();
        verify(asyncContext, times(1)).finish();
    }

    @Test
    void blockEndFailureStillFinishesState() {
        startResumeTrace();
        doThrow(new IllegalStateException("end boom")).when(resumeTrace).traceBlockEnd();
        KtorClientTraceHolder holder = newHolder();

        holder.recordCompletion(resumeTrace, null);

        verify(asyncContext, times(1)).finish();
    }

    @Test
    void recordCompletionOnLiveTraceNeverTouchesAsyncBinder() {
        startResumeTrace();
        KtorClientTraceHolder holder = newHolder();

        holder.recordCompletion(resumeTrace, null);

        verify(asyncContext, never()).continueAsyncTraceObject(true);
        verify(asyncContext, never()).close();
    }

    @Test
    void cancelAsyncOnlyFinishesStateAndBlocksFinish() {
        KtorClientTraceHolder holder = newHolder();

        holder.cancelAsync();
        holder.recordCompletion(resumeTrace, null);

        verify(asyncContext, times(1)).finish();
        verifyNoInteractions(resumeTrace);
    }

    @Test
    void cancelAsyncAfterRecordCompletionIsNoOp() {
        startResumeTrace();
        KtorClientTraceHolder holder = newHolder();

        holder.recordCompletion(resumeTrace, null);
        holder.cancelAsync();

        verify(asyncContext, times(1)).finish();
    }

    @Test
    void cancelAsyncSwallowsFinishFailure() {
        doThrow(new IllegalStateException("finish boom")).when(asyncContext).finish();
        KtorClientTraceHolder holder = newHolder();

        holder.cancelAsync();

        verify(asyncContext, times(1)).finish();
    }

    private void startResumeTrace() {
        when(resumeTrace.traceBlockBegin()).thenReturn(recorder);
    }
}
