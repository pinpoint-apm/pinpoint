package com.navercorp.pinpoint.plugin.druid.interceptor;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class DataSourceCloseConnectionInterceptorTest {

    @Mock
    private TraceContext traceContext;

    @Mock
    private MethodDescriptor descriptor;

    @Mock
    private SpanEventRecorder recorder;

    @Test
    public void doInBeforeTrace() {

        DataSourceCloseConnectionInterceptor interceptor = new DataSourceCloseConnectionInterceptor(traceContext, descriptor);

        interceptor.doInBeforeTrace(null, null, null);
    }

    @Test
    public void doInAfterTrace() {

        DataSourceCloseConnectionInterceptor interceptor = new DataSourceCloseConnectionInterceptor(traceContext, descriptor);

        interceptor.doInAfterTrace(recorder, null, null, null, null);
    }
}