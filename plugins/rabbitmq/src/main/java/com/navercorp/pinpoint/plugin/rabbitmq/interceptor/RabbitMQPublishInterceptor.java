package com.navercorp.pinpoint.plugin.rabbitmq.interceptor;

import com.navercorp.pinpoint.bootstrap.context.*;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.annotation.Scope;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.plugin.rabbitmq.RabbitMQConstants;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

/**
 * @author Jinkai.Ma
 */
@Scope(value = RabbitMQConstants.RABBITMQ_SCOPE)
public class RabbitMQPublishInterceptor implements AroundInterceptor {

    private final PLogger logger = PLoggerFactory.getLogger(getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final MethodDescriptor descriptor;
    private final TraceContext traceContext;

    public RabbitMQPublishInterceptor(TraceContext traceContext, MethodDescriptor descriptor) {
        this.descriptor = descriptor;
        this.traceContext = traceContext;
    }

    @Override
    public void before(Object target, Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, args);
        }
        Trace trace = traceContext.currentTraceObject();

        if (trace == null) {
            return;
        }

        SpanEventRecorder recorder = trace.traceBlockBegin();
        recorder.recordServiceType(RabbitMQConstants.RABBITMQ_SERVICE_TYPE);
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            logger.afterInterceptor(target, args);
        }
        Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }

        try {
            String exchange = (String) args[0];
            String routingKey = (String) args[1];
            AMQP.BasicProperties properties = (AMQP.BasicProperties) args[4];
            byte[] body = (byte[]) args[5];

            SpanEventRecorder recorder = trace.currentSpanEventRecorder();
            recorder.recordApi(descriptor);
            if (throwable == null) {
                Channel channel = (Channel) target;
                Connection connection = channel.getConnection();
                String endPoint = connection.getAddress().getHostAddress() + ":" + connection.getPort();

                recorder.recordEndPoint(endPoint);

                recorder.recordDestinationId(endPoint);
                recorder.recordAttribute(RabbitMQConstants.RABBITMQ_EXCHANGE_ANNOTATION_KEY, exchange);
                recorder.recordAttribute(RabbitMQConstants.RABBITMQ_ROUTINGKEY_ANNOTATION_KEY, routingKey);
                recorder.recordAttribute(RabbitMQConstants.RABBITMQ_PROPERTIES_ANNOTATION_KEY, properties);
                recorder.recordAttribute(RabbitMQConstants.RABBITMQ_BODY_ANNOTATION_KEY, body);
            } else {
                recorder.recordException(throwable);
            }
        } finally {
            trace.traceBlockEnd();
        }
    }
}
