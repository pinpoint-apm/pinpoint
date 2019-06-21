package com.navercorp.pinpoint.plugin.apache.dubbo.interceptor;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.rpc.RpcInvocation;
import org.apache.dubbo.rpc.protocol.dubbo.DubboInvoker;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ApacheDubboProviderInterceptorTest {

    @Mock
    private TraceContext traceContext;

    @Mock
    private MethodDescriptor descriptor;

    @Mock
    private SpanEventRecorder recorder;

    @Mock
    private RpcInvocation rpcInvocation;

    private Object obj = new Object();

    @Test
    public void doInBeforeTrace() {
        ApacheDubboProviderInterceptor interceptor = new ApacheDubboProviderInterceptor(traceContext, descriptor);
        RpcInvocation rpcInvocation = new RpcInvocation();
        rpcInvocation.setInvoker(new DubboInvoker(Object.class, new URL("http", "127.0.0.1", 8080), null));
        rpcInvocation.setMethodName("test");
        Object[] args = new Object[]{rpcInvocation};
        interceptor.doInBeforeTrace(recorder, obj, args);
    }

    @Test
    public void doInAfterTrace() {
        ApacheDubboProviderInterceptor interceptor = new ApacheDubboProviderInterceptor(traceContext, descriptor);
        Object[] args = new Object[]{rpcInvocation};
        interceptor.doInAfterTrace(recorder, obj, args, null, null);
    }
}