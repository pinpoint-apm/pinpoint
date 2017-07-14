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

import com.navercorp.pinpoint.bootstrap.async.AsyncTraceIdAccessor;
import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.context.AsyncTraceId;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.common.trace.ServiceType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;

import static org.mockito.Mockito.*;

/**
 * @author Woonduk Kang(emeroad)
 */
@RunWith(org.mockito.junit.MockitoJUnitRunner.class)
public class SpanAsyncEventSimpleAroundInterceptorTest {

    @Mock
    private TraceContext traceContext;
    @Mock
    private Trace trace;
    @Mock
    private MethodDescriptor methodDescriptor;

    @Mock
    private SpanEventRecorder spanEventRecorder;

    @Mock
    private AsyncTraceIdAccessor asyncTraceIdAccessor;

    @Mock
    private AsyncTraceId asyncTraceId;

    @Before
    public void setUp() throws Exception {
        when(asyncTraceIdAccessor._$PINPOINT$_getAsyncTraceId()).thenReturn(asyncTraceId);
    }

    @Test
    public void asyncTraceCreateError_currentRawTrace_continueAsyncTraceObject_return_Null() throws Exception {

        AroundInterceptor interceptor = mockSpanAsyncEventSimpleInterceptor(traceContext, methodDescriptor);
        interceptor.before(asyncTraceIdAccessor, null);

        verify(traceContext).currentRawTraceObject();
        verify(traceContext).continueAsyncTraceObject(asyncTraceId, asyncTraceId.getAsyncId(), asyncTraceId.getSpanStartTime());
    }

    @Test
    public void asyncTraceCreate() throws Exception {

        when(traceContext.continueAsyncTraceObject(any(AsyncTraceId.class), any(Integer.class), any(Long.class))).thenReturn(trace);
        when(trace.currentSpanEventRecorder()).thenReturn(spanEventRecorder);


        AroundInterceptor interceptor = mockSpanAsyncEventSimpleInterceptor(traceContext, methodDescriptor);
        interceptor.before(asyncTraceIdAccessor, null);

        verify(traceContext).currentRawTraceObject();
        verify(traceContext).continueAsyncTraceObject(asyncTraceId, asyncTraceId.getAsyncId(), asyncTraceId.getSpanStartTime());
        verify(trace).getScope(AsyncContext.ASYNC_TRACE_SCOPE);
    }

    @Test
    public void nestedAsyncTraceCreate() throws Exception {

        when(traceContext.currentRawTraceObject()).thenReturn(trace);

        AroundInterceptor interceptor = mockSpanAsyncEventSimpleInterceptor(traceContext, methodDescriptor);
        interceptor.before(asyncTraceIdAccessor, null);

        verify(traceContext).currentRawTraceObject();
        verify(traceContext, never()).continueAsyncTraceObject(asyncTraceId, asyncTraceId.getAsyncId(), asyncTraceId.getSpanStartTime());

    }

    protected AroundInterceptor mockSpanAsyncEventSimpleInterceptor(TraceContext traceContext, MethodDescriptor methodDescriptor) {
        return Mockito.mock(SpanAsyncEventSimpleAroundInterceptor.class,  withSettings()
                    .useConstructor(traceContext, methodDescriptor).defaultAnswer(CALLS_REAL_METHODS));
    }


}