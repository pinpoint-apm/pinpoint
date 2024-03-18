package com.navercorp.pinpoint.plugin.druid.interceptor;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class DataSourceGetConnectionInterceptorTest {

    @Mock
    private TraceContext traceContext;

    @Mock
    private MethodDescriptor descriptor;

    @Mock
    private SpanEventRecorder recorder;

    @Test
    public void doInBeforeTrace() {

        DataSourceGetConnectionInterceptor interceptor = new DataSourceGetConnectionInterceptor(traceContext, descriptor);

        interceptor.doInBeforeTrace(null, null, null);
    }

    @Test
    public void doInAfterTrace1() {

        DataSourceGetConnectionInterceptor interceptor = new DataSourceGetConnectionInterceptor(traceContext, descriptor);

        interceptor.doInAfterTrace(recorder, null, null, null, null);
    }

    @Test
    public void doInAfterTrace2() {

        DataSourceGetConnectionInterceptor interceptor = new DataSourceGetConnectionInterceptor(traceContext, descriptor);

        interceptor.doInAfterTrace(recorder, null, new Object[]{"", ""}, null, null);
    }
}