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

import com.navercorp.pinpoint.bootstrap.config.DefaultProfilerConfig;
import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.context.Header;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.context.TraceId;
import com.navercorp.pinpoint.bootstrap.sampler.SamplingFlagUtils;
import com.navercorp.pinpoint.plugin.ktor.KtorConstants;
import com.navercorp.pinpoint.plugin.ktor.KtorTestServiceTypes;
import io.ktor.client.request.HttpRequestBuilder;
import io.ktor.http.URLProtocol;
import kotlin.coroutines.intrinsics.IntrinsicsKt;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class KtorClientSendInterceptorTest {
    @BeforeAll
    static void registerServiceTypes() {
        // registrations come from this plugin's own type-provider.yml
        KtorTestServiceTypes.register();
    }

    private final TraceContext traceContext = mock(TraceContext.class);
    private final Trace trace = mock(Trace.class);
    private final TraceId traceId = mock(TraceId.class);
    private final TraceId nextId = mock(TraceId.class);
    private final SpanEventRecorder recorder = mock(SpanEventRecorder.class);
    private final AsyncContext asyncContext = mock(AsyncContext.class);
    private final MethodDescriptor methodDescriptor = mock(MethodDescriptor.class);

    private KtorClientSendInterceptor newInterceptor() {
        short serverTypeCode = KtorConstants.KTOR.getCode();
        when(traceContext.getProfilerConfig()).thenReturn(new DefaultProfilerConfig());
        when(traceContext.getApplicationName()).thenReturn("test-app");
        when(traceContext.getServerTypeCode()).thenReturn(serverTypeCode);
        return new KtorClientSendInterceptor(traceContext, methodDescriptor);
    }

    @AfterEach
    void cleanup() {
        KtorClientTraceStorage.clearPending();
    }

    @Test
    void tracedCallWritesHeadersAndClosesOnReturn() {
        KtorClientSendInterceptor interceptor = newInterceptor();
        startBlock();
        when(trace.currentSpanEventRecorder()).thenReturn(recorder);
        HttpRequestBuilder request = newRequest();

        interceptor.before(this, new Object[]{request});
        interceptor.after(this, new Object[]{request}, new Object(), null);

        verify(recorder, times(1)).recordNextSpanId(100L);
        verify(recorder, times(1)).recordServiceType(KtorConstants.KTOR_CLIENT);
        verify(recorder, times(1)).recordNextAsyncContext(true);
        verify(recorder, times(1)).recordDestinationId("api.example.com:8443");
        verify(recorder, times(1)).recordApi(methodDescriptor);
        verify(recorder, times(1)).recordException(true, null);
        verify(trace, times(1)).traceBlockEnd();
        // synchronous completion cancels the async span
        verify(asyncContext, times(1)).finish();
        assertHeader(request, Header.HTTP_TRACE_ID, "ktor-app^1710000000000^7");
        assertHeader(request, Header.HTTP_SPAN_ID, "100");
        assertHeader(request, Header.HTTP_PARENT_SPAN_ID, "99");
        assertHeader(request, Header.HTTP_FLAGS, "0");
        assertHeader(request, Header.HTTP_PARENT_APPLICATION_NAME, "test-app");
        assertHeader(request, Header.HTTP_PARENT_APPLICATION_TYPE, "1160");
        assertHeader(request, Header.HTTP_HOST, "api.example.com:8443");
        assertNull(KtorClientTraceStorage.getPending());
    }

    @Test
    void thrownErrorIsRecordedOnCompletion() {
        KtorClientSendInterceptor interceptor = newInterceptor();
        startBlock();
        when(trace.currentSpanEventRecorder()).thenReturn(recorder);
        HttpRequestBuilder request = newRequest();
        Throwable throwable = new IllegalStateException("send boom");

        interceptor.before(this, new Object[]{request});
        interceptor.after(this, new Object[]{request}, null, throwable);

        verify(recorder, times(1)).recordException(true, throwable);
        verify(trace, times(1)).traceBlockEnd();
        verify(asyncContext, times(1)).finish();
    }

    @Test
    void suspendedCallKeepsAsyncTraceOpen() {
        KtorClientSendInterceptor interceptor = newInterceptor();
        startBlock();
        when(trace.currentSpanEventRecorder()).thenReturn(recorder);
        HttpRequestBuilder request = newRequest();

        interceptor.before(this, new Object[]{request});
        // the continuation constructor binds the pending holder to the continuation
        KtorClientTraceHolder holder = KtorClientTraceStorage.getPending();
        holder.markAttached();

        interceptor.after(this, new Object[]{request}, IntrinsicsKt.getCOROUTINE_SUSPENDED(), null);

        verify(trace, times(1)).traceBlockEnd();
        verify(recorder, times(1)).recordException(true, null);
        // the suspended continuation finishes the async trace later
        verify(asyncContext, never()).finish();
        assertNull(KtorClientTraceStorage.getPending());

        holder.cancelAsync();
        verify(asyncContext, times(1)).finish();
    }

    @Test
    void unsampledCallWritesSamplingHeaderOnly() {
        KtorClientSendInterceptor interceptor = newInterceptor();
        when(traceContext.currentRawTraceObject()).thenReturn(trace);
        when(trace.canSampled()).thenReturn(false);
        HttpRequestBuilder request = newRequest();

        interceptor.before(this, new Object[]{request});
        interceptor.after(this, new Object[]{request}, new Object(), null);

        assertHeader(request, Header.HTTP_SAMPLED, SamplingFlagUtils.SAMPLING_RATE_FALSE);
        verify(trace, never()).traceBlockBegin();
        verify(trace, never()).traceBlockEnd();
    }

    @Test
    void noActiveTraceSkipsInstrumentation() {
        KtorClientSendInterceptor interceptor = newInterceptor();
        when(traceContext.currentRawTraceObject()).thenReturn(null);
        HttpRequestBuilder request = newRequest();

        interceptor.before(this, new Object[]{request});
        interceptor.after(this, new Object[]{request}, new Object(), null);

        assertNull(KtorClientTraceStorage.getPending());
        assertHeader(request, Header.HTTP_TRACE_ID, null);
        assertHeader(request, Header.HTTP_SAMPLED, null);
    }

    @Test
    void nonRequestArgumentSkipsInstrumentation() {
        KtorClientSendInterceptor interceptor = newInterceptor();
        when(traceContext.currentRawTraceObject()).thenReturn(trace);

        interceptor.before(this, null);
        interceptor.before(this, new Object[0]);
        interceptor.before(this, new Object[]{new Object()});

        verify(trace, never()).traceBlockBegin();
        assertNull(KtorClientTraceStorage.getPending());
    }

    @Test
    void headerWriteFailureClosesTraceBlock() {
        KtorClientSendInterceptor interceptor = newInterceptor();
        when(traceContext.currentRawTraceObject()).thenReturn(trace);
        when(trace.canSampled()).thenReturn(true);
        when(trace.traceBlockBegin()).thenReturn(recorder);
        when(trace.getTraceId()).thenReturn(traceId);
        // a null next id fails the span id recording after the block has started
        when(traceId.getNextTraceId()).thenReturn(null);
        HttpRequestBuilder request = newRequest();

        interceptor.before(this, new Object[]{request});
        interceptor.after(this, new Object[]{request}, new Object(), null);

        verify(trace, times(1)).traceBlockEnd();
        verify(recorder, never()).recordNextAsyncContext(true);
        assertNull(KtorClientTraceStorage.getPending());
    }

    @Test
    void afterWithoutBeforeIsNoOp() {
        KtorClientSendInterceptor interceptor = newInterceptor();

        interceptor.after(this, new Object[]{newRequest()}, new Object(), null);

        verify(trace, never()).traceBlockEnd();
    }

    @Test
    void nullRequestArgumentSkipsInstrumentation() {
        KtorClientSendInterceptor interceptor = newInterceptor();
        when(traceContext.currentRawTraceObject()).thenReturn(trace);

        interceptor.before(this, new Object[]{null});

        verify(trace, never()).traceBlockBegin();
        assertNull(KtorClientTraceStorage.getPending());
    }

    @Test
    void suspendedWithoutAttachedContinuationFinishesSynchronously() {
        KtorClientSendInterceptor interceptor = newInterceptor();
        startBlock();
        when(trace.currentSpanEventRecorder()).thenReturn(recorder);
        HttpRequestBuilder request = newRequest();

        interceptor.before(this, new Object[]{request});
        // no continuation constructor ran, so the holder is not attached
        interceptor.after(this, new Object[]{request}, IntrinsicsKt.getCOROUTINE_SUSPENDED(), null);

        verify(trace, times(1)).traceBlockEnd();
        verify(asyncContext, times(1)).finish();
    }

    @Test
    void headerWriteFailureSwallowsBrokenBlockEnd() {
        KtorClientSendInterceptor interceptor = newInterceptor();
        when(traceContext.currentRawTraceObject()).thenReturn(trace);
        when(trace.canSampled()).thenReturn(true);
        when(trace.traceBlockBegin()).thenReturn(recorder);
        when(trace.getTraceId()).thenReturn(traceId);
        when(traceId.getNextTraceId()).thenReturn(null);
        doThrow(new IllegalStateException("traceBlockEnd boom")).when(trace).traceBlockEnd();
        HttpRequestBuilder request = newRequest();

        interceptor.before(this, new Object[]{request});
        interceptor.after(this, new Object[]{request}, new Object(), null);

        verify(trace, times(1)).traceBlockEnd();
        assertNull(KtorClientTraceStorage.getPending());
    }

    @Test
    void reentrantBeforeDropsPreviousState() {
        KtorClientSendInterceptor interceptor = newInterceptor();
        startBlock();
        when(trace.currentSpanEventRecorder()).thenReturn(recorder);
        HttpRequestBuilder first = newRequest();
        HttpRequestBuilder second = newRequest();

        interceptor.before(this, new Object[]{first});
        // a nested send invocation on the same thread starts without the previous after()
        interceptor.before(this, new Object[]{second});
        interceptor.after(this, new Object[]{second}, new Object(), null);

        verify(trace, times(2)).traceBlockBegin();
        // only the latest state is finished; the dropped one must not double-close the trace
        verify(trace, times(1)).traceBlockEnd();
        verify(asyncContext, times(1)).finish();
        assertNull(KtorClientTraceStorage.getPending());
    }

    private void startBlock() {
        when(traceContext.currentRawTraceObject()).thenReturn(trace);
        when(trace.canSampled()).thenReturn(true);
        when(trace.traceBlockBegin()).thenReturn(recorder);
        when(trace.getTraceId()).thenReturn(traceId);
        when(traceId.getNextTraceId()).thenReturn(nextId);
        when(nextId.getSpanId()).thenReturn(100L);
        when(nextId.getParentSpanId()).thenReturn(99L);
        when(nextId.getFlags()).thenReturn((short) 0);
        when(nextId.getTransactionId()).thenReturn("ktor-app^1710000000000^7");
        when(recorder.recordNextAsyncContext(true)).thenReturn(asyncContext);
    }

    private HttpRequestBuilder newRequest() {
        HttpRequestBuilder request = new HttpRequestBuilder();
        request.getUrl().setProtocol(URLProtocol.Companion.getHTTPS());
        request.getUrl().setHost("api.example.com");
        request.getUrl().setPort(8443);
        request.getUrl().setPathSegments(java.util.Arrays.asList("v1", "images"));
        return request;
    }

    private void assertHeader(HttpRequestBuilder request, Header header, String expected) {
        assertEquals(expected, request.getHeaders().get(header.toString()), header.toString());
    }
}
