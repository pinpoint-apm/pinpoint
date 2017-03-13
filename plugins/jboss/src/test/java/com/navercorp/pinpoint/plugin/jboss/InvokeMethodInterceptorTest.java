/*
 * Copyright 2016 Pinpoint contributors and NAVER Corp.
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
package com.navercorp.pinpoint.plugin.jboss;

import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.navercorp.pinpoint.bootstrap.config.DefaultProfilerConfig;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.context.TraceId;
import com.navercorp.pinpoint.profiler.context.id.DefaultTraceId;
import com.navercorp.pinpoint.test.MockTraceContextFactory;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.navercorp.pinpoint.bootstrap.context.Header;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.plugin.jboss.interceptor.StandardHostValveInvokeInterceptor;
import com.navercorp.pinpoint.profiler.context.DefaultMethodDescriptor;
import com.navercorp.pinpoint.profiler.logging.Slf4jLoggerBinder;

import static org.mockito.Mockito.*;

/**
 * The Class InvokeMethodInterceptorTest.
 *
 * @author emeroad
 */
public class InvokeMethodInterceptorTest {

    /** The request. */
    @Mock
    public HttpServletRequest request;

    /** The response. */
    @Mock
    public HttpServletResponse response;

    /** The descriptor. */
    private final MethodDescriptor descriptor = new DefaultMethodDescriptor("org.apache.catalina.core.StandardHostValve", "invoke", new String[] {
        "org.apache.catalina.connector.Request", "org.apache.catalina.connector.Response" }, new String[] { "request", "response" });

    /**
     * Before.
     */
    @BeforeClass
    public static void before() {
        PLoggerFactory.initialize(new Slf4jLoggerBinder());
    }

    /**
     * Sets the up.
     *
     * @throws Exception the exception
     */
    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    private TraceContext spyTraceContext() {
        ProfilerConfig profilerConfig = new DefaultProfilerConfig();
        TraceContext traceContext = MockTraceContextFactory.newTestTraceContext(profilerConfig);
        return spy(traceContext);
    }

    /**
     * Test header not exists.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testHeaderNOTExists() {

        when(request.getRequestURI()).thenReturn("/hellotest.nhn");
        when(request.getRemoteAddr()).thenReturn("10.0.0.1");
        when(request.getHeader(Header.HTTP_TRACE_ID.toString())).thenReturn(null);
        when(request.getHeader(Header.HTTP_PARENT_SPAN_ID.toString())).thenReturn(null);
        when(request.getHeader(Header.HTTP_SPAN_ID.toString())).thenReturn(null);
        when(request.getHeader(Header.HTTP_SAMPLED.toString())).thenReturn(null);
        when(request.getHeader(Header.HTTP_FLAGS.toString())).thenReturn(null);
        final Enumeration<?> enumeration = mock(Enumeration.class);
        when(request.getParameterNames()).thenReturn((Enumeration<String>) enumeration);

        TraceContext traceContext = spyTraceContext();
        final StandardHostValveInvokeInterceptor interceptor = new StandardHostValveInvokeInterceptor(traceContext, descriptor);

        interceptor.before("target", new Object[] { request, response });
        interceptor.after("target", new Object[] { request, response }, new Object(), null);

        verify(traceContext, times(1)).newTraceObject();

        interceptor.before("target", new Object[] { request, response });
        interceptor.after("target", new Object[] { request, response }, new Object(), null);

        verify(traceContext, times(2)).newTraceObject();
    }

    /**
     * Test invalid header exists.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testInvalidHeaderExists() {

        when(request.getRequestURI()).thenReturn("/hellotest.nhn");
        when(request.getRemoteAddr()).thenReturn("10.0.0.1");
        when(request.getHeader(Header.HTTP_TRACE_ID.toString())).thenReturn("TRACEID");
        when(request.getHeader(Header.HTTP_PARENT_SPAN_ID.toString())).thenReturn("PARENTSPANID");
        when(request.getHeader(Header.HTTP_SPAN_ID.toString())).thenReturn("SPANID");
        when(request.getHeader(Header.HTTP_SAMPLED.toString())).thenReturn("false");
        when(request.getHeader(Header.HTTP_FLAGS.toString())).thenReturn("0");
        final Enumeration<?> enumeration = mock(Enumeration.class);
        when(request.getParameterNames()).thenReturn((Enumeration<String>) enumeration);

        TraceContext traceContext = spyTraceContext();
        final StandardHostValveInvokeInterceptor interceptor = new StandardHostValveInvokeInterceptor(traceContext, descriptor);
        interceptor.before("target", new Object[] { request, response });
        interceptor.after("target", new Object[] { request, response }, new Object(), null);

        verify(traceContext, never()).newTraceObject();
        verify(traceContext, never()).disableSampling();
        verify(traceContext, never()).continueTraceObject(any(TraceId.class));


        interceptor.before("target", new Object[] { request, response });
        interceptor.after("target", new Object[] { request, response }, new Object(), null);

        verify(traceContext, never()).newTraceObject();
        verify(traceContext, never()).disableSampling();
        verify(traceContext, never()).continueTraceObject(any(TraceId.class));
    }

    /**
     * Test valid header exists.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testValidHeaderExists() {

        when(request.getRequestURI()).thenReturn("/hellotest.nhn");
        when(request.getRemoteAddr()).thenReturn("10.0.0.1");

        TraceId  traceId = new DefaultTraceId("agentTest", System.currentTimeMillis(), 1);
        when(request.getHeader(Header.HTTP_TRACE_ID.toString())).thenReturn(traceId.getTransactionId());
        when(request.getHeader(Header.HTTP_PARENT_SPAN_ID.toString())).thenReturn("PARENTSPANID");
        when(request.getHeader(Header.HTTP_SPAN_ID.toString())).thenReturn("SPANID");
        when(request.getHeader(Header.HTTP_SAMPLED.toString())).thenReturn("false");
        when(request.getHeader(Header.HTTP_FLAGS.toString())).thenReturn("0");
        final Enumeration<?> enumeration = mock(Enumeration.class);
        when(request.getParameterNames()).thenReturn((Enumeration<String>) enumeration);

        TraceContext traceContext = spyTraceContext();
        final StandardHostValveInvokeInterceptor interceptor = new StandardHostValveInvokeInterceptor(traceContext, descriptor);

        interceptor.before("target", new Object[] { request, response });
        interceptor.after("target", new Object[] { request, response }, new Object(), null);

        verify(traceContext, times(1)).continueTraceObject(any(TraceId.class));

        interceptor.before("target", new Object[] { request, response });
        interceptor.after("target", new Object[] { request, response }, new Object(), null);

        verify(traceContext, times(2)).continueTraceObject(any(TraceId.class));
    }
}
