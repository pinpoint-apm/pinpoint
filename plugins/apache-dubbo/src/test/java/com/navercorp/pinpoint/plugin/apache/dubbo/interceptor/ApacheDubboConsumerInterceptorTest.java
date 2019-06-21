package com.navercorp.pinpoint.plugin.apache.dubbo.interceptor;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import org.apache.dubbo.rpc.RpcInvocation;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ApacheDubboConsumerInterceptorTest {

    @Mock
    private TraceContext traceContext;

    @Mock
    private MethodDescriptor descriptor;

    @Mock
    private RpcInvocation rpcInvocation;

    private Object obj = new Object();
    private Object[] args = new Object[]{rpcInvocation};

    @Test
    public void before() {
        ApacheDubboConsumerInterceptor interceptor = new ApacheDubboConsumerInterceptor(traceContext, descriptor);

        interceptor.before(obj, args);
    }

    @Test
    public void after() {
        ApacheDubboConsumerInterceptor interceptor = new ApacheDubboConsumerInterceptor(traceContext, descriptor);

        interceptor.after(obj, args, null, null);
    }
}