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

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfigLoader;
import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.context.scope.TraceScope;
import com.navercorp.pinpoint.bootstrap.util.ScopeUtils;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

public class RetrySubscriberResubscribeInterceptorTest {
    private static final int API_ID = 99;
    private static final Object[] ARGS = new Object[0];

    @Test
    public void sameSeedCanOpenAndCloseTwoResubscribeWindows() {
        TraceContext traceContext = traceContext(true);
        RetrySubscriberResubscribeInterceptor interceptor = new RetrySubscriberResubscribeInterceptor(traceContext);
        AsyncContext seed = mock(AsyncContext.class);
        Trace firstTrace = asyncTrace();
        Trace secondTrace = asyncTrace();
        when(seed.continueAsyncTraceObject(false)).thenReturn(firstTrace, secondTrace);
        when(seed.currentAsyncTraceObject()).thenReturn(firstTrace, secondTrace);

        execute(interceptor, seed);
        execute(interceptor, seed);

        verify(seed, times(2)).continueAsyncTraceObject(false);
        verify(seed, times(2)).close();
        verify(firstTrace).close();
        verify(secondTrace).close();
    }

    @Test
    public void nestedResubscribeClosesOnlyOutermostWindow() {
        TraceContext traceContext = traceContext(true);
        RetrySubscriberResubscribeInterceptor interceptor = new RetrySubscriberResubscribeInterceptor(traceContext);
        AsyncContext seed = mock(AsyncContext.class);
        Trace trace = mock(Trace.class);
        TraceScope scope = mock(TraceScope.class);
        when(seed.continueAsyncTraceObject(false)).thenReturn(trace);
        when(seed.currentAsyncTraceObject()).thenReturn(trace);
        when(trace.getScope(ScopeUtils.ASYNC_TRACE_SCOPE)).thenReturn(scope);
        when(trace.isAsync()).thenReturn(true);
        when(scope.canLeave()).thenReturn(true);
        // The inner after leaves one nested level; only the outer after reaches the end scope.
        when(scope.isActive()).thenReturn(true, false);

        Object target = new Object();
        interceptor.before(target, seed, API_ID, ARGS);
        interceptor.before(target, seed, API_ID, ARGS);
        interceptor.after(target, seed, API_ID, ARGS, null, null);
        interceptor.after(target, seed, API_ID, ARGS, null, null);

        verify(seed, times(2)).continueAsyncTraceObject(false);
        verify(seed).close();
        verify(trace).close();
    }

    @Test
    public void resubscribeFailureStillClosesWindow() {
        TraceContext traceContext = traceContext(true);
        RetrySubscriberResubscribeInterceptor interceptor = new RetrySubscriberResubscribeInterceptor(traceContext);
        AsyncContext seed = mock(AsyncContext.class);
        Trace trace = asyncTrace();
        when(seed.continueAsyncTraceObject(false)).thenReturn(trace);
        when(seed.currentAsyncTraceObject()).thenReturn(trace);

        Object target = new Object();
        interceptor.before(target, seed, API_ID, ARGS);
        interceptor.after(target, seed, API_ID, ARGS, null, new RuntimeException("subscribe failed"));

        verify(seed).close();
        verify(trace).close();
    }

    @Test
    public void traceCloseFailureStillUnbindsAndDoesNotEscape() {
        TraceContext traceContext = traceContext(true);
        RetrySubscriberResubscribeInterceptor interceptor = new RetrySubscriberResubscribeInterceptor(traceContext);
        AsyncContext seed = mock(AsyncContext.class);
        Trace trace = asyncTrace();
        when(seed.continueAsyncTraceObject(false)).thenReturn(trace);
        when(seed.currentAsyncTraceObject()).thenReturn(trace);
        doThrow(new IllegalStateException("close failed")).when(trace).close();

        assertDoesNotThrow(() -> execute(interceptor, seed));

        verify(seed).close();
    }

    @Test
    public void unbindFailureDoesNotEscapeAfterTraceClose() {
        TraceContext traceContext = traceContext(true);
        RetrySubscriberResubscribeInterceptor interceptor = new RetrySubscriberResubscribeInterceptor(traceContext);
        AsyncContext seed = mock(AsyncContext.class);
        Trace trace = asyncTrace();
        when(seed.continueAsyncTraceObject(false)).thenReturn(trace);
        when(seed.currentAsyncTraceObject()).thenReturn(trace);
        doThrow(new IllegalStateException("unbind failed")).when(seed).close();

        assertDoesNotThrow(() -> execute(interceptor, seed));

        verify(trace).close();
    }

    @Test
    public void retryTracingDisabledLeavesSeedUntouched() {
        TraceContext traceContext = traceContext(false);
        RetrySubscriberResubscribeInterceptor interceptor = new RetrySubscriberResubscribeInterceptor(traceContext);
        AsyncContext seed = mock(AsyncContext.class);

        execute(interceptor, seed);

        verifyNoInteractions(seed);
    }

    @Test
    public void missingSeedIsNoop() {
        TraceContext traceContext = traceContext(true);
        RetrySubscriberResubscribeInterceptor interceptor = new RetrySubscriberResubscribeInterceptor(traceContext);

        execute(interceptor, null);
    }

    private void execute(RetrySubscriberResubscribeInterceptor interceptor, AsyncContext seed) {
        Object target = new Object();
        interceptor.before(target, seed, API_ID, ARGS);
        interceptor.after(target, seed, API_ID, ARGS, null, null);
    }

    private Trace asyncTrace() {
        Trace trace = mock(Trace.class);
        TraceScope scope = mock(TraceScope.class);
        when(trace.getScope(ScopeUtils.ASYNC_TRACE_SCOPE)).thenReturn(scope);
        when(trace.isAsync()).thenReturn(true);
        when(scope.canLeave()).thenReturn(true);
        when(scope.isActive()).thenReturn(false);
        return trace;
    }

    private TraceContext traceContext(boolean traceRetry) {
        Properties properties = new Properties();
        properties.put("profiler.reactor.trace.retry", Boolean.toString(traceRetry));
        TraceContext traceContext = mock(TraceContext.class);
        when(traceContext.getProfilerConfig()).thenReturn(ProfilerConfigLoader.load(properties));
        return traceContext;
    }
}
