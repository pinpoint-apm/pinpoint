package com.navercorp.pinpoint.plugin.activemq.client.interceptor;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.context.TraceId;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.exception.PinpointException;
import com.navercorp.pinpoint.plugin.activemq.client.ActiveMQClientConstants;
import com.navercorp.pinpoint.plugin.activemq.client.ActiveMQClientHeader;
import com.navercorp.pinpoint.plugin.activemq.client.ActiveMQClientUtils;
import com.navercorp.pinpoint.plugin.activemq.client.field.getter.ActiveMQSessionGetter;
import com.navercorp.pinpoint.plugin.activemq.client.field.getter.SocketGetter;
import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQMessageProducer;
import org.apache.activemq.ActiveMQSession;
import org.apache.activemq.command.ActiveMQDestination;
import org.apache.activemq.command.ActiveMQMessage;
import org.apache.activemq.transport.Transport;
import org.apache.activemq.transport.TransportFilter;

import javax.jms.JMSException;
import javax.jms.Message;
import java.net.Socket;
import java.net.SocketAddress;

/**
 * @author HyunGil Jeong
 */
public class ActiveMQMessageProducerSendInterceptor implements AroundInterceptor {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final TraceContext traceContext;
    private final MethodDescriptor descriptor;

    public ActiveMQMessageProducerSendInterceptor(TraceContext traceContext, MethodDescriptor descriptor) {
        this.traceContext = traceContext;
        this.descriptor = descriptor;
    }

    @Override
    public void before(Object target, Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, args);
        }
        if (!validate(target, args)) {
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
                ActiveMQSession session = ((ActiveMQSessionGetter) target)._$PINPOINT$_getActiveMQSession();
                ActiveMQConnection connection = session.getConnection();
                Transport transport = getRootTransport(connection.getTransport());
                Socket socket = ((SocketGetter) transport)._$PINPOINT$_getSocket();
                SocketAddress remoteSocketAddress = socket.getRemoteSocketAddress();
                String remoteAddress = ActiveMQClientUtils.getEndPoint(remoteSocketAddress);
                // Producer's endPoint should be the socket address of where the producer is actually connected to.
                recorder.recordEndPoint(remoteAddress);
                recorder.recordAttribute(ActiveMQClientConstants.ACTIVEMQ_BROKER_URL, remoteAddress);

                ActiveMQDestination destination = (ActiveMQDestination) args[0];
                // This annotation indicates the uri to which the call is made
                recorder.recordAttribute(AnnotationKey.MESSAGE_QUEUE_URI, destination.getQualifiedName());
                // DestinationId is used to render the virtual queue node.
                // We choose the queue/topic name as the logical name of the queue node.
                recorder.recordDestinationId(destination.getPhysicalName());
            } else {
                recorder.recordException(throwable);
            }
        } catch (Throwable t) {
            logger.warn("AFTER error. Cause:{}", t.getMessage(), t);
        } finally {
            trace.traceBlockEnd();
        }
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
        if (connection == null) {
            return false;
        }
        Transport transport = getRootTransport(connection.getTransport());
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
        }
        return possiblyWrappedTransport;
    }

}