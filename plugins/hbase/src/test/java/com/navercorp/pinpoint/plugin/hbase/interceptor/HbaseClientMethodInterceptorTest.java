package com.navercorp.pinpoint.plugin.hbase.interceptor;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.plugin.hbase.HbasePluginConstants;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.net.InetSocketAddress;

import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class HbaseClientMethodInterceptorTest {

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

        HbaseClientMethodInterceptor interceptor = new HbaseClientMethodInterceptor(traceContext, descriptor);
        interceptor.doInBeforeTrace(recorder, target, args);
        verify(recorder).recordServiceType(HbasePluginConstants.HBASE_CLIENT);
    }

    @Test
    public void doInAfterTrace() {

        Object target = new Object();
        Object[] args = new Object[]{null, null, null, null, null, InetSocketAddress.createUnresolved("localhost", 1234), null};

        HbaseClientMethodInterceptor interceptor = new HbaseClientMethodInterceptor(traceContext, descriptor);
        interceptor.doInAfterTrace(recorder, target, args, null, null);
        verify(recorder).recordEndPoint("localhost");
        verify(recorder).recordDestinationId("HBASE");
        verify(recorder).recordApi(descriptor);
        verify(recorder).recordException(null);
    }

}