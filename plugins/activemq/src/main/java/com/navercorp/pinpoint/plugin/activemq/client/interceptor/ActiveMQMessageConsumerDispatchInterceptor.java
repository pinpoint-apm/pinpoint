package com.navercorp.pinpoint.plugin.activemq.client.interceptor;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanId;
import com.navercorp.pinpoint.bootstrap.context.SpanRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.context.TraceId;
import com.navercorp.pinpoint.bootstrap.interceptor.SpanSimpleAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.annotation.Scope;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.plugin.activemq.client.ActiveMQClientConstants;
import com.navercorp.pinpoint.plugin.activemq.client.ActiveMQClientHeader;
import com.navercorp.pinpoint.plugin.activemq.client.ActiveMQClientUtils;
import com.navercorp.pinpoint.plugin.activemq.client.descriptor.ActiveMQConsumerEntryMethodDescriptor;
import com.navercorp.pinpoint.plugin.activemq.client.field.getter.ActiveMQSessionGetter;
import com.navercorp.pinpoint.plugin.activemq.client.field.getter.SocketGetter;
import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQMessageConsumer;
import org.apache.activemq.ActiveMQSession;
import org.apache.activemq.command.ActiveMQDestination;
import org.apache.activemq.command.ActiveMQMessage;
import org.apache.activemq.command.Message;
import org.apache.activemq.command.MessageDispatch;
import org.apache.activemq.transport.Transport;
import org.apache.activemq.transport.TransportFilter;

import java.net.Socket;
import java.net.SocketAddress;

/**
 * @author HyunGil Jeong
 */
@Scope(value = ActiveMQClientConstants.ACTIVEMQ_CLIENT_SCOPE)
public class ActiveMQMessageConsumerDispatchInterceptor extends SpanSimpleAroundInterceptor {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    public ActiveMQMessageConsumerDispatchInterceptor(TraceContext traceContext) {
        this(traceContext, new ActiveMQConsumerEntryMethodDescriptor());
    }

    private ActiveMQMessageConsumerDispatchInterceptor(TraceContext traceContext, MethodDescriptor methodDescriptor) {
        super(traceContext, methodDescriptor, ActiveMQMessageConsumerDispatchInterceptor.class);
        traceContext.cacheApi(methodDescriptor);
    }

    @Override
    protected Trace createTrace(Object target, Object[] args) {
        if (!validate(target, args)) {
            return null;
        }
        MessageDispatch md = (MessageDispatch) args[0];
        ActiveMQMessage message = (ActiveMQMessage) md.getMessage();
        // These might trigger unmarshalling.
        if (!ActiveMQClientHeader.getSampled(message, true)) {
            return traceContext.disableSampling();
        }
        String transactionId = ActiveMQClientHeader.getTraceId(message, null);
        if (transactionId != null) {
            long parentSpanId = ActiveMQClientHeader.getParentSpanId(message, SpanId.NULL);
            long spanId = ActiveMQClientHeader.getSpanId(message, SpanId.NULL);
            short flags = ActiveMQClientHeader.getFlags(message, (short) 0);
            final TraceId traceId = traceContext.createTraceId(transactionId, parentSpanId, spanId, flags);
            return traceContext.continueTraceObject(traceId);
        } else {
            return traceContext.newTraceObject();
        }
    }

    @Override
    protected void doInBeforeTrace(SpanRecorder recorder, Object target, Object[] args) {
        recorder.recordServiceType(ActiveMQClientConstants.ACTIVEMQ_CLIENT);

        ActiveMQSession session = ((ActiveMQSessionGetter) target)._$PINPOINT$_getActiveMQSession();
        ActiveMQConnection connection = session.getConnection();
        Transport transport = getRootTransport(connection.getTransport());
        Socket socket = ((SocketGetter) transport)._$PINPOINT$_getSocket();

        SocketAddress localSocketAddress = socket.getLocalSocketAddress();
        String endPoint = ActiveMQClientUtils.getEndPoint(localSocketAddress);
        // Endpoint should be the local socket address of the consumer.
        recorder.recordEndPoint(endPoint);

        SocketAddress remoteSocketAddress = socket.getRemoteSocketAddress();
        String remoteAddress = ActiveMQClientUtils.getEndPoint(remoteSocketAddress);
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

    @Override
    protected void doInAfterTrace(SpanRecorder recorder, Object target, Object[] args, Object result, Throwable throwable) {
        recorder.recordApi(methodDescriptor);
        if (throwable != null) {
            recorder.recordException(throwable);
        }
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
