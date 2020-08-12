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

import java.util.HashMap;
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

    public RemotingServerInvocationInterceptor(TraceContext traceContext, MethodDescriptor descriptor) {
        super(traceContext, descriptor, RemotingServerInvocationInterceptor.class);

        traceContext.cacheApi(REMOTING_INVOCATION_METHOD_DESCRIPTOR);
    }

    /**
     * In this method, you have to check if the current request contains following
     * informations:
     * 
     * 1. Marker that indicates this transaction must not be traced 2. Data required
     * to continue tracing a transaction. transaction id, paraent id and so on.
     * 
     * Then you have to create appropriate Trace object.
     */
    @Override
    protected Trace createTrace(Object target, Object[] args) {
        final InvocationRequest invocationReq = (InvocationRequest) args[0];
        logger.debug("XXX invocationReq={}", invocationReq);

        final String subsystem = invocationReq.getSubsystem();

        // For now we are only interested in Aspect Oriented Programming subsystem
        if (!subsystem.equalsIgnoreCase("AOP")) {
            logger.debug("XXX subsystem not AOP, disable tracing");
            return traceContext.disableSampling();
        }

        Map<String, Object> metadata = invocationReq.getRequestPayload();

        if (metadata == null) {
            logger.debug("XXX There is no metadata in createTrace");
            metadata = new HashMap<String, Object>();
        }
        
        // If this transaction is not traceable, mark as disabled.
        if (metadata.get(JbossConstants.META_DO_NOT_TRACE) != null) {
            return traceContext.disableSampling();
        }
        
        String transactionId = (String) metadata.get(JbossConstants.META_TRANSACTION_ID);
        logger.debug("XXX transactionId={}", transactionId);

        // If there's no trasanction id, a new trasaction begins here. 
        if (transactionId == null) {
            return traceContext.newTraceObject();
        }

        // otherwise, continue tracing with given data.
        long parentSpanID = NumberUtils.parseLong((String) metadata.get(JbossConstants.META_PARENT_SPAN_ID), SpanId.NULL);
        long spanID = NumberUtils.parseLong((String) metadata.get(JbossConstants.META_SPAN_ID), SpanId.NULL);
        short flags = NumberUtils.parseShort((String) metadata.get(JbossConstants.META_FLAGS), (short) 0);
        logger.debug("XXX parentSpanID={}, spanID={}, flags={}", parentSpanID, spanID, flags);
        TraceId traceId = traceContext.createTraceId(transactionId, parentSpanID, spanID, flags);

        return traceContext.continueTraceObject(traceId);
    }
    
    
    @Override
    protected void doInBeforeTrace(SpanRecorder recorder, Object target, Object[] args) {
        //ServerInvoker server = (ServerInvoker)target;
        final InvocationRequest invocationReq = (InvocationRequest) args[0];
        Map<String, Object> metadata = invocationReq.getRequestPayload();
        if (metadata == null) {
            logger.debug("XXX There is no metadata in doInBeforeTrace");
            metadata = new HashMap<String, Object>();
        }
        
        recorder.recordServiceType(JbossConstants.JBOSS_REMOTING);
        
        String remoteAddress = (String) metadata.get(JbossConstants.META_CLIENT_ADDRESS);
        logger.debug("XXX remoteAddress={}", remoteAddress);
        recorder.recordRemoteAddress(remoteAddress);

        final Invocation invocation = (Invocation) invocationReq.getParameter();;
        logger.debug("XXX invocation={}", invocation);

        final String oid = (String)invocation.getMetaData("DISPATCHER", "OID");
        logger.debug("XXX oid={}", oid);

        recorder.recordRpcName(oid);

        InvokerLocator locator = (InvokerLocator)invocation.getMetaData("REMOTING", "INVOKER_LOCATOR");
        logger.debug("XXX locator={}", locator);

        String serverHostName = locator.getHost();                
        if (serverHostName != null) {
            final String endPoint = HostAndPort.toHostAndPortString(serverHostName, locator.getPort());
            logger.debug("XXX endPoint={}", endPoint);
            recorder.recordEndPoint(endPoint);
            recorder.recordAcceptorHost(endPoint);
        }

        // If this transaction did not begin here, record parent(client who sent this request) information 
        if (!recorder.isRoot()) {
            String parentApplicationName = (String) metadata.get(JbossConstants.META_PARENT_APPLICATION_NAME);
            logger.debug("XXX parentApplicationName={}", parentApplicationName);
            
            if (parentApplicationName != null) {
                short parentApplicationType = NumberUtils.parseShort((String) metadata.get(JbossConstants.META_PARENT_APPLICATION_TYPE), ServiceType.UNDEFINED.getCode());
                logger.debug("XXX parentApplicationType={}", parentApplicationType);
                recorder.recordParentApplication(parentApplicationName, parentApplicationType);
            }
        }
    }

    @Override
    protected void doInAfterTrace(SpanRecorder recorder, Object target, Object[] args, Object result, Throwable throwable) {
        final InvocationRequest invocationReq = (InvocationRequest) args[0];
        Map<String, Object> metadata = invocationReq.getRequestPayload();
        if (metadata == null) {
            logger.debug("XXX There is no metadata in doInAfterTrace");
            metadata = new HashMap<String, Object>();
        }

        recorder.recordApi(REMOTING_INVOCATION_METHOD_DESCRIPTOR);
        //recorder.recordAttribute(JbossConstants.MY_RPC_ARGUMENT_ANNOTATION_KEY, request.getArgument());
        
        if (throwable == null) {
            recorder.recordAttribute(AnnotationKey.RETURN_DATA, result);
        } else {
            recorder.recordException(throwable);
        }
    }
}
