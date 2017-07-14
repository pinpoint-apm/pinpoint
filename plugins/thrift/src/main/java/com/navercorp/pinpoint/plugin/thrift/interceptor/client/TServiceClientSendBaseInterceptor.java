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

package com.navercorp.pinpoint.plugin.thrift.interceptor.client;

import java.net.Socket;

import com.navercorp.pinpoint.bootstrap.interceptor.scope.InterceptorScope;
import com.navercorp.pinpoint.common.util.StringUtils;
import org.apache.thrift.TBase;
import org.apache.thrift.TServiceClient;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TTransport;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.context.TraceId;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.InterceptorScopeInvocation;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.plugin.thrift.ThriftConstants;
import com.navercorp.pinpoint.plugin.thrift.ThriftRequestProperty;
import com.navercorp.pinpoint.plugin.thrift.ThriftUtils;
import com.navercorp.pinpoint.plugin.thrift.field.accessor.SocketFieldAccessor;

/**
 * Starting point for tracing synchronous client calls for Thrift services.
 * <p>
 * Note that in order to trace remote agents, trace data must be sent to them. These data are serialized as Thrift fields and attached to the body of the Thrift
 * message by other interceptors down the chain.
 * <p>
 * <b><tt>TServiceClientSendBaseInterceptor</tt></b> -> <tt>TProtocolWriteFieldStopInterceptor</tt>
 * <p>
 * Based on Thrift 0.8.0+
 * 
 * @author HyunGil Jeong
 * 
 * @see com.navercorp.pinpoint.plugin.thrift.interceptor.tprotocol.client.TProtocolWriteFieldStopInterceptor TProtocolWriteFieldStopInterceptor
 */
public class TServiceClientSendBaseInterceptor implements AroundInterceptor {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final TraceContext traceContext;
    private final MethodDescriptor descriptor;
    private final InterceptorScope scope;

    private final boolean traceServiceArgs;

    public TServiceClientSendBaseInterceptor(TraceContext traceContext, MethodDescriptor descriptor, InterceptorScope scope, boolean traceServiceArgs) {
        this.traceContext = traceContext;
        this.descriptor = descriptor;
        this.scope = scope;
        this.traceServiceArgs = traceServiceArgs;
    }

    @Override
    public void before(Object target, Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, args);
        }
        if (target instanceof TServiceClient) {
            TServiceClient client = (TServiceClient)target;
            TProtocol oprot = client.getOutputProtocol();
            TTransport transport = oprot.getTransport();
            final Trace trace = traceContext.currentRawTraceObject();
            if (trace == null) {
                return;
            }
            ThriftRequestProperty parentTraceInfo = new ThriftRequestProperty();
            final boolean shouldSample = trace.canSampled();
            if (!shouldSample) {
                if (isDebug) {
                    logger.debug("set Sampling flag=false");
                }
                parentTraceInfo.setShouldSample(shouldSample);
            } else {
                SpanEventRecorder recorder = trace.traceBlockBegin();
                recorder.recordServiceType(ThriftConstants.THRIFT_CLIENT);

                // retrieve connection information
                String remoteAddress = ThriftConstants.UNKNOWN_ADDRESS;
                if (transport instanceof SocketFieldAccessor) {
                    Socket socket = ((SocketFieldAccessor)transport)._$PINPOINT$_getSocket();
                    if (socket != null) {
                        remoteAddress = ThriftUtils.getHostPort(socket.getRemoteSocketAddress());
                    }
                } else {
                    if (isDebug) {
                        logger.debug("Invalid target object. Need field accessor({}).", SocketFieldAccessor.class.getName());
                    }
                }
                recorder.recordDestinationId(remoteAddress);

                String methodName = ThriftConstants.UNKNOWN_METHOD_NAME;
                if (args[0] instanceof String) {
                    methodName = (String)args[0];
                }
                String serviceName = ThriftUtils.getClientServiceName(client);

                String thriftUrl = getServiceUrl(remoteAddress, serviceName, methodName);
                recorder.recordAttribute(ThriftConstants.THRIFT_URL, thriftUrl);

                TraceId nextId = trace.getTraceId().getNextTraceId();
                recorder.recordNextSpanId(nextId.getSpanId());

                parentTraceInfo.setTraceId(nextId.getTransactionId());
                parentTraceInfo.setSpanId(nextId.getSpanId());
                parentTraceInfo.setParentSpanId(nextId.getParentSpanId());

                parentTraceInfo.setFlags(nextId.getFlags());
                parentTraceInfo.setParentApplicationName(traceContext.getApplicationName());
                parentTraceInfo.setParentApplicationType(traceContext.getServerTypeCode());
                parentTraceInfo.setAcceptorHost(remoteAddress);
            }
            InterceptorScopeInvocation currentTransaction = this.scope.getCurrentInvocation();
            currentTransaction.setAttachment(parentTraceInfo);
        }
    }

    private String getServiceUrl(String url, String serviceName, String methodName) {
        StringBuilder sb = new StringBuilder();
        sb.append(url).append("/").append(serviceName).append("/").append(methodName);
        return sb.toString();
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            logger.afterInterceptor(target, args, result, throwable);
        }

        Trace trace = this.traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }

        try {
            SpanEventRecorder recorder = trace.currentSpanEventRecorder();
            if (this.traceServiceArgs) {
                if (args.length == 2 && (args[1] instanceof TBase)) {
                    recorder.recordAttribute(ThriftConstants.THRIFT_ARGS, getMethodArgs((TBase<?, ?>)args[1]));
                }
            }
            recorder.recordApi(descriptor);
            recorder.recordException(throwable);
        } finally {
            trace.traceBlockEnd();
        }
    }

    private String getMethodArgs(TBase<?, ?> args) {
        return StringUtils.abbreviate(args.toString(), 256);
    }

}
