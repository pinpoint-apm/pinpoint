package com.navercorp.pinpoint.plugin.fastjson.interceptor;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.plugin.fastjson.FastjsonConstants;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class ToJavaObjectInterceptorTest {

    @Mock
    private TraceContext traceContext;

    @Mock
    private MethodDescriptor descriptor;

    @Mock
    private Trace trace;

    @Mock
    private SpanEventRecorder recorder;

    @Test
    public void before() {

        doReturn(trace).when(traceContext).currentTraceObject();
        doReturn(recorder).when(trace).traceBlockBegin();

        ToJavaObjectInterceptor interceptor = new ToJavaObjectInterceptor(traceContext, descriptor);

        interceptor.before(null, null);
    }

    @Test
    public void after() {

        doReturn(trace).when(traceContext).currentTraceObject();
        doReturn(recorder).when(trace).currentSpanEventRecorder();

        ToJavaObjectInterceptor interceptor = new ToJavaObjectInterceptor(traceContext, descriptor);

        interceptor.after(null, new Object[]{}, "{\"firstName\": \"Json\"}", null);

        verify(recorder).recordServiceType(FastjsonConstants.SERVICE_TYPE);
        verify(recorder).recordAttribute(FastjsonConstants.ANNOTATION_KEY_JSON_LENGTH, "{\"firstName\": \"Json\"}".hashCode());
    }
}