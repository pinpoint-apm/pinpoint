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

import com.navercorp.pinpoint.bootstrap.config.Filter;
import com.navercorp.pinpoint.bootstrap.context.*;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.plugin.activemq.client.ActiveMQClientConstants;
import com.navercorp.pinpoint.plugin.activemq.client.ActiveMQClientHeader;
import com.navercorp.pinpoint.plugin.activemq.client.field.getter.ActiveMQSessionGetter;
import com.navercorp.pinpoint.plugin.activemq.client.field.getter.SocketGetter;
import com.navercorp.pinpoint.plugin.activemq.client.field.getter.TransportGetter;
import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQMessageProducer;
import org.apache.activemq.ActiveMQSession;
import org.apache.activemq.command.ActiveMQDestination;
import org.apache.activemq.transport.Transport;
import org.apache.activemq.transport.TransportFilter;
import org.apache.activemq.transport.failover.FailoverTransport;

import javax.jms.Message;

/**
 * @author HyunGil Jeong
 */
public class ActiveMQMessageProducerSendInterceptor implements AroundInterceptor {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final TraceContext traceContext;
    private final MethodDescriptor descriptor;
    private final Filter<String> excludeDestinationFilter;

    public ActiveMQMessageProducerSendInterceptor(TraceContext traceContext, MethodDescriptor descriptor, Filter<String> excludeDestinationFilter) {
        this.traceContext = traceContext;
        this.descriptor = descriptor;
        this.excludeDestinationFilter = excludeDestinationFilter;
    }

    @Override
    public void before(Object target, Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, args);
        }
        if (!validate(target, args)) {
            return;
        }
        ActiveMQDestination activeMQDestination = (ActiveMQDestination) args[0];
        if (filterDestination(activeMQDestination)) {
            return;
        }

        Trace trace = traceContext.currentRawTraceObject();

        if (trace == null) {
            return;
        }

        Message message = (Message) args[1];
        try {
            if (trace.canSampled()) {
                SpanEventRecorder recorder = trace.traceBlockBegin();
                recorder.recordServiceType(ActiveMQClientConstants.ACTIVEMQ_CLIENT);

                TraceId nextId = trace.getTraceId().getNextTraceId();
                recorder.recordNextSpanId(nextId.getSpanId());

                ActiveMQClientHeader.setTraceId(message, nextId.getTransactionId());
                ActiveMQClientHeader.setSpanId(message, nextId.getSpanId());
                ActiveMQClientHeader.setParentSpanId(message, nextId.getParentSpanId());
                ActiveMQClientHeader.setFlags(message, nextId.getFlags());
                ActiveMQClientHeader.setParentApplicationName(message, traceContext.getApplicationName());
                ActiveMQClientHeader.setParentApplicationType(message, traceContext.getServerTypeCode());
            } else {
                ActiveMQClientHeader.setSampled(message, false);
            }
        } catch (Throwable t) {
            logger.warn("BEFORE. Cause:{}", t.getMessage(), t);
        }
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            logger.afterInterceptor(target, args);
        }
        if (!validate(target, args)) {
            return;
        }
        Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }

        try {
            SpanEventRecorder recorder = trace.currentSpanEventRecorder();
            recorder.recordApi(descriptor);

            if (throwable == null) {
                ActiveMQDestination destination = (ActiveMQDestination) args[0];
                // This annotation indicates the uri to which the call is made
                recorder.recordAttribute(AnnotationKey.MESSAGE_QUEUE_URI, destination.getQualifiedName());
                // DestinationId is used to render the virtual queue node.
                // We choose the queue/topic name as the logical name of the queue node.
                recorder.recordDestinationId(destination.getPhysicalName());

                ActiveMQSession session = ((ActiveMQSessionGetter) target)._$PINPOINT$_getActiveMQSession();
                ActiveMQConnection connection = session.getConnection();
                Transport transport = getRootTransport(((TransportGetter) connection)._$PINPOINT$_getTransport());

                String remoteAddress = transport.getRemoteAddress();
                // Producer's endPoint should be the socket address of where the producer is actually connected to.
                recorder.recordEndPoint(remoteAddress);
                recorder.recordAttribute(ActiveMQClientConstants.ACTIVEMQ_BROKER_URL, remoteAddress);
            } else {
                recorder.recordException(throwable);
            }
        } catch (Throwable t) {
            logger.warn("AFTER error. Cause:{}", t.getMessage(), t);
        } finally {
            trace.traceBlockEnd();
        }
    }

    private boolean filterDestination(ActiveMQDestination destination) {
        String destinationName = destination.getPhysicalName();
        return this.excludeDestinationFilter.filter(destinationName);
    }

    private boolean validate(Object target, Object[] args) {
        if (!(target instanceof ActiveMQMessageProducer)) {
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
        if (args == null || args.length < 2) {
            return false;
        }
        if (!(args[0] instanceof ActiveMQDestination)) {
            return false;
        }
        if (!(args[1] instanceof Message)) {
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
