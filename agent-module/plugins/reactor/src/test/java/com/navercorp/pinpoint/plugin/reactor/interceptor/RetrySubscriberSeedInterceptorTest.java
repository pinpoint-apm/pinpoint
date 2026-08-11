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

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessor;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfigLoader;
import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.common.trace.ServiceType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

public class RetrySubscriberSeedInterceptorTest {
    private TraceContext traceContext;
    private MethodDescriptor methodDescriptor;
    private ServiceType serviceType;

    @BeforeEach
    public void setUp() {
        traceContext = mock(TraceContext.class);
        methodDescriptor = mock(MethodDescriptor.class);
        serviceType = mock(ServiceType.class);
    }

    @Test
    public void failedConstructorDoesNotCreateSeed() {
        RetrySubscriberSeedInterceptor interceptor = newInterceptor(true);
        TestAccessor target = new TestAccessor();

        interceptor.after(target, new Object[0], null, new RuntimeException("constructor failed"));

        assertNull(target._$PINPOINT$_getAsyncContext());
        verify(traceContext, never()).currentTraceObject();
    }

    @Test
    public void inheritedSeedIsKept() {
        RetrySubscriberSeedInterceptor interceptor = newInterceptor(true);
        AsyncContext inherited = mock(AsyncContext.class);
        TestAccessor target = new TestAccessor();
        target._$PINPOINT$_setAsyncContext(inherited);

        interceptor.after(target, new Object[0], null, null);

        assertSame(inherited, target._$PINPOINT$_getAsyncContext());
        verify(traceContext, never()).currentTraceObject();
    }

    @Test
    public void constructorBeforeCopiesUniqueInheritedSeed() {
        RetrySubscriberSeedInterceptor interceptor = newInterceptor(true);
        AsyncContext inherited = mock(AsyncContext.class);
        TestAccessor carrier = new TestAccessor();
        carrier._$PINPOINT$_setAsyncContext(inherited);
        TestAccessor target = new TestAccessor();

        interceptor.before(target, new Object[]{carrier});
        interceptor.after(target, new Object[]{carrier}, null, null);

        assertSame(inherited, target._$PINPOINT$_getAsyncContext());
        verify(traceContext, never()).currentTraceObject();
    }

    @Test
    public void retryTracingDisabledStillPreservesOrdinaryConstructorRelay() {
        RetrySubscriberSeedInterceptor interceptor = newInterceptor(false);
        AsyncContext inherited = mock(AsyncContext.class);
        TestAccessor carrier = new TestAccessor();
        carrier._$PINPOINT$_setAsyncContext(inherited);
        TestAccessor target = new TestAccessor();

        interceptor.before(target, new Object[]{carrier});
        interceptor.after(target, new Object[]{carrier}, null, null);

        assertSame(inherited, target._$PINPOINT$_getAsyncContext());
        verify(traceContext, never()).currentTraceObject();
    }

    @Test
    public void emptySubscriberRecordsSeedFromCurrentTrace() {
        RetrySubscriberSeedInterceptor interceptor = newInterceptor(true);
        TestAccessor target = new TestAccessor();
        Trace trace = mock(Trace.class);
        SpanEventRecorder recorder = mock(SpanEventRecorder.class);
        AsyncContext seed = mock(AsyncContext.class);
        when(traceContext.currentTraceObject()).thenReturn(trace);
        when(trace.traceBlockBegin()).thenReturn(recorder);
        when(recorder.recordNextAsyncContext()).thenReturn(seed);

        interceptor.after(target, new Object[0], null, null);

        assertSame(seed, target._$PINPOINT$_getAsyncContext());
        verify(recorder).recordServiceType(serviceType);
        verify(recorder).recordApi(methodDescriptor);
        verify(trace).traceBlockEnd();
    }

    @Test
    public void retryTracingDisabledDoesNotCreateSeed() {
        RetrySubscriberSeedInterceptor interceptor = newInterceptor(false);
        TestAccessor target = new TestAccessor();

        interceptor.after(target, new Object[0], null, null);

        assertNull(target._$PINPOINT$_getAsyncContext());
        verify(traceContext, never()).currentTraceObject();
    }

    @Test
    public void noCurrentTraceLeavesSubscriberEmpty() {
        RetrySubscriberSeedInterceptor interceptor = newInterceptor(true);
        TestAccessor target = new TestAccessor();
        when(traceContext.currentTraceObject()).thenReturn(null);

        interceptor.after(target, new Object[0], null, null);

        assertNull(target._$PINPOINT$_getAsyncContext());
    }

    @Test
    public void recorderFailureStillEndsTraceBlock() {
        RetrySubscriberSeedInterceptor interceptor = newInterceptor(true);
        TestAccessor target = new TestAccessor();
        Trace trace = mock(Trace.class);
        SpanEventRecorder recorder = mock(SpanEventRecorder.class);
        when(traceContext.currentTraceObject()).thenReturn(trace);
        when(trace.traceBlockBegin()).thenReturn(recorder);
        when(recorder.recordNextAsyncContext()).thenThrow(new IllegalStateException("record failed"));

        interceptor.after(target, new Object[0], null, null);

        assertNull(target._$PINPOINT$_getAsyncContext());
        verify(trace).traceBlockEnd();
    }

    @Test
    public void noCurrentTraceDoesNotTouchRecorderDependencies() {
        RetrySubscriberSeedInterceptor interceptor = newInterceptor(true);
        when(traceContext.currentTraceObject()).thenReturn(null);

        interceptor.after(new TestAccessor(), new Object[0], null, null);

        verifyNoInteractions(methodDescriptor, serviceType);
    }

    private RetrySubscriberSeedInterceptor newInterceptor(boolean traceRetry) {
        Properties properties = new Properties();
        properties.put("profiler.reactor.trace.retry", Boolean.toString(traceRetry));
        when(traceContext.getProfilerConfig()).thenReturn(ProfilerConfigLoader.load(properties));
        return new RetrySubscriberSeedInterceptor(traceContext, methodDescriptor, serviceType);
    }

    private static class TestAccessor implements AsyncContextAccessor {
        private AsyncContext asyncContext;

        @Override
        public void _$PINPOINT$_setAsyncContext(AsyncContext asyncContext) {
            this.asyncContext = asyncContext;
        }

        @Override
        public AsyncContext _$PINPOINT$_getAsyncContext() {
            return asyncContext;
        }
    }
}
