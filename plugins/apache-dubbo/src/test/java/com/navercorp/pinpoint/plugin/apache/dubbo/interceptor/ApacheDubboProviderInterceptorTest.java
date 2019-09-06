/*
 * Copyright 2019 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.plugin.apache.dubbo.interceptor;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.SpanRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.plugin.apache.dubbo.ApacheDubboConstants;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.RpcInvocation;
import org.apache.dubbo.rpc.protocol.dubbo.DubboInvoker;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.UUID;

import static org.mockito.Mockito.doReturn;

@RunWith(MockitoJUnitRunner.class)
public class ApacheDubboProviderInterceptorTest {

    @Mock
    private TraceContext traceContext;

    @Mock
    private MethodDescriptor descriptor;

    @Mock
    private SpanEventRecorder recorder;

    @Mock
    private Trace trace;

    @Mock
    private SpanRecorder spanRecorder;

    @Mock
    private RpcInvocation rpcInvocation;

    private Object obj = new Object();

    @Test
    public void createTrace() {
        doReturn(true).when(trace).canSampled();
        doReturn(spanRecorder).when(trace).getSpanRecorder();
        doReturn(trace).when(traceContext).newTraceObject();

        Invoker invoker = new DubboInvoker(Object.class, new URL("http", "127.0.0.1", 8080), null);
        ApacheDubboProviderInterceptor interceptor = new ApacheDubboProviderInterceptor(traceContext, descriptor);
        RpcInvocation rpcInvocation = new RpcInvocation();
        rpcInvocation.setInvoker(invoker);
        rpcInvocation.setMethodName("test");
        rpcInvocation.setAttachment(ApacheDubboConstants.META_PARENT_APPLICATION_NAME, UUID.randomUUID().toString());
        Object[] args = new Object[]{rpcInvocation};
        interceptor.createTrace(invoker, args);
    }

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