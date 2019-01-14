package com.navercorp.pinpoint.plugin.hbase.interceptor;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.plugin.hbase.HbasePluginConstants;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;

import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class HbaseTableMethodInterceptorTest {

    @Mock
    private TraceContext traceContext;

    @Mock
    private MethodDescriptor descriptor;

    @Mock
    private SpanEventRecorder recorder;

    @Test
    public void doInBeforeTrace() {

        Object target = new Object();
        Object[] args = new Object[]{};

        HbaseTableMethodInterceptor interceptor = new HbaseTableMethodInterceptor(traceContext, descriptor,true);
        interceptor.doInBeforeTrace(recorder, target, args);
        verify(recorder).recordServiceType(HbasePluginConstants.HBASE_CLIENT_TABLE);
    }

    @Test
    public void doInAfterTrace() {

        Object target = new Object();
        Object[] args = new Object[]{Collections.singletonList("test")};

        HbaseTableMethodInterceptor interceptor = new HbaseTableMethodInterceptor(traceContext, descriptor, true);
        interceptor.doInAfterTrace(recorder, target, args, null, null);
        verify(recorder).recordAttribute(HbasePluginConstants.HBASE_CLIENT_PARAMS, "size: 1");
        verify(recorder).recordApi(descriptor);
        verify(recorder).recordException(null);
    }
}