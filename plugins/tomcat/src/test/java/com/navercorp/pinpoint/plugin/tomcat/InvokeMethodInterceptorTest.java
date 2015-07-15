package com.navercorp.pinpoint.plugin.tomcat;
/*
 * Copyright 2014 NAVER Corp.
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



import static org.mockito.Mockito.*;

import java.util.Enumeration;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.navercorp.pinpoint.bootstrap.MetadataAccessor;
import com.navercorp.pinpoint.bootstrap.config.SkipFilter;
import com.navercorp.pinpoint.bootstrap.context.Header;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.plugin.tomcat.interceptor.StandardHostValveInvokeInterceptor;
import com.navercorp.pinpoint.profiler.interceptor.DefaultMethodDescriptor;
import com.navercorp.pinpoint.profiler.logging.Slf4jLoggerBinder;
import com.navercorp.pinpoint.test.mock.MockTraceContext;

/**
 * @author emeroad
 */
public class InvokeMethodInterceptorTest {
    
    @Mock
    public HttpServletRequest request;
    
    @Mock
    public HttpServletResponse response;

    private final MethodDescriptor descriptor = new DefaultMethodDescriptor("org.apache.catalina.core.StandardHostValve", "invoke", new String[] {"org.apache.catalina.connector.Request", "org.apache.catalina.connector.Response"}, new String[] {"request", "response"});;
    
    @BeforeClass
    public static void before() {
        PLoggerFactory.initialize(new Slf4jLoggerBinder());
    }
    
    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testHeaderNOTExists() {

        when(request.getRequestURI()).thenReturn("/hellotest.nhn");
        when(request.getRemoteAddr()).thenReturn("10.0.0.1");
        when(request.getHeader(Header.HTTP_TRACE_ID.toString())).thenReturn(null);
        when(request.getHeader(Header.HTTP_PARENT_SPAN_ID.toString())).thenReturn(null);
        when(request.getHeader(Header.HTTP_SPAN_ID.toString())).thenReturn(null);
        when(request.getHeader(Header.HTTP_SAMPLED.toString())).thenReturn(null);
        when(request.getHeader(Header.HTTP_FLAGS.toString())).thenReturn(null);
        Enumeration<?> enumeration = mock(Enumeration.class);
        when(request.getParameterNames()).thenReturn(enumeration);

        TraceContext traceContext = new MockTraceContext();
        StandardHostValveInvokeInterceptor interceptor = new StandardHostValveInvokeInterceptor(traceContext, descriptor, new SkipFilter<String>(), MetadataAccessor.get(0), MetadataAccessor.get(1));

        interceptor.before("target", new Object[]{request, response});
        interceptor.after("target", new Object[]{request, response}, new Object(), null);

        interceptor.before("target", new Object[]{request, response});
        interceptor.after("target", new Object[]{request, response}, new Object(), null);
    }

    @Test
    public void testInvalidHeaderExists() {

        when(request.getRequestURI()).thenReturn("/hellotest.nhn");
        when(request.getRemoteAddr()).thenReturn("10.0.0.1");
        when(request.getHeader(Header.HTTP_TRACE_ID.toString())).thenReturn("TRACEID");
        when(request.getHeader(Header.HTTP_PARENT_SPAN_ID.toString())).thenReturn("PARENTSPANID");
        when(request.getHeader(Header.HTTP_SPAN_ID.toString())).thenReturn("SPANID");
        when(request.getHeader(Header.HTTP_SAMPLED.toString())).thenReturn("false");
        when(request.getHeader(Header.HTTP_FLAGS.toString())).thenReturn("0");
        Enumeration<?> enumeration = mock(Enumeration.class);
        when(request.getParameterNames()).thenReturn(enumeration);

        TraceContext traceContext = new MockTraceContext();
        StandardHostValveInvokeInterceptor interceptor = new StandardHostValveInvokeInterceptor(traceContext, descriptor, new SkipFilter<String>(), MetadataAccessor.get(0), MetadataAccessor.get(1));
        interceptor.before("target",  new Object[]{request, response});
        interceptor.after("target", new Object[]{request, response}, new Object(), null);

        interceptor.before("target", new Object[]{request, response});
        interceptor.after("target", new Object[]{request, response}, new Object(), null);
    }

    @Test
    public void testValidHeaderExists() {

        when(request.getRequestURI()).thenReturn("/hellotest.nhn");
        when(request.getRemoteAddr()).thenReturn("10.0.0.1");
        when(request.getHeader(Header.HTTP_TRACE_ID.toString())).thenReturn(UUID.randomUUID().toString());
        when(request.getHeader(Header.HTTP_PARENT_SPAN_ID.toString())).thenReturn("PARENTSPANID");
        when(request.getHeader(Header.HTTP_SPAN_ID.toString())).thenReturn("SPANID");
        when(request.getHeader(Header.HTTP_SAMPLED.toString())).thenReturn("false");
        when(request.getHeader(Header.HTTP_FLAGS.toString())).thenReturn("0");
        Enumeration<?> enumeration = mock(Enumeration.class);
        when(request.getParameterNames()).thenReturn(enumeration);

        TraceContext traceContext = new MockTraceContext();
        StandardHostValveInvokeInterceptor interceptor = new StandardHostValveInvokeInterceptor(traceContext, descriptor, new SkipFilter<String>(), MetadataAccessor.get(0), MetadataAccessor.get(1));

        interceptor.before("target", new Object[]{request, response});
        interceptor.after("target", new Object[]{request, response}, new Object(), null);

        interceptor.before("target", new Object[]{request, response});
        interceptor.after("target", new Object[]{request, response}, new Object(), null);
    }
}
