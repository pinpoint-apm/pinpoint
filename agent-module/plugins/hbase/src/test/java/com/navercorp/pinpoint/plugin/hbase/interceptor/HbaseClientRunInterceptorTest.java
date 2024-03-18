package com.navercorp.pinpoint.plugin.hbase.interceptor;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.plugin.hbase.HbasePluginConstants;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class HbaseClientRunInterceptorTest {

    @Mock
    private TraceContext traceContext;

    @Mock
    private MethodDescriptor descriptor;

    @Mock
    private SpanEventRecorder recorder;

    @Test
    public void doInAfterTrace() {

        Object target = new Object();
        Object[] args = new Object[]{"foo", "bar"};

        HbaseClientRunInterceptor interceptor = new HbaseClientRunInterceptor(traceContext, descriptor);
        interceptor.doInAfterTrace(recorder, target, args, null, null);

        verify(recorder).recordServiceType(HbasePluginConstants.HBASE_ASYNC_CLIENT);
        verify(recorder).recordApi(descriptor);
        verify(recorder).recordException(null);
    }
}