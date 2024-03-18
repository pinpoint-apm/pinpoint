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
import com.navercorp.pinpoint.bootstrap.context.SpanId;
import com.navercorp.pinpoint.bootstrap.context.SpanRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.context.TraceId;
import com.navercorp.pinpoint.bootstrap.interceptor.SpanRecursiveAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.util.NumberUtils;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.plugin.apache.dubbo.ApacheDubboConstants;
import com.navercorp.pinpoint.plugin.apache.dubbo.ApacheDubboProviderMethodDescriptor;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.RpcContext;
import org.apache.dubbo.rpc.RpcInvocation;

import java.util.Map;

/**
 * @author K
 */
public class ApacheDubboProviderInterceptor extends SpanRecursiveAroundInterceptor {
    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private static final String SCOPE_NAME = "##DUBBO_PROVIDER_TRACE";
    private static final MethodDescriptor DUBBO_PROVIDER_METHOD_DESCRIPTOR = new ApacheDubboProviderMethodDescriptor();

    public ApacheDubboProviderInterceptor(TraceContext traceContext, MethodDescriptor descriptor) {
        super(traceContext, descriptor, SCOPE_NAME);
        traceContext.cacheApi(DUBBO_PROVIDER_METHOD_DESCRIPTOR);
    }

    @Override
    protected Trace createTrace(Object target, Object[] args) {
        final Trace trace = readRequestTrace(target, args);
        if (trace.canSampled()) {
            final SpanRecorder recorder = trace.getSpanRecorder();
            // You have to record a service type within Server range.
            recorder.recordServiceType(ApacheDubboConstants.DUBBO_PROVIDER_SERVICE_TYPE);
            recorder.recordApi(DUBBO_PROVIDER_METHOD_DESCRIPTOR);
            recordRequest(recorder, target, args);
        }

        return trace;
    }

    private Trace readRequestTrace(Object target, Object[] args) {
        final Invoker invoker = (Invoker) target;
        // Ignore monitor service.
        if (ApacheDubboConstants.MONITOR_SERVICE_FQCN.equals(invoker.getInterface().getName())) {
            return traceContext.disableSampling();
        }

        final RpcInvocation invocation = (RpcInvocation) args[0];
        // If this transaction is not traceable, mark as disabled.
        if (invocation.getAttachment(ApacheDubboConstants.META_DO_NOT_TRACE) != null) {
            return traceContext.disableSampling();
        }
        final String transactionId = invocation.getAttachment(ApacheDubboConstants.META_TRANSACTION_ID);
        // If there's no trasanction id, a new trasaction begins here.
        // FIXME There seems to be cases where the invoke method is called after a span is already created.
        // We'll have to check if a trace object already exists and create a span event instead of a span in that case.
        if (transactionId == null) {
            return traceContext.newTraceObject();
        }

        // otherwise, continue tracing with given data.
        final long parentSpanID = NumberUtils.parseLong(invocation.getAttachment(ApacheDubboConstants.META_PARENT_SPAN_ID), SpanId.NULL);
        final long spanID = NumberUtils.parseLong(invocation.getAttachment(ApacheDubboConstants.META_SPAN_ID), SpanId.NULL);
        final short flags = NumberUtils.parseShort(invocation.getAttachment(ApacheDubboConstants.META_FLAGS), (short) 0);
        final TraceId traceId = traceContext.createTraceId(transactionId, parentSpanID, spanID, flags);

        return traceContext.continueTraceObject(traceId);
    }

    private void recordRequest(SpanRecorder recorder, Object target, Object[] args) {
        final RpcInvocation invocation = (RpcInvocation) args[0];
        final RpcContext rpcContext = RpcContext.getContext();

        // Record rpc name, client address, server address.
        recorder.recordRpcName(invocation.getInvoker().getInterface().getSimpleName() + ":" + invocation.getMethodName());
        recorder.recordEndPoint(rpcContext.getLocalAddressString());
        if (rpcContext.getRemoteHost() != null) {
            recorder.recordRemoteAddress(rpcContext.getRemoteAddressString());
        } else {
            recorder.recordRemoteAddress("Unknown");
        }

        // If this transaction did not begin here, record parent(client who sent this request) information
        if (!recorder.isRoot()) {
            final String parentApplicationName = invocation.getAttachment(ApacheDubboConstants.META_PARENT_APPLICATION_NAME);
            if (parentApplicationName != null) {
                final short parentApplicationType = NumberUtils.parseShort(invocation.getAttachment(ApacheDubboConstants.META_PARENT_APPLICATION_TYPE), ServiceType.UNDEFINED.getCode());
                recorder.recordParentApplication(parentApplicationName, parentApplicationType);

                final String host = invocation.getAttachment(ApacheDubboConstants.META_HOST);
                if (host != null) {
                    recorder.recordAcceptorHost(host);
                } else {
                    // old version fallback
                    final String estimatedLocalHost = getLocalHost(rpcContext);
                    if (estimatedLocalHost != null) {
                        recorder.recordAcceptorHost(estimatedLocalHost);
                    }
                }
            }
        }
        //clear attachments
        this.clearAttachments(rpcContext);
    }

    /**
     * clear {@link org.apache.dubbo.rpc.RpcContext#getAttachments()} trace header.
     * you should to know,since apache dubbo 2.6.2 version.
     * {@link org.apache.dubbo.rpc.protocol.AbstractInvoker#invoke(org.apache.dubbo.rpc.Invocation)}
     * will force put {@link org.apache.dubbo.rpc.RpcContext#getAttachments()} to current Invocation
     * replace origin invocation.addAttachmentsIfAbsent(context) method;
     * to imagine if application(B) methodB called by application(A), application(B) is dubbo provider, methodB call next dubbo application(C).
     * when application(C) received trace header is overwrite by application(B) received trace header.
     *
     * @param rpcContext
     */
    private void clearAttachments(RpcContext rpcContext) {
        Map<String, String> attachments = rpcContext.getAttachments();
        if (attachments != null) {
            attachments.remove(ApacheDubboConstants.META_TRANSACTION_ID);
            attachments.remove(ApacheDubboConstants.META_SPAN_ID);
            attachments.remove(ApacheDubboConstants.META_PARENT_SPAN_ID);
            attachments.remove(ApacheDubboConstants.META_PARENT_APPLICATION_TYPE);
            attachments.remove(ApacheDubboConstants.META_PARENT_APPLICATION_NAME);
            attachments.remove(ApacheDubboConstants.META_FLAGS);
            attachments.remove(ApacheDubboConstants.META_HOST);
        }
    }

    private String getLocalHost(RpcContext rpcContext) {
        // Pinpoint finds caller - callee relation by matching caller's end point and callee's acceptor host.
        // https://github.com/naver/pinpoint/issues/1395
        // @Nullable
        final String localHost = NetworkUtils.getLocalHost();
        if (localHost == null) {
            logger.debug("NetworkUtils.getLocalHost() is null");
        }
        final String rpcContextLocalhost = rpcContext.getLocalHost();
        if (rpcContextLocalhost == null) {
            logger.debug("rpcContext.getLocalHost() is null");
        }
        if (localHost == null && rpcContextLocalhost == null) {
            logger.debug("localHost == null && rpcContextLocalhost == null");
            return null;
        }
        if (localHost == null) {
            logger.debug("return rpcContextLocalhost:{}", rpcContextLocalhost);
            return rpcContextLocalhost;
        }
        if (localHost.equals(rpcContextLocalhost)) {
            return rpcContext.getLocalAddressString();
        } else {
            return localHost + ":" + rpcContext.getLocalPort();
        }
    }

    @Override
    protected void doInBeforeTrace(SpanEventRecorder recorder, Object target, Object[] args) {
        final RpcInvocation invocation = (RpcInvocation) args[0];
        recorder.recordServiceType(ApacheDubboConstants.DUBBO_PROVIDER_SERVICE_NO_STATISTICS_TYPE);
        recorder.recordApi(methodDescriptor);
        recorder.recordAttribute(ApacheDubboConstants.DUBBO_RPC_ANNOTATION_KEY,
                invocation.getInvoker().getInterface().getSimpleName() + ":" + invocation.getMethodName());
    }

    @Override
    protected void doInAfterTrace(SpanEventRecorder recorder, Object target, Object[] args, Object result, Throwable throwable) {
        final RpcInvocation invocation = (RpcInvocation) args[0];
        recorder.recordServiceType(ApacheDubboConstants.DUBBO_PROVIDER_SERVICE_NO_STATISTICS_TYPE);
        recorder.recordApi(methodDescriptor);
        recorder.recordAttribute(ApacheDubboConstants.DUBBO_ARGS_ANNOTATION_KEY, invocation.getArguments());

        if (throwable == null) {
            recorder.recordAttribute(ApacheDubboConstants.DUBBO_RESULT_ANNOTATION_KEY, result);
        } else {
            recorder.recordException(throwable);
        }
    }

}