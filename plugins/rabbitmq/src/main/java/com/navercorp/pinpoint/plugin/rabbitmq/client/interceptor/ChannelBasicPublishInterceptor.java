package com.navercorp.pinpoint.plugin.rabbitmq.client.interceptor;

import com.navercorp.pinpoint.bootstrap.config.Filter;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.context.TraceId;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.InterceptorScope;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.InterceptorScopeInvocation;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.plugin.rabbitmq.client.RabbitMQClientConstants;
import com.navercorp.pinpoint.plugin.rabbitmq.client.RabbitMQClientPluginConfig;
import com.navercorp.pinpoint.plugin.rabbitmq.client.field.accessor.RemoteAddressAccessor;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.impl.AMQConnection;
import com.rabbitmq.client.impl.FrameHandler;

/**
 * @author Jinkai.Ma
 * @author Jiaqi Feng
 * @author HyunGil Jeong
 */
public class ChannelBasicPublishInterceptor implements AroundInterceptor {

    private final PLogger logger = PLoggerFactory.getLogger(getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final MethodDescriptor descriptor;
    private final TraceContext traceContext;
    private final InterceptorScope scope;
    private final Filter<String> excludeExchangeFilter;

    public ChannelBasicPublishInterceptor(TraceContext traceContext, MethodDescriptor descriptor, InterceptorScope scope) {
        this.descriptor = descriptor;
        this.traceContext = traceContext;
        this.scope = scope;

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

        final Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }

        SpanEventRecorder recorder = trace.traceBlockBegin();
        recorder.recordServiceType(RabbitMQClientConstants.RABBITMQ_CLIENT);

        TraceId nextId = trace.getTraceId().getNextTraceId();

        recorder.recordNextSpanId(nextId.getSpanId());

        InterceptorScopeInvocation invocation = scope.getCurrentInvocation();
        invocation.setAttachment(nextId);
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            logger.afterInterceptor(target, args);
        }

        if (!validate(target, args)) {
            return;
        }

        final Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }

        try {
            String exchange = (String) args[0];
            String routingKey = (String) args[1];

            if (exchange == null || exchange.equals("")) {
                exchange = RabbitMQClientConstants.UNKNOWN;
            }

            SpanEventRecorder recorder = trace.currentSpanEventRecorder();
            recorder.recordApi(descriptor);

            String endPoint = RabbitMQClientConstants.UNKNOWN;
            // Producer's endPoint should be the socket address of where the producer is actually connected to.
            final Connection connection = ((Channel) target).getConnection();
            if (connection instanceof AMQConnection) {
                AMQConnection amqConnection = (AMQConnection) connection;
                FrameHandler frameHandler = amqConnection.getFrameHandler();
                if (frameHandler instanceof RemoteAddressAccessor) {
                    endPoint = ((RemoteAddressAccessor) frameHandler)._$PINPOINT$_getRemoteAddress();
                }
            }
            recorder.recordEndPoint(endPoint);
            // DestinationId is used to render the virtual queue node.
            // We choose the exchange name as the logical name of the queue node.
            recorder.recordDestinationId("exchange-" + exchange);

            recorder.recordAttribute(RabbitMQClientConstants.RABBITMQ_EXCHANGE_ANNOTATION_KEY, exchange);
            recorder.recordAttribute(RabbitMQClientConstants.RABBITMQ_ROUTINGKEY_ANNOTATION_KEY, routingKey);

            recorder.recordException(throwable);

        } finally {
            trace.traceBlockEnd();
        }
    }

    private boolean validate(Object target, Object[] args) {
        if (!(target instanceof Channel)) {
            return false;
        }
        if (args == null || args.length < 6) {
            return false;
        }
        if (args[0] != null && !(args[0] instanceof String)) {
            return false;
        }
        if (args[1] != null && !(args[1] instanceof String)) {
            return false;
        }
        if (args[4] != null && !(args[4] instanceof AMQP.BasicProperties)) {
            return false;
        }
        return true;
    }
}
