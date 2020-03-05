/*
 * Copyright 2019 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.plugin.log4j2.interceptor;

import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.context.TraceId;
import org.apache.logging.log4j.ThreadContext;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * @author minwoo.jung
 */
public class LogEventFactoryInterceptorTest {

    private static final String TRANSACTION_ID = "PtxId";

    @Test
    public void interceptorTest() {
        TraceContext traceContext = mock(TraceContext.class);
        LogEventFactoryInterceptor interceptor = new LogEventFactoryInterceptor(traceContext);
        interceptor.before(null);
        interceptor.after(null, null, null);
        Assert.assertTrue(ThreadContext.get(TRANSACTION_ID) == null);
    }

    @Test
    public void interceptorTest2() {
        TraceContext traceContext = spy(TraceContext.class);
        Trace trace = mock(Trace.class);
        TraceId traceId = spy(TraceId.class);
        when(traceContext.currentTraceObject()).thenReturn(trace);
        when(traceContext.currentRawTraceObject()).thenReturn(trace);
        when(traceContext.currentRawTraceObject().getTraceId()).thenReturn(traceId);
        when(traceContext.currentRawTraceObject().getTraceId().getTransactionId()).thenReturn("aaa");
        when(traceContext.currentRawTraceObject().getTraceId().getSpanId()).thenReturn(112343l);
        LogEventFactoryInterceptor interceptor = spy(new LogEventFactoryInterceptor(traceContext));
        interceptor.before(null);
        interceptor.after(null, null, null);
        Assert.assertTrue(ThreadContext.get(TRANSACTION_ID) != null);
    }
}