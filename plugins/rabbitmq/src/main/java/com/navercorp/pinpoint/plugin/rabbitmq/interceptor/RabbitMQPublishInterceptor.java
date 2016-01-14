package com.navercorp.pinpoint.plugin.rabbitmq.interceptor;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.annotation.Scope;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.plugin.rabbitmq.RabbitMQConstants;
import com.rabbitmq.client.AMQP;

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

//        AMQP.BasicProperties properties = (AMQP.BasicProperties) args[4];
//        Map<String, Object> headers = new HashMap<String, Object>();
//        for (String key : properties.getHeaders().keySet()) {
//            headers.put(key, properties.getHeaders().get(key));
//        }

        if (trace.canSampled()) {
            SpanEventRecorder recorder = trace.traceBlockBegin();
            recorder.recordServiceType(RabbitMQConstants.RABBITMQ_SERVICE_TYPE);

//            TraceId nextId = trace.getTraceId().getNextTraceId();
//
//            recorder.recordNextSpanId(nextId.getSpanId());
//
//            headers.put(RabbitMQConstants.META_TRANSACTION_ID, nextId.getTransactionId());
//            headers.put(RabbitMQConstants.META_SPAN_ID, Long.toString(nextId.getSpanId()));
//            headers.put(RabbitMQConstants.META_PARENT_SPAN_ID, Long.toString(nextId.getParentSpanId()));
//            headers.put(RabbitMQConstants.META_PARENT_APPLICATION_TYPE, Short.toString(RabbitMQConstants.RABBITMQ_SERVICE_TYPE.getCode()));
//            headers.put(RabbitMQConstants.META_PARENT_APPLICATION_NAME, traceContext.getApplicationName());
//            headers.put(RabbitMQConstants.META_FLAGS, Short.toString(nextId.getFlags()));
//        } else {
//            headers.put(RabbitMQConstants.META_DO_NOT_TRACE, "1");
        }

//        ((HeadersFieldSetter) properties)._$PINPOINT$_setHeaders(headers);
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            logger.afterInterceptor(target, args);
        }
        Trace trace = traceContext.currentTraceObject();
        if (trace == null || !trace.canSampled()) {
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
                recorder.recordEndPoint(exchange);

                recorder.recordDestinationId(exchange);
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
