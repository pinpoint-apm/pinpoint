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
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceBlock;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.common.profiler.trace.TraceMetadataRegistrar;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.trace.ServiceTypeLocator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class FluxAndMonoSubscribeMethodInterceptorTest {
    private static final int API_ID = 1;

    private TraceContext traceContext;
    private Trace trace;
    private TraceBlock traceBlock;
    private AsyncContext nextAsyncContext;
    private FluxAndMonoSubscribeMethodInterceptor interceptor;

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
        traceBlock = mock(TraceBlock.class);
        nextAsyncContext = mock(AsyncContext.class);
        Properties properties = new Properties();
        properties.put("profiler.reactor.trace.subscribe", "true");
        when(traceContext.getProfilerConfig()).thenReturn(ProfilerConfigLoader.load(properties));
        when(trace.getTraceBlock()).thenReturn(traceBlock);
        when(traceBlock.getTrace()).thenReturn(trace);
        when(traceBlock.isBegin()).thenReturn(true);
        when(traceBlock.recordNextAsyncContext()).thenReturn(nextAsyncContext);
        interceptor = new FluxAndMonoSubscribeMethodInterceptor(traceContext);
    }

    @Test
    public void sampledTraceIsCarriedToSubscriber() {
        verifyCarriedToSubscriber(true);
    }

    @Test
    public void unsampledTraceIsCarriedToSubscriber() {
        verifyCarriedToSubscriber(false);
    }

    private void verifyCarriedToSubscriber(boolean sampled) {
        when(trace.canSampled()).thenReturn(sampled);
        // the real TraceContext hides unsampled traces from currentTraceObject()
        when(traceContext.currentTraceObject()).thenReturn(sampled ? trace : null);
        when(traceContext.currentRawTraceObject()).thenReturn(trace);
        TestAccessor subscriber = new TestAccessor();
        Object[] args = {subscriber};

        TraceBlock block = interceptor.before(new Object(), API_ID, args);
        interceptor.after(block, new Object(), API_ID, args, null, null);

        assertSame(traceBlock, block);
        assertSame(nextAsyncContext, subscriber._$PINPOINT$_getAsyncContext());
        InOrder inOrder = inOrder(trace, traceBlock);
        inOrder.verify(trace).getTraceBlock();
        inOrder.verify(traceBlock).begin();
        inOrder.verify(traceBlock).recordNextAsyncContext();
        inOrder.verify(traceBlock).close();
        verify(traceContext, never()).currentTraceObject();
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
