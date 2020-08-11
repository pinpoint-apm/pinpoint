/*
 * Copyright 2016 Pinpoint contributors and NAVER Corp.
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

package com.navercorp.pinpoint.plugin.jboss.interceptor;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

import org.jboss.aop.joinpoint.Invocation;
import org.jboss.remoting.InvocationRequest;
import org.jboss.remoting.InvokerLocator;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.context.TraceId;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.common.plugin.util.HostAndPort;
import com.navercorp.pinpoint.common.trace.AnnotationKey;

import com.navercorp.pinpoint.plugin.jboss.JbossConstants;

/**
 * The Class ContextInvocationInterceptor.
 *
 * @author <a href="mailto:guillermoadrianmolina@hotmail.com">Guillermo Adrian
 *         Molina</a>
 * @author Guillermo Adrian Molina
 */
public class RemotingClientInvocationInterceptor implements AroundInterceptor {
    private final MethodDescriptor descriptor;
    private final TraceContext traceContext;

    protected final PLogger logger;
    protected final boolean isDebug;

    protected final String HOSTNAME = getHostname();

    public RemotingClientInvocationInterceptor(TraceContext traceContext, MethodDescriptor descriptor) {
        this.logger = PLoggerFactory.getLogger(this.getClass());
        this.isDebug = logger.isDebugEnabled();

        this.descriptor = descriptor;
        this.traceContext = traceContext;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor#before(java.lang.Object, java.lang.Object[])
     */
    @Override
    public void before(final Object target, final Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, args);
        }

        Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }

        final InvocationRequest invocationReq = (InvocationRequest) args[0];
        logger.debug("XXX invocationReq={}", invocationReq);

        Map<String, Object> metadata = invocationReq.getRequestPayload();
        if (metadata == null) {
            logger.debug("XXX There is no metadata");
            metadata = new HashMap<String, Object>();
            invocationReq.setRequestPayload(metadata);
        }
  
        if (trace.canSampled()) {
            SpanEventRecorder recorder = trace.traceBlockBegin();

            // RPC call trace have to be recorded with a service code in RPC client code range.
            recorder.recordServiceType(JbossConstants.JBOSS_REMOTING_CLIENT);

            // You have to issue a TraceId the receiver of this request will use.
            TraceId nextId = trace.getTraceId().getNextTraceId();

            // Then record it as next span id.
            recorder.recordNextSpanId(nextId.getSpanId());

            final Invocation invocation = (Invocation) invocationReq.getParameter();;
            logger.debug("XXX invocation={}", invocation);
 
            final String oid = (String)invocation.getMetaData("DISPATCHER", "OID");
            logger.debug("XXX oid={}", oid);

            recorder.recordDestinationId(oid);

            InvokerLocator locator = (InvokerLocator)invocation.getMetaData("REMOTING", "INVOKER_LOCATOR");
            logger.debug("XXX locator={}", locator);
        
            final String endPoint = HostAndPort.toHostAndPortString(locator.getHost(), locator.getPort());

            recorder.recordEndPoint(endPoint);
            logger.debug("XXX End Point={}", endPoint);

            // Finally, pass some tracing data to the server.
            // How to put them in a message is protocol specific.
            // This example assumes that the target protocol message can include any metadata (like HTTP headers).
            metadata.put(JbossConstants.META_TRANSACTION_ID, nextId.getTransactionId());
            metadata.put(JbossConstants.META_SPAN_ID, Long.toString(nextId.getSpanId()));
            metadata.put(JbossConstants.META_PARENT_SPAN_ID, Long.toString(nextId.getParentSpanId()));
            metadata.put(JbossConstants.META_PARENT_APPLICATION_TYPE, Short.toString(traceContext.getServerTypeCode()));
            metadata.put(JbossConstants.META_PARENT_APPLICATION_NAME, traceContext.getApplicationName());
            metadata.put(JbossConstants.META_FLAGS, Short.toString(nextId.getFlags()));
            metadata.put(JbossConstants.META_CLIENT_ADDRESS, HOSTNAME);
        } else {
            // If sampling this transaction is disabled, pass only that infomation to the server.  
            metadata.put(JbossConstants.META_DO_NOT_TRACE, "1");
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor#after(java.lang.Object, java.lang.Object[],
     * java.lang.Object, java.lang.Throwable)
     */
    @Override
    public void after(final Object target, final Object[] args, final Object result, final Throwable throwable) {
        if (isDebug) {
            logger.afterInterceptor(target, args, result, throwable);
        }
        Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }

        try {
            SpanEventRecorder recorder = trace.currentSpanEventRecorder();

            recorder.recordApi(descriptor);

            if (throwable == null) {
                //TargetClass13_Request request = (TargetClass13_Request) arg0;
                // Optionally, record the destination id (logical name of server. e.g. DB name)
                /*recorder.recordDestinationId(request.getNamespace());
                recorder.recordAttribute(JbossConstants.MY_RPC_PROCEDURE_ANNOTATION_KEY, request.getProcedure());
                recorder.recordAttribute(JbossConstants.MY_RPC_ARGUMENT_ANNOTATION_KEY, request.getArgument());
                */
                /*final Method method = (Method) args[1];
                final String methodName = getMethodName(method);
                logger.debug("XXX Method Name = {}", methodName);
                recorder.recordAttribute(JbossConstants.JBOSS_METHOD_ANNOTATION_KEY, methodName);*/
                recorder.recordAttribute(AnnotationKey.RETURN_DATA, result);
            } else {
                recorder.recordException(throwable);
            }
        } finally {
            trace.traceBlockEnd();
        }
    }

 
    private String getHostname() {
        String hostname = "localhost";
        try {
            hostname = InetAddress.getLocalHost().getHostName();
        } catch (final Throwable th) {
            if (logger.isWarnEnabled()) {
                logger.warn("getHostname. Caused:{}", th.getMessage(), th);
            }
        }
        return hostname;
    }
 }
