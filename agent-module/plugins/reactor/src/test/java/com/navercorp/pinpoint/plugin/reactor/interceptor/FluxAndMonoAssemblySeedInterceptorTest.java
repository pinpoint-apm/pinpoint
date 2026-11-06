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
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.SpanEventApiIdAwareAroundInterceptorForPlugin;
import com.navercorp.pinpoint.common.profiler.trace.TraceMetadataRegistrar;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.trace.ServiceTypeLocator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The assembly-time seeds (publishOn/subscribeOn/delay/interval) must attach an AsyncContext to the
 * returned publisher for an unsampled trace as well, exactly as they do for a sampled one. Otherwise a
 * chain assembled on the request thread and subscribed elsewhere loses the sampling flag downstream.
 */
public class FluxAndMonoAssemblySeedInterceptorTest {
    private static final int API_ID = 1;

    private TraceContext traceContext;
    private Trace trace;
    private SpanEventRecorder recorder;
    private AsyncContext nextAsyncContext;

    @BeforeAll
    public static void registerServiceTypes() {
        // ReactorConstants resolves its service types from ServiceTypeProvider when it is first loaded
        ServiceTypeLocator locator = mock(ServiceTypeLocator.class);
        when(locator.findServiceTypeByName("REACTOR")).thenReturn(mock(ServiceType.class));
        when(locator.findServiceTypeByName("REACTOR_SCHEDULER")).thenReturn(mock(ServiceType.class));
        TraceMetadataRegistrar.registerServiceTypes(locator);
    }

    @BeforeEach
    public void setUp() {
        traceContext = mock(TraceContext.class);
        trace = mock(Trace.class);
        recorder = mock(SpanEventRecorder.class);
        nextAsyncContext = mock(AsyncContext.class);

        Properties properties = new Properties();
        properties.put("profiler.reactor.trace.publishOn", "true");
        properties.put("profiler.reactor.trace.subscribeOn", "true");
        properties.put("profiler.reactor.trace.delay", "true");
        properties.put("profiler.reactor.trace.interval", "true");
        when(traceContext.getProfilerConfig()).thenReturn(ProfilerConfigLoader.load(properties));
        when(trace.traceBlockBegin()).thenReturn(recorder);
        when(trace.currentSpanEventRecorder()).thenReturn(recorder);
        when(recorder.recordNextAsyncContext()).thenReturn(nextAsyncContext);
    }

    @Test
    public void publishOn_sampled() {
        verifySeeded(new FluxAndMonoPublishOnInterceptor(traceContext), true);
    }

    @Test
    public void publishOn_unsampled() {
        verifySeeded(new FluxAndMonoPublishOnInterceptor(traceContext), false);
    }

    @Test
    public void subscribeOn_sampled() {
        verifySeeded(new FluxAndMonoSubscribeOnInterceptor(traceContext), true);
    }

    @Test
    public void subscribeOn_unsampled() {
        verifySeeded(new FluxAndMonoSubscribeOnInterceptor(traceContext), false);
    }

    @Test
    public void delay_sampled() {
        verifySeeded(new FluxAndMonoDelayInterceptor(traceContext), true);
    }

    @Test
    public void delay_unsampled() {
        verifySeeded(new FluxAndMonoDelayInterceptor(traceContext), false);
    }

    @Test
    public void interval_sampled() {
        verifySeeded(new FluxAndMonoIntervalInterceptor(traceContext), true);
    }

    @Test
    public void interval_unsampled() {
        verifySeeded(new FluxAndMonoIntervalInterceptor(traceContext), false);
    }

    @Test
    public void noTrace_leavesPublisherUntouched() {
        when(traceContext.currentTraceObject()).thenReturn(null);
        when(traceContext.currentRawTraceObject()).thenReturn(null);
        SpanEventApiIdAwareAroundInterceptorForPlugin interceptor = new FluxAndMonoPublishOnInterceptor(traceContext);

        TestPublisher publisher = new TestPublisher();
        interceptor.before(new Object(), API_ID, new Object[0]);
        interceptor.after(new Object(), API_ID, new Object[0], publisher, null);

        assertNull(publisher._$PINPOINT$_getAsyncContext());
    }

    private void verifySeeded(SpanEventApiIdAwareAroundInterceptorForPlugin interceptor, boolean sampled) {
        when(trace.canSampled()).thenReturn(sampled);
        // the real TraceContext hides unsampled traces from currentTraceObject()
        when(traceContext.currentTraceObject()).thenReturn(sampled ? trace : null);
        when(traceContext.currentRawTraceObject()).thenReturn(trace);

        TestPublisher publisher = new TestPublisher();
        interceptor.before(new Object(), API_ID, new Object[0]);
        interceptor.after(new Object(), API_ID, new Object[0], publisher, null);

        assertSame(nextAsyncContext, publisher._$PINPOINT$_getAsyncContext());

        InOrder inOrder = inOrder(trace, recorder);
        inOrder.verify(trace).traceBlockBegin();
        inOrder.verify(recorder).recordNextAsyncContext();
        inOrder.verify(trace).traceBlockEnd();
        verify(traceContext, never()).currentTraceObject();
    }

    private static class TestPublisher implements AsyncContextAccessor {
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
