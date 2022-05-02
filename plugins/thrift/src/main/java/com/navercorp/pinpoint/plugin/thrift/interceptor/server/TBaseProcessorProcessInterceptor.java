/*
 * Copyright 2015 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.plugin.thrift.interceptor.server;


import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.InterceptorScope;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.InterceptorScopeInvocation;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.plugin.thrift.ThriftClientCallContext;
import com.navercorp.pinpoint.plugin.thrift.ThriftClientCallContextAttachmentFactory;
import com.navercorp.pinpoint.plugin.thrift.ThriftConstants;
import com.navercorp.pinpoint.plugin.thrift.ThriftUtils;
import org.apache.thrift.TBaseProcessor;

/**
 * Entry/exit point for tracing synchronous processors for Thrift services.
 * <p>
 * Because trace objects cannot be created until the message is read, this interceptor merely sends trace objects created by other interceptors in the tracing
 * pipeline:
 * <ol>
 * <li>
 * <p>
 * {@link com.navercorp.pinpoint.plugin.thrift.interceptor.server.ProcessFunctionProcessInterceptor ProcessFunctionProcessInterceptor} marks the start of a
 * trace, and sets up the environment for trace data to be injected.</li>
 * </p>
 *
 * <li>
 * <p>
 * {@link com.navercorp.pinpoint.plugin.thrift.interceptor.tprotocol.server.TProtocolReadFieldBeginInterceptor TProtocolReadFieldBeginInterceptor},
 * {@link com.navercorp.pinpoint.plugin.thrift.interceptor.tprotocol.server.TProtocolReadTTypeInterceptor TProtocolReadTTypeInterceptor} reads the header fields
 * and injects the parent trace object (if any).</li></p>
 *
 * <li>
 * <p>
 * {@link com.navercorp.pinpoint.plugin.thrift.interceptor.tprotocol.server.TProtocolReadMessageEndInterceptor TProtocolReadMessageEndInterceptor} creates the
 * actual root trace object.</li></p> </ol>
 * <p>
 * <b><tt>TBaseProcessorProcessInterceptor</tt></b> -> <tt>ProcessFunctionProcessInterceptor</tt> -> <tt>TProtocolReadFieldBeginInterceptor</tt> <->
 * <tt>TProtocolReadTTypeInterceptor</tt> -> <tt>TProtocolReadMessageEndInterceptor</tt>
 * <p>
 * Based on Thrift 0.8.0+
 *
 * @author HyunGil Jeong
 * @see com.navercorp.pinpoint.plugin.thrift.interceptor.server.ProcessFunctionProcessInterceptor ProcessFunctionProcessInterceptor
 * @see com.navercorp.pinpoint.plugin.thrift.interceptor.tprotocol.server.TProtocolReadFieldBeginInterceptor TProtocolReadFieldBeginInterceptor
 * @see com.navercorp.pinpoint.plugin.thrift.interceptor.tprotocol.server.TProtocolReadTTypeInterceptor TProtocolReadTTypeInterceptor
 * @see com.navercorp.pinpoint.plugin.thrift.interceptor.tprotocol.server.TProtocolReadMessageEndInterceptor TProtocolReadMessageEndInterceptor
 */
public class TBaseProcessorProcessInterceptor implements AroundInterceptor {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final TraceContext traceContext;
    private final MethodDescriptor descriptor;
    private final InterceptorScope scope;

    public TBaseProcessorProcessInterceptor(TraceContext traceContext, MethodDescriptor descriptor, InterceptorScope scope) {
        this.traceContext = traceContext;
        this.descriptor = descriptor;
        this.scope = scope;
    }

    @Override
    public void before(Object target, Object[] args) {
        final InterceptorScopeInvocation currentTransaction = this.scope.getCurrentInvocation();
        final Object attachment = currentTransaction.getOrCreateAttachment(ThriftClientCallContextAttachmentFactory.INSTANCE);
        if (attachment instanceof ThriftClientCallContext) {
            final ThriftClientCallContext clientCallContext = (ThriftClientCallContext) attachment;
            final String processName = toProcessName(target);
            clientCallContext.setProcessName(processName);
        }
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
    }

    private String toProcessName(Object target) {
        String methodUri = ThriftConstants.UNKNOWN_METHOD_URI;
        final InterceptorScopeInvocation currentTransaction = this.scope.getCurrentInvocation();
        final Object attachment = currentTransaction.getAttachment();
        if (attachment instanceof ThriftClientCallContext && target instanceof TBaseProcessor) {
            methodUri = ThriftUtils.getProcessorNameAsUri((TBaseProcessor<?>) target);
        }
        return methodUri;
    }
}
