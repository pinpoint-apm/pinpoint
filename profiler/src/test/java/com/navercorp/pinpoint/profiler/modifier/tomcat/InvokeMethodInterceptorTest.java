package com.nhn.pinpoint.profiler.modifier.tomcat;

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

import com.nhn.pinpoint.bootstrap.context.Header;
import com.nhn.pinpoint.bootstrap.context.TraceContext;
import com.nhn.pinpoint.bootstrap.logging.PLoggerFactory;
import com.nhn.pinpoint.profiler.logging.Slf4jLoggerBinder;
import com.nhn.pinpoint.profiler.modifier.tomcat.interceptor.StandardHostValveInvokeInterceptor;

/**
 * @author emeroad
 */
public class InvokeMethodInterceptorTest {
    
    @Mock
    public HttpServletRequest request;
    
    @Mock
    public HttpServletResponse response;
    
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

        StandardHostValveInvokeInterceptor interceptor = new StandardHostValveInvokeInterceptor();
        TraceContext traceContext = new MockTraceContextFactory().create();
        interceptor.setTraceContext(traceContext);

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

        TraceContext traceContext = new MockTraceContextFactory().create();
        StandardHostValveInvokeInterceptor interceptor = new StandardHostValveInvokeInterceptor();
        interceptor.setTraceContext(traceContext);
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

        TraceContext traceContext = new MockTraceContextFactory().create();
        StandardHostValveInvokeInterceptor interceptor = new StandardHostValveInvokeInterceptor();
        interceptor.setTraceContext(traceContext);

        interceptor.before("target", new Object[]{request, response});
        interceptor.after("target", new Object[]{request, response}, new Object(), null);

        interceptor.before("target", new Object[]{request, response});
        interceptor.after("target", new Object[]{request, response}, new Object(), null);
    }
}
