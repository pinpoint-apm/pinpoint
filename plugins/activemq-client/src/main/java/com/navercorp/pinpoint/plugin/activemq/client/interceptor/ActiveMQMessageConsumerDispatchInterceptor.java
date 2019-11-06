/*
 * Copyright 2016 Naver Corp.
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

package com.navercorp.pinpoint.plugin.activemq.client.interceptor;

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessor;
import com.navercorp.pinpoint.bootstrap.config.Filter;
import com.navercorp.pinpoint.bootstrap.context.*;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.common.plugin.util.HostAndPort;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.plugin.activemq.client.ActiveMQClientConstants;
import com.navercorp.pinpoint.plugin.activemq.client.ActiveMQClientHeader;
import com.navercorp.pinpoint.plugin.activemq.client.ActiveMQClientUtils;
import com.navercorp.pinpoint.plugin.activemq.client.descriptor.ActiveMQConsumerEntryMethodDescriptor;
import com.navercorp.pinpoint.plugin.activemq.client.field.getter.ActiveMQSessionGetter;
import com.navercorp.pinpoint.plugin.activemq.client.field.getter.SocketGetter;
import com.navercorp.pinpoint.plugin.activemq.client.field.getter.TransportGetter;
import com.navercorp.pinpoint.plugin.activemq.client.field.getter.URIGetter;
import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQMessageConsumer;
import org.apache.activemq.ActiveMQSession;
import org.apache.activemq.command.ActiveMQDestination;
import org.apache.activemq.command.ActiveMQMessage;
import org.apache.activemq.command.Message;
import org.apache.activemq.command.MessageDispatch;
import org.apache.activemq.transport.Transport;
import org.apache.activemq.transport.TransportFilter;
import org.apache.activemq.transport.failover.FailoverTransport;

import java.net.Socket;
import java.net.SocketAddress;
import java.net.URI;

/**
 * @author HyunGil Jeong
 */
public class ActiveMQMessageConsumerDispatchInterceptor implements AroundInterceptor {

    private static final ActiveMQConsumerEntryMethodDescriptor CONSUMER_ENTRY_METHOD_DESCRIPTOR = new ActiveMQConsumerEntryMethodDescriptor();

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final TraceContext traceContext;
    private final MethodDescriptor methodDescriptor;
    private final Filter<String> excludeDestinationFilter;

    public ActiveMQMessageConsumerDispatchInterceptor(TraceContext traceContext, MethodDescriptor methodDescriptor, Filter<String> excludeDestinationFilter) {
        this.traceContext = traceContext;
        this.methodDescriptor = methodDescriptor;
        this.excludeDestinationFilter = excludeDestinationFilter;
        traceContext.cacheApi(CONSUMER_ENTRY_METHOD_DESCRIPTOR);
    }

    @Override
    public void before(Object target, Object[] args) {
        if (!validate(target, args)) {
            return;
        }
        if (isDebug) {
            logger.beforeInterceptor(target, args);
        }

        try {
            final Trace trace = createTrace(target, args);
            if (trace == null) {
                return;
            }
            if (!trace.canSampled()) {
                return;
            }
            // ------------------------------------------------------
            SpanEventRecorder recorder = trace.traceBlockBegin();
            recorder.recordServiceType(ActiveMQClientConstants.ACTIVEMQ_CLIENT_INTERNAL);
            if (args[0] instanceof AsyncContextAccessor) {
                AsyncContext asyncContext = recorder.recordNextAsyncContext();
                ((AsyncContextAccessor) args[0])._$PINPOINT$_setAsyncContext(asyncContext);
            }
        } catch (Throwable th) {
            if (logger.isWarnEnabled()) {
                logger.warn("BEFORE. Caused:{}", th.getMessage(), th);
            }
        }
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (!validate(target, args)) {
            return;
        }
        if (isDebug) {
            logger.afterInterceptor(target, args, result, throwable);
        }

        final Trace trace = traceContext.currentRawTraceObject();
        if (trace == null) {
            return;
        }
        if (!trace.canSampled()) {
            traceContext.removeTraceObject();
            return;
        }
        try {
            SpanEventRecorder recorder = trace.currentSpanEventRecorder();
            recorder.recordApi(methodDescriptor);
            if (throwable != null) {
                recorder.recordException(throwable);
            }
        } catch (Throwable th) {
            if (logger.isWarnEnabled()) {
                logger.warn("after. Caused:{}", th.getMessage(), th);
            }
        } finally {
            traceContext.removeTraceObject();
            trace.traceBlockEnd();
            trace.close();
        }
    }

    private Trace createTrace(Object target, Object[] args) {
        if (!validate(target, args)) {
            return null;
        }
        MessageDispatch md = (MessageDispatch) args[0];
        ActiveMQMessage message = (ActiveMQMessage) md.getMessage();
        if (filterDestination(message.getDestination())) {
            return null;
        }
        // These might trigger unmarshalling.
        if (!ActiveMQClientHeader.getSampled(message, true)) {
            return traceContext.disableSampling();
        }

        final TraceId traceId = populateTraceIdFromRequest(message);
        final Trace trace = traceId == null ? traceContext.newTraceObject() : traceContext.continueTraceObject(traceId);
        if (trace.canSampled()) {
            SpanRecorder recorder = trace.getSpanRecorder();
            recordRootSpan(recorder, target, args);
        }
        return trace;
    }

    private TraceId populateTraceIdFromRequest(ActiveMQMessage message) {
        String transactionId = ActiveMQClientHeader.getTraceId(message, null);
        if (transactionId == null) {
            return null;
        }
        long parentSpanId = ActiveMQClientHeader.getParentSpanId(message, SpanId.NULL);
        long spanId = ActiveMQClientHeader.getSpanId(message, SpanId.NULL);
        short flags = ActiveMQClientHeader.getFlags(message, (short) 0);
        return traceContext.createTraceId(transactionId, parentSpanId, spanId, flags);
    }

    private boolean filterDestination(ActiveMQDestination destination) {
        String destinationName = destination.getPhysicalName();
        return this.excludeDestinationFilter.filter(destinationName);
    }

    private void recordRootSpan(SpanRecorder recorder, Object target, Object[] args) {
        recorder.recordServiceType(ActiveMQClientConstants.ACTIVEMQ_CLIENT);
        recorder.recordApi(CONSUMER_ENTRY_METHOD_DESCRIPTOR);

        ActiveMQSession session = ((ActiveMQSessionGetter) target)._$PINPOINT$_getActiveMQSession();
        ActiveMQConnection connection = session.getConnection();
        Transport transport = getRootTransport(((TransportGetter) connection)._$PINPOINT$_getTransport());

        final String endPoint = getEndPoint(transport);
        // Endpoint should be the local socket address of the consumer.
        recorder.recordEndPoint(endPoint);

        final String remoteAddress = transport.getRemoteAddress();
        // Remote address is the socket address of where the consumer is connected to.
        recorder.recordRemoteAddress(remoteAddress);

        MessageDispatch md = (MessageDispatch) args[0];
        ActiveMQMessage message = (ActiveMQMessage) md.getMessage();

        ActiveMQDestination destination = message.getDestination();
        // Rpc name is the URI of the queue/topic we're consuming from.
        recorder.recordRpcName(destination.getQualifiedName());
        // Record acceptor host as the queue/topic name in order to generate virtual queue node.
        recorder.recordAcceptorHost(destination.getPhysicalName());

        String parentApplicationName = ActiveMQClientHeader.getParentApplicationName(message, null);
        if (!recorder.isRoot() && parentApplicationName != null) {
            short parentApplicationType = ActiveMQClientHeader.getParentApplicationType(message, ServiceType.UNDEFINED.getCode());
            recorder.recordParentApplication(parentApplicationName, parentApplicationType);
        }
    }

    private String getEndPoint(Transport transport) {
        if (transport instanceof SocketGetter) {
            Socket socket = ((SocketGetter) transport)._$PINPOINT$_getSocket();
            SocketAddress localSocketAddress = socket.getLocalSocketAddress();
            return ActiveMQClientUtils.getEndPoint(localSocketAddress);
        } else if (transport instanceof URIGetter) {
            URI uri = ((URIGetter) transport)._$PINPOINT$_getUri();
            return HostAndPort.toHostAndPortString(uri.getHost(), uri.getPort());
        }
        return null;
    }

    private boolean validate(Object target, Object[] args) {
        if (!(target instanceof ActiveMQMessageConsumer)) {
            return false;
        }
        if (!(target instanceof ActiveMQSessionGetter)) {
            if (isDebug) {
                logger.debug("Invalid target object. Need field accessor({}).", ActiveMQSessionGetter.class.getName());
            }
            return false;
        }
        if (!validateTransport(((ActiveMQSessionGetter) target)._$PINPOINT$_getActiveMQSession())) {
            return false;
        }
        if (args == null || args.length < 1) {
            return false;
        }
        if (!(args[0] instanceof MessageDispatch)) {
            return false;
        }
        MessageDispatch md = (MessageDispatch) args[0];
        Message message = md.getMessage();
        if (!(message instanceof ActiveMQMessage)) {
            return false;
        }
        return true;
    }

    private boolean validateTransport(ActiveMQSession session) {
        if (session == null) {
            return false;
        }
        ActiveMQConnection connection = session.getConnection();
        if (!(connection instanceof TransportGetter)) {
            if (isDebug) {
                logger.debug("Invalid connection object. Need field accessor({}).", TransportGetter.class.getName());
            }
            return false;
        }
        Transport transport = getRootTransport(((TransportGetter) connection)._$PINPOINT$_getTransport());
        if (!(transport instanceof SocketGetter)) {
            if (isDebug) {
                logger.debug("Transport not traceable({}).", transport.getClass().getName());
            }
            return false;
        }
        return true;
    }

    private Transport getRootTransport(Transport transport) {
        Transport possiblyWrappedTransport = transport;
        while (possiblyWrappedTransport instanceof TransportFilter) {
            possiblyWrappedTransport = ((TransportFilter) possiblyWrappedTransport).getNext();
            if (possiblyWrappedTransport instanceof FailoverTransport) {
                possiblyWrappedTransport = ((FailoverTransport) possiblyWrappedTransport).getConnectedTransport();
            }
        }
        return possiblyWrappedTransport;
    }
}
