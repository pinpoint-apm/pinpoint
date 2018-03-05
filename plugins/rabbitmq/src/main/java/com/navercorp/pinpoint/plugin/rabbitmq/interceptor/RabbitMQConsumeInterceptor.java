package com.navercorp.pinpoint.plugin.rabbitmq.interceptor;

import com.navercorp.pinpoint.bootstrap.config.Filter;
import com.navercorp.pinpoint.bootstrap.context.*;
import com.navercorp.pinpoint.bootstrap.interceptor.SpanSimpleAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.util.NumberUtils;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.plugin.rabbitmq.RabbitMQClientPluginConfig;
import com.navercorp.pinpoint.plugin.rabbitmq.RabbitMQConstants;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

import java.util.Map;

/**
 * @author Jinkai.Ma
 * @author Jiaqi Feng
 */
public class RabbitMQConsumeInterceptor extends SpanSimpleAroundInterceptor {
    private final Filter<String> excludeExchangeFilter;

    public RabbitMQConsumeInterceptor(TraceContext traceContext, MethodDescriptor descriptor) {
        super(traceContext, descriptor, RabbitMQConsumeInterceptor.class);

        RabbitMQClientPluginConfig rabbitMQClientPluginConfig = new RabbitMQClientPluginConfig(traceContext.getProfilerConfig());
        this.excludeExchangeFilter = rabbitMQClientPluginConfig.getExcludeExchangeFilter();
    }

    @Override
    protected Trace createTrace(Object target, Object[] args) {
        Envelope envelope = (Envelope) args[1];
        String exchange = envelope.getExchange();

        if (RabbitMQClientPluginConfig.isExchangeExcluded(exchange, excludeExchangeFilter)) {
            if (isDebug) {
                logger.debug("exchange {} is excluded", exchange);
            }
            return null;
        }

        AMQP.BasicProperties properties = (AMQP.BasicProperties) args[2];
        if (properties == null) {
            return traceContext.newTraceObject();
        }

        Map<String, Object> headers = properties.getHeaders();
        if (headers == null) {
            return traceContext.newTraceObject();
        }

        // If this transaction is not traceable, mark as disabled.
        if (headers.get(RabbitMQConstants.META_DO_NOT_TRACE) != null) {
            return traceContext.disableSampling();
        }

        Object transactionId = headers.get(RabbitMQConstants.META_TRANSACTION_ID);
        // If there's no trasanction id, a new trasaction begins here.
        if (transactionId == null) {
            return traceContext.newTraceObject();
        }

        // otherwise, continue tracing with given data.
        long parentSpanID = NumberUtils.parseLong(headers.get(RabbitMQConstants.META_PARENT_SPAN_ID).toString(), SpanId.NULL);
        long spanID = NumberUtils.parseLong(headers.get(RabbitMQConstants.META_SPAN_ID).toString(), SpanId.NULL);
        short flags = NumberUtils.parseShort(headers.get(RabbitMQConstants.META_FLAGS).toString(), (short) 0);
        TraceId traceId = traceContext.createTraceId(transactionId.toString(), parentSpanID, spanID, flags);

        return traceContext.continueTraceObject(traceId);
    }


    @Override
    protected void doInBeforeTrace(SpanRecorder recorder, Object target, Object[] args) {
        AMQP.BasicProperties properties = (AMQP.BasicProperties) args[2];
        Map<String, Object> headers = properties.getHeaders();
        Envelope envelope = (Envelope) args[1];

        String exchange = envelope.getExchange();
        if (exchange == null || exchange.equals("")) {
            exchange = "unknown";
        }

        recorder.recordServiceType(RabbitMQConstants.RABBITMQ_SERVICE_TYPE);
        recorder.recordEndPoint("exchange:" + exchange);

        if (headers != null) {
            Object parentApplicationName = headers.get(RabbitMQConstants.META_PARENT_APPLICATION_NAME);
            Object parentApplicationType = headers.get(RabbitMQConstants.META_PARENT_APPLICATION_TYPE);
            if (parentApplicationName != null) {
                recorder.recordParentApplication(parentApplicationName.toString(), NumberUtils.parseShort(parentApplicationType.toString(), ServiceType.UNDEFINED.getCode()));
            }
        }
        recorder.recordRpcName("rabbitmq://exchange=" + exchange);
        recorder.recordAcceptorHost("exchange-" + exchange);
        if (isDebug)
            logger.debug("endPoint={}->{}", envelope.getExchange(), exchange);
    }

    @Override
    protected void doInAfterTrace(SpanRecorder recorder, Object target, Object[] args, Object result, Throwable throwable) {
        DefaultConsumer consumer = (DefaultConsumer) target;
        Connection connection = consumer.getChannel().getConnection();
        Envelope envelope = (Envelope) args[1];

        recorder.recordApi(methodDescriptor);
        recorder.recordAttribute(RabbitMQConstants.RABBITMQ_ROUTINGKEY_ANNOTATION_KEY, envelope.getRoutingKey());
        recorder.recordRemoteAddress(connection.getAddress().getHostAddress() + ":" + connection.getPort());

        if (throwable != null) {
            recorder.recordException(throwable);
        }
    }

}
