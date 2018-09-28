package com.navercorp.pinpoint.plugin.fastjson.interceptor;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ParseInterceptorTest {

    @Mock
    private TraceContext traceContext;

    @Mock
    private MethodDescriptor descriptor;

    @Test
    public void before() {

        ParseInterceptor interceptor = new ParseInterceptor(traceContext, descriptor);

        interceptor.before(null, null);
    }

    @Test
    public void after() {

        ParseInterceptor interceptor = new ParseInterceptor(traceContext, descriptor);

        interceptor.after(null, new Object[]{null}, null, null);
    }
}