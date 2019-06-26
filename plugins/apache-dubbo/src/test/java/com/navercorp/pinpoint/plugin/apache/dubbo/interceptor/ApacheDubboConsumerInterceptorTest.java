package com.navercorp.pinpoint.plugin.apache.dubbo.interceptor;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.context.TraceId;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.rpc.RpcInvocation;
import org.apache.dubbo.rpc.protocol.dubbo.DubboInvoker;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.doReturn;

@RunWith(MockitoJUnitRunner.class)
public class ApacheDubboConsumerInterceptorTest {

    @Mock
    private TraceContext traceContext;

    @Mock
    private MethodDescriptor descriptor;

    @Mock
    private Trace trace;

    @Mock
    private TraceId traceId;

    @Mock
    private TraceId nextId;

    @Mock
    private SpanEventRecorder spanRecorder;

    private Object obj = new Object();

    @Test
    public void before() {
        doReturn(trace).when(traceContext).currentRawTraceObject();
        doReturn(true).when(trace).canSampled();
        doReturn(traceId).when(trace).getTraceId();
        doReturn(nextId).when(traceId).getNextTraceId();
        doReturn(spanRecorder).when(trace).traceBlockBegin();

        ApacheDubboConsumerInterceptor interceptor = new ApacheDubboConsumerInterceptor(traceContext, descriptor);
        RpcInvocation rpcInvocation = new RpcInvocation();
        rpcInvocation.setInvoker(new DubboInvoker(Object.class, new URL("http", "127.0.0.1", 8080), null));
        rpcInvocation.setMethodName("test");
        Object[] args = new Object[]{rpcInvocation};
        interceptor.before(obj, args);
    }

    @Test
    public void after() {
        doReturn(trace).when(traceContext).currentTraceObject();
        doReturn(spanRecorder).when(trace).currentSpanEventRecorder();

        RpcInvocation rpcInvocation = new RpcInvocation();
        Object[] args = new Object[]{rpcInvocation};
        ApacheDubboConsumerInterceptor interceptor = new ApacheDubboConsumerInterceptor(traceContext, descriptor);
        interceptor.after(obj, args, null, null);
    }
}