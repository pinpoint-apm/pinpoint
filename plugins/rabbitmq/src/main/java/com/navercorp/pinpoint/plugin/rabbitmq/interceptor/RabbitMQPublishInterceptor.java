package com.navercorp.pinpoint.plugin.rabbitmq.interceptor;

import com.navercorp.pinpoint.bootstrap.config.Filter;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.context.TraceId;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.annotation.Scope;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.plugin.rabbitmq.RabbitMQClientPluginConfig;
import com.navercorp.pinpoint.plugin.rabbitmq.RabbitMQConstants;
import com.navercorp.pinpoint.plugin.rabbitmq.field.setter.HeadersFieldSetter;
import com.rabbitmq.client.AMQP;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Jinkai.Ma
 * @author Jiaqi Feng
 */
public class RabbitMQPublishInterceptor implements AroundInterceptor {

    private final PLogger logger = PLoggerFactory.getLogger(getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final MethodDescriptor descriptor;
    private final TraceContext traceContext;
    private final Filter<String> excludeExchangeFilter;

    public RabbitMQPublishInterceptor(TraceContext traceContext, MethodDescriptor descriptor) {
        this.descriptor = descriptor;
        this.traceContext = traceContext;

        RabbitMQClientPluginConfig rabbitMQClientPluginConfig = new RabbitMQClientPluginConfig(traceContext.getProfilerConfig());
        this.excludeExchangeFilter = rabbitMQClientPluginConfig.getExcludeExchangeFilter();
    }

    @Override
    public void before(Object target, Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, args);
        }

        if (!validate(target, args)) {
            if (isDebug)
                logger.debug("validate argument failed!");
            return;
        }

        String exchange = (String) args[0];
        if (RabbitMQClientPluginConfig.isExchangeExcluded(exchange, excludeExchangeFilter)) {
            return;
        }

        Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }
        AMQP.BasicProperties properties = (AMQP.BasicProperties) args[4];
        Map<String, Object> headers = new HashMap<String, Object>();
        if (properties != null && properties.getHeaders() != null && properties.getHeaders().keySet() != null) {
            for (String key : properties.getHeaders().keySet()) {
                headers.put(key, properties.getHeaders().get(key));
            }
        }
        if (trace.canSampled()) {
            SpanEventRecorder recorder = trace.traceBlockBegin();
            recorder.recordServiceType(RabbitMQConstants.RABBITMQ_SERVICE_TYPE);

            TraceId nextId = trace.getTraceId().getNextTraceId();

            recorder.recordNextSpanId(nextId.getSpanId());

            headers.put(RabbitMQConstants.META_TRANSACTION_ID, nextId.getTransactionId());
            headers.put(RabbitMQConstants.META_SPAN_ID, Long.toString(nextId.getSpanId()));
            headers.put(RabbitMQConstants.META_PARENT_SPAN_ID, Long.toString(nextId.getParentSpanId()));
            headers.put(RabbitMQConstants.META_PARENT_APPLICATION_TYPE, Short.toString(traceContext.getServerTypeCode()));
            headers.put(RabbitMQConstants.META_PARENT_APPLICATION_NAME, traceContext.getApplicationName());
            headers.put(RabbitMQConstants.META_FLAGS, Short.toString(nextId.getFlags()));
        } else {
            headers.put(RabbitMQConstants.META_DO_NOT_TRACE, "1");
        }

        ((HeadersFieldSetter) properties)._$PINPOINT$_setHeaders(headers);
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
        if (trace == null || !trace.canSampled()) {
            return;
        }

        try {
            String exchange = (String) args[0];
            String routingKey = (String) args[1];
            AMQP.BasicProperties properties = (AMQP.BasicProperties) args[4];
            byte[] body = (byte[]) args[5];

            if (exchange == null || exchange.equals("")) exchange = "unknown";

            SpanEventRecorder recorder = trace.currentSpanEventRecorder();
            recorder.recordApi(descriptor);
            if (throwable == null) {
                recorder.recordEndPoint("exchange:"+exchange);
                recorder.recordDestinationId("exchange-" + exchange);

                recorder.recordAttribute(RabbitMQConstants.RABBITMQ_EXCHANGE_ANNOTATION_KEY, exchange);
                recorder.recordAttribute(RabbitMQConstants.RABBITMQ_ROUTINGKEY_ANNOTATION_KEY, routingKey);
            } else {
                recorder.recordException(throwable);
            }
        } finally {
            trace.traceBlockEnd();
        }
    }

    private boolean validate(Object target, Object[] args) {
        if (!(target instanceof com.rabbitmq.client.Channel)) {
            return false;
        }
        if (args == null || args.length < 6) {
            return false;
        }
        if (!(args[0] instanceof String)) {
            return false;
        }
        if (!(args[1] instanceof String)) {
            return false;
        }
        if (!(args[4] instanceof AMQP.BasicProperties)) {
            return false;
        }
        if (!(args[5] instanceof byte[])) {
            return false;
        }
        return true;
    }
}
