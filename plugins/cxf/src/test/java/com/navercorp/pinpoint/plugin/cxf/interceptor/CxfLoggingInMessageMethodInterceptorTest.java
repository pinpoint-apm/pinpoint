package com.navercorp.pinpoint.plugin.cxf.interceptor;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.plugin.cxf.CxfPluginConstants;
import org.apache.cxf.interceptor.LoggingMessage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class CxfLoggingInMessageMethodInterceptorTest {

    @Mock
    private TraceContext traceContext;

    @Mock
    private MethodDescriptor descriptor;

    @Mock
    private SpanEventRecorder recorder;

    @Test
    public void doInBeforeTrace() {

        LoggingMessage message = new LoggingMessage("", "1");
        message.getAddress().append("http://foo.com/getFoo");
        message.getContentType().append("application/json");
        message.getHttpMethod().append("POST");
        message.getHeader().append("test");

        Object target = new Object();
        Object[] args = new Object[]{message};

        CxfLoggingInMessageMethodInterceptor inMessageMethodInterceptor =
                new CxfLoggingInMessageMethodInterceptor(traceContext, descriptor);

        inMessageMethodInterceptor.doInBeforeTrace(recorder, target, args);

        verify(recorder).recordServiceType(CxfPluginConstants.CXF_LOGGING_IN_SERVICE_TYPE);
        verify(recorder).recordAttribute(CxfPluginConstants.CXF_ADDRESS, "http://foo.com/getFoo");
        verify(recorder).recordAttribute(CxfPluginConstants.CXF_HTTP_METHOD, "POST");
        verify(recorder).recordAttribute(CxfPluginConstants.CXF_CONTENT_TYPE, "application/json");
        verify(recorder).recordAttribute(CxfPluginConstants.CXF_HEADERS, "test");

    }

    @Test
    public void doInAfterTrace() {

        LoggingMessage message = new LoggingMessage("", "1");
        message.getAddress().append("http://foo.com/getFoo");
        message.getContentType().append("application/json");
        message.getHttpMethod().append("POST");
        message.getHeader().append("test");

        Object target = new Object();
        Object[] args = new Object[]{message};

        CxfLoggingInMessageMethodInterceptor inMessageMethodInterceptor =
                new CxfLoggingInMessageMethodInterceptor(traceContext, descriptor);

        inMessageMethodInterceptor.doInAfterTrace(recorder, target, args, null, null);

        verify(recorder, never()).recordServiceType(CxfPluginConstants.CXF_LOGGING_IN_SERVICE_TYPE);

        verify(recorder).recordApi(descriptor);
    }
}