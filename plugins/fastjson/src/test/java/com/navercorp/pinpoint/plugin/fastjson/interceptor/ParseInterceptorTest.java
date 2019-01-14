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
public class ParseInterceptorTest {

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

        ParseInterceptor interceptor = new ParseInterceptor(traceContext, descriptor);

        interceptor.before(null, null);
    }

    @Test
    public void after1() {

        doReturn(trace).when(traceContext).currentTraceObject();
        doReturn(recorder).when(trace).currentSpanEventRecorder();

        ParseInterceptor interceptor = new ParseInterceptor(traceContext, descriptor);

        interceptor.after(null, new Object[]{"{\"firstName\": \"Json\"}"}, null, null);

        verify(recorder).recordServiceType(FastjsonConstants.SERVICE_TYPE);
        verify(recorder).recordAttribute(FastjsonConstants.ANNOTATION_KEY_JSON_LENGTH, "{\"firstName\": \"Json\"}".length());
    }


    @Test
    public void after2() {

        doReturn(trace).when(traceContext).currentTraceObject();
        doReturn(recorder).when(trace).currentSpanEventRecorder();

        ParseInterceptor interceptor = new ParseInterceptor(traceContext, descriptor);

        interceptor.after(null, new Object[]{new byte[]{01}}, null, null);

        verify(recorder).recordServiceType(FastjsonConstants.SERVICE_TYPE);
        verify(recorder).recordAttribute(FastjsonConstants.ANNOTATION_KEY_JSON_LENGTH, new byte[]{01}.length);

    }
}