/**
 * Copyright 2014 NAVER Corp.
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
package com.navercorp.pinpoint.plugin.jboss.interceptor;

import org.jboss.aop.joinpoint.Invocation;
import org.jboss.remoting.InvocationRequest;
import org.jboss.remoting.InvokerLocator;

import java.util.Map;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanId;
import com.navercorp.pinpoint.bootstrap.context.SpanRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.context.TraceId;
import com.navercorp.pinpoint.bootstrap.interceptor.SpanSimpleAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.util.NumberUtils;
import com.navercorp.pinpoint.common.plugin.util.HostAndPort;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.trace.ServiceType;

import com.navercorp.pinpoint.plugin.jboss.JbossConstants;
import com.navercorp.pinpoint.plugin.jboss.descriptor.RemotingInvocationMethodDescriptor;

public class RemotingServerInvocationInterceptor extends SpanSimpleAroundInterceptor {

    protected static final RemotingInvocationMethodDescriptor REMOTING_INVOCATION_METHOD_DESCRIPTOR = new RemotingInvocationMethodDescriptor();

    public RemotingServerInvocationInterceptor(final TraceContext traceContext, final MethodDescriptor descriptor) {
        super(traceContext, descriptor, RemotingServerInvocationInterceptor.class);

        traceContext.cacheApi(REMOTING_INVOCATION_METHOD_DESCRIPTOR);
    }

    @Override
    protected Trace createTrace(final Object target, final Object[] args) {
        final InvocationRequest invocationReq = (InvocationRequest) args[0];
        final String subsystem = invocationReq.getSubsystem();

        // For now we are only interested in Aspect Oriented Programming subsystem
        if (!subsystem.equalsIgnoreCase("AOP")) {
            logger.debug("Subsystem not AOP, disable tracing");
            return traceContext.disableSampling();
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> metadata = invocationReq.getRequestPayload();

        if (metadata != null) {
            // If this transaction is not traceable, mark as disabled.
            if (metadata.get(JbossConstants.META_DO_NOT_TRACE) != null) {
                return traceContext.disableSampling();
            }

            final String transactionId = (String) metadata.get(JbossConstants.META_TRANSACTION_ID);
            // If there is a transaction ID continue tracing
            if (transactionId != null) {
                final long parentSpanID = NumberUtils
                        .parseLong((String) metadata.get(JbossConstants.META_PARENT_SPAN_ID), SpanId.NULL);
                final long spanID = NumberUtils.parseLong((String) metadata.get(JbossConstants.META_SPAN_ID),
                        SpanId.NULL);
                final short flags = NumberUtils.parseShort((String) metadata.get(JbossConstants.META_FLAGS), (short) 0);
                final TraceId traceId = traceContext.createTraceId(transactionId, parentSpanID, spanID, flags);

                return traceContext.continueTraceObject(traceId);
            }

        }

        return traceContext.newTraceObject();
    }

    @Override
    protected void doInBeforeTrace(final SpanRecorder recorder, final Object target, final Object[] args) {
        recorder.recordServiceType(JbossConstants.JBOSS_REMOTING);

        final InvocationRequest invocationReq = (InvocationRequest) args[0];

        @SuppressWarnings("unchecked")
        Map<String, Object> metadata = invocationReq.getRequestPayload();
        if (metadata != null) {
            final String remoteAddress = (String) metadata.get(JbossConstants.META_CLIENT_ADDRESS);
            recorder.recordRemoteAddress(remoteAddress);

            // If this transaction did not begin here, record parent(client who sent this
            // request) information
            if (!recorder.isRoot()) {
                final String parentApplicationName = (String) metadata.get(JbossConstants.META_PARENT_APPLICATION_NAME);
                if (parentApplicationName != null) {
                    final short parentApplicationType = NumberUtils.parseShort(
                            (String) metadata.get(JbossConstants.META_PARENT_APPLICATION_TYPE),
                            ServiceType.UNDEFINED.getCode());
                    recorder.recordParentApplication(parentApplicationName, parentApplicationType);
                }
            }
        }

        final Invocation invocation = (Invocation) invocationReq.getParameter();
        final String oid = (String) invocation.getMetaData("DISPATCHER", "OID");
        recorder.recordRpcName(oid);

        final InvokerLocator locator = (InvokerLocator) invocation.getMetaData("REMOTING", "INVOKER_LOCATOR");
        final String serverHostName = locator.getHost();
        if (serverHostName != null) {
            final String endPoint = HostAndPort.toHostAndPortString(serverHostName, locator.getPort());
            recorder.recordEndPoint(endPoint);
            recorder.recordAcceptorHost(endPoint);
        }

    }

    @Override
    protected void doInAfterTrace(final SpanRecorder recorder, final Object target, final Object[] args,
            final Object result, final Throwable throwable) {
        recorder.recordApi(REMOTING_INVOCATION_METHOD_DESCRIPTOR);
        if (throwable == null) {
            recorder.recordAttribute(AnnotationKey.RETURN_DATA, result);
        } else {
            recorder.recordException(throwable);
        }
    }
}
