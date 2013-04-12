package com.profiler.modifier.tomcat;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Enumeration;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.profiler.context.MockTraceContextFactory;
import com.profiler.context.TraceContext;
import com.profiler.logger.Slf4jLoggerBinder;
import com.profiler.logging.LoggerFactory;
import org.junit.BeforeClass;
import org.junit.Test;

import com.profiler.context.Header;
import com.profiler.modifier.tomcat.interceptors.StandardHostValveInvokeInterceptor;

public class InvokeMethodInterceptorTest {
    @BeforeClass
    public static void before() {
        LoggerFactory.initialize(new Slf4jLoggerBinder());
    }

    @Test
    public void testHeaderNOTExists() {

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        when(request.getRequestURI()).thenReturn("/hellotest.nhn");
        when(request.getRemoteAddr()).thenReturn("10.0.0.1");
        when(request.getHeader(Header.HTTP_TRACE_ID.toString())).thenReturn(null);
        when(request.getHeader(Header.HTTP_PARENT_SPAN_ID.toString())).thenReturn(null);
        when(request.getHeader(Header.HTTP_SPAN_ID.toString())).thenReturn(null);
        when(request.getHeader(Header.HTTP_SAMPLED.toString())).thenReturn(null);
        when(request.getHeader(Header.HTTP_FLAGS.toString())).thenReturn(null);

        Enumeration<?> enumeration = mock(Enumeration.class);
        when(request.getParameterNames()).thenReturn(enumeration);

        StandardHostValveInvokeInterceptor interceptor = new StandardHostValveInvokeInterceptor();
        TraceContext traceContext = new MockTraceContextFactory().create();
        interceptor.setTraceContext(traceContext);

        interceptor.before("target", "classname", "methodname", null, new Object[]{request, response});
        interceptor.after("target", "classname", "methodname", null, new Object[]{request, response}, new Object());

        interceptor.before("target", "classname", "methodname", null, new Object[]{request, response});
        interceptor.after("target", "classname", "methodname", null, new Object[]{request, response}, new Object());
    }

    @Test
    public void testInvalidHeaderExists() {

        // TODO 결과값 검증 필요.
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        when(request.getRequestURI()).thenReturn("/hellotest.nhn");
        when(request.getRemoteAddr()).thenReturn("10.0.0.1");
        when(request.getHeader(Header.HTTP_TRACE_ID.toString())).thenReturn("TRACEID");
        when(request.getHeader(Header.HTTP_PARENT_SPAN_ID.toString())).thenReturn("PARENTSPANID");
        when(request.getHeader(Header.HTTP_SPAN_ID.toString())).thenReturn("SPANID");
        when(request.getHeader(Header.HTTP_SAMPLED.toString())).thenReturn("false");
        when(request.getHeader(Header.HTTP_FLAGS.toString())).thenReturn("0");

        Enumeration<?> enumeration = mock(Enumeration.class);
        when(request.getParameterNames()).thenReturn(enumeration);

        TraceContext traceContext = new MockTraceContextFactory().create();
        StandardHostValveInvokeInterceptor interceptor = new StandardHostValveInvokeInterceptor();
        interceptor.setTraceContext(traceContext);
        interceptor.before("target", "classname", "methodname", null, new Object[]{request, response});
        interceptor.after("target", "classname", "methodname", null, new Object[]{request, response}, new Object());

        interceptor.before("target", "classname", "methodname", null, new Object[]{request, response});
        interceptor.after("target", "classname", "methodname", null, new Object[]{request, response}, new Object());
    }

    @Test
    public void testValidHeaderExists() {

        // TODO 결과값 검증 필요.
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        when(request.getRequestURI()).thenReturn("/hellotest.nhn");
        when(request.getRemoteAddr()).thenReturn("10.0.0.1");
        when(request.getHeader(Header.HTTP_TRACE_ID.toString())).thenReturn(UUID.randomUUID().toString());
        when(request.getHeader(Header.HTTP_PARENT_SPAN_ID.toString())).thenReturn("PARENTSPANID");
        when(request.getHeader(Header.HTTP_SPAN_ID.toString())).thenReturn("SPANID");
        when(request.getHeader(Header.HTTP_SAMPLED.toString())).thenReturn("false");
        when(request.getHeader(Header.HTTP_FLAGS.toString())).thenReturn("0");

        Enumeration<?> enumeration = mock(Enumeration.class);
        when(request.getParameterNames()).thenReturn(enumeration);

        TraceContext traceContext = new MockTraceContextFactory().create();
        StandardHostValveInvokeInterceptor interceptor = new StandardHostValveInvokeInterceptor();
        interceptor.setTraceContext(traceContext);

        interceptor.before("target", "classname", "methodname", null, new Object[]{request, response});
        interceptor.after("target", "classname", "methodname", null, new Object[]{request, response}, new Object());

        interceptor.before("target", "classname", "methodname", null, new Object[]{request, response});
        interceptor.after("target", "classname", "methodname", null, new Object[]{request, response}, new Object());
    }
}
