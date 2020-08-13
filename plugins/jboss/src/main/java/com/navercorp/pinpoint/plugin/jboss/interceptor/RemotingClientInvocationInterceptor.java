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

    public RemotingClientInvocationInterceptor(final TraceContext traceContext, final MethodDescriptor descriptor) {
        this.logger = PLoggerFactory.getLogger(this.getClass());
        this.isDebug = logger.isDebugEnabled();

        this.descriptor = descriptor;
        this.traceContext = traceContext;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor#before(java.
     * lang.Object, java.lang.Object[])
     */
    @Override
    public void before(final Object target, final Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, args);
        }

        final Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }

        final InvocationRequest invocationReq = (InvocationRequest) args[0];

        @SuppressWarnings("unchecked")
        Map<String, Object> metadata = invocationReq.getRequestPayload();
        if (metadata == null) {
            metadata = new HashMap<String, Object>();
            invocationReq.setRequestPayload(metadata);
        }

        if (trace.canSampled()) {
            final SpanEventRecorder recorder = trace.traceBlockBegin();

            recorder.recordServiceType(JbossConstants.JBOSS_REMOTING_CLIENT);

            final TraceId nextId = trace.getTraceId().getNextTraceId();
            recorder.recordNextSpanId(nextId.getSpanId());

            final Invocation invocation = (Invocation) invocationReq.getParameter();
            final InvokerLocator locator = (InvokerLocator) invocation.getMetaData("REMOTING", "INVOKER_LOCATOR");
            final String endPoint = HostAndPort.toHostAndPortString(locator.getHost(), locator.getPort());
            recorder.recordDestinationId(endPoint);
            recorder.recordEndPoint(endPoint);

            metadata.put(JbossConstants.META_TRANSACTION_ID, nextId.getTransactionId());
            metadata.put(JbossConstants.META_SPAN_ID, Long.toString(nextId.getSpanId()));
            metadata.put(JbossConstants.META_PARENT_SPAN_ID, Long.toString(nextId.getParentSpanId()));
            metadata.put(JbossConstants.META_PARENT_APPLICATION_TYPE, Short.toString(traceContext.getServerTypeCode()));
            metadata.put(JbossConstants.META_PARENT_APPLICATION_NAME, traceContext.getApplicationName());
            metadata.put(JbossConstants.META_FLAGS, Short.toString(nextId.getFlags()));
            metadata.put(JbossConstants.META_CLIENT_ADDRESS, HOSTNAME);
        } else {
            metadata.put(JbossConstants.META_DO_NOT_TRACE, "1");
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor#after(java.
     * lang.Object, java.lang.Object[], java.lang.Object, java.lang.Throwable)
     */
    @Override
    public void after(final Object target, final Object[] args, final Object result, final Throwable throwable) {
        if (isDebug) {
            logger.afterInterceptor(target, args, result, throwable);
        }
        final Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }

        try {
            final SpanEventRecorder recorder = trace.currentSpanEventRecorder();

            recorder.recordApi(descriptor);

            if (throwable == null) {
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
