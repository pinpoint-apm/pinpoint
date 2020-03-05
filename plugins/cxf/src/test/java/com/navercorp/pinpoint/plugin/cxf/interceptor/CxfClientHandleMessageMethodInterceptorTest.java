package com.navercorp.pinpoint.plugin.cxf.interceptor;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.context.TraceId;
import com.navercorp.pinpoint.plugin.cxf.CxfPluginConstants;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class CxfClientHandleMessageMethodInterceptorTest {

    @Mock
    private TraceContext traceContext;

    @Mock
    private MethodDescriptor descriptor;

    @Mock
    private ProfilerConfig profilerConfig;

    @Mock
    private Trace trace;

    @Mock
    private TraceId traceId;

    @Mock
    private TraceId nextId;

    @Mock
    private SpanEventRecorder recorder;

    @Test
    public void test1() throws Exception {
        doReturn(profilerConfig).when(traceContext).getProfilerConfig();
        doReturn(trace).when(traceContext).currentTraceObject();
        doReturn(traceId).when(trace).getTraceId();
        doReturn(nextId).when(traceId).getNextTraceId();
        doReturn(recorder).when(trace).traceBlockBegin();

        Object target = new Object();
        Map map = new HashMap();
        map.put("org.apache.cxf.message.Message.ENDPOINT_ADDRESS", "http://foo.com/getFoo");
        map.put("org.apache.cxf.request.uri", "http://foo.com/getFoo");
        map.put("org.apache.cxf.request.method", "POST");
        map.put("Content-Type", "application/json");
        Object[] args = new Object[]{map};

        CxfClientHandleMessageMethodInterceptor interceptor = new CxfClientHandleMessageMethodInterceptor(traceContext, descriptor);
        interceptor.before(target, args);

        verify(recorder).recordServiceType(CxfPluginConstants.CXF_CLIENT_SERVICE_TYPE);
        verify(recorder).recordDestinationId("http://foo.com");
        verify(recorder).recordAttribute(CxfPluginConstants.CXF_ADDRESS, "http://foo.com/getFoo");
        verify(recorder).recordAttribute(CxfPluginConstants.CXF_HTTP_METHOD, "POST");
        verify(recorder).recordAttribute(CxfPluginConstants.CXF_CONTENT_TYPE, "application/json");
    }

    @Test
    public void test2() throws Exception {
        doReturn(profilerConfig).when(traceContext).getProfilerConfig();
        doReturn(trace).when(traceContext).currentTraceObject();

        Object target = new Object();
        Object[] args = new Object[]{""};

        CxfClientHandleMessageMethodInterceptor interceptor = new CxfClientHandleMessageMethodInterceptor(traceContext, descriptor);
        interceptor.before(target, args);

        verify(trace, never()).traceBlockBegin();
    }

    @Test
    public void test3() throws Exception {
        doReturn(profilerConfig).when(traceContext).getProfilerConfig();
        doReturn(trace).when(traceContext).currentTraceObject();
        doReturn(traceId).when(trace).getTraceId();
        doReturn(nextId).when(traceId).getNextTraceId();
        doReturn(recorder).when(trace).traceBlockBegin();

        Object target = new Object();
        Map map = new HashMap();
        map.put("org.apache.cxf.message.Message.ENDPOINT_ADDRESS", "http://foo.com/getFoo");
        Object[] args = new Object[]{map};

        CxfClientHandleMessageMethodInterceptor interceptor = new CxfClientHandleMessageMethodInterceptor(traceContext, descriptor);
        interceptor.before(target, args);

        verify(recorder).recordServiceType(CxfPluginConstants.CXF_CLIENT_SERVICE_TYPE);
        verify(recorder).recordDestinationId("http://foo.com");
        verify(recorder).recordAttribute(CxfPluginConstants.CXF_ADDRESS, "unknown");
        verify(recorder).recordAttribute(CxfPluginConstants.CXF_HTTP_METHOD, "unknown");
        verify(recorder).recordAttribute(CxfPluginConstants.CXF_CONTENT_TYPE, "unknown");
    }
}