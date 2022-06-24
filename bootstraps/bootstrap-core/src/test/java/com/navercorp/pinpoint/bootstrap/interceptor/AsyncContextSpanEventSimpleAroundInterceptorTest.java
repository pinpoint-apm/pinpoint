/*
 * Copyright 2017 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.bootstrap.interceptor;

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessor;
import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.common.trace.ServiceType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

/**
 * @author Woonduk Kang(emeroad)
 */
@ExtendWith(MockitoExtension.class)
public class AsyncContextSpanEventSimpleAroundInterceptorTest {

    @Mock
    private TraceContext traceContext;
    @Mock
    private Trace trace;
    @Mock
    private MethodDescriptor methodDescriptor;

    @Mock
    private SpanEventRecorder spanEventRecorder;

    @Mock
    private AsyncContextAccessor asyncContextAccessor;

    @Mock
    private AsyncContext asyncContext;

    @BeforeEach
    public void setUp() {

    }

    @Test
    public void propagation_fail() {

        AroundInterceptor interceptor = mockSpanAsyncEventSimpleInterceptor(traceContext, methodDescriptor);
        interceptor.before(asyncContextAccessor, null);

        verify(asyncContext, never()).continueAsyncTraceObject();
        verify(asyncContext, never()).currentAsyncTraceObject();

    }

    @Test
    public void asyncTraceCreate() {
        when(asyncContextAccessor._$PINPOINT$_getAsyncContext()).thenReturn(asyncContext);
        when(asyncContext.continueAsyncTraceObject()).thenReturn(trace);

        AroundInterceptor interceptor = mockSpanAsyncEventSimpleInterceptor(traceContext, methodDescriptor);
        interceptor.before(asyncContextAccessor, null);

        verify(asyncContext).continueAsyncTraceObject();
        verify(trace).getScope(AsyncContext.ASYNC_TRACE_SCOPE);
    }

    @Test
    public void nestedAsyncTraceCreate() {
        when(asyncContextAccessor._$PINPOINT$_getAsyncContext()).thenReturn(asyncContext);
//        when(asyncContext.continueAsyncTraceObject()).thenReturn(trace);

        AroundInterceptor interceptor = mockSpanAsyncEventSimpleInterceptor(traceContext, methodDescriptor);
        interceptor.before(asyncContextAccessor, null);

        verify(asyncContext).continueAsyncTraceObject();
        verify(spanEventRecorder, never()).recordServiceType(any(ServiceType.class));
    }

    protected AroundInterceptor mockSpanAsyncEventSimpleInterceptor(TraceContext traceContext, MethodDescriptor methodDescriptor) {
        return Mockito.mock(AsyncContextSpanEventSimpleAroundInterceptor.class, withSettings()
                .useConstructor(traceContext, methodDescriptor).defaultAnswer(CALLS_REAL_METHODS));
    }


}