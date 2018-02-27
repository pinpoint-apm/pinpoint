/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.rabbitmq.client.interceptor;

import com.navercorp.pinpoint.bootstrap.config.Filter;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanId;
import com.navercorp.pinpoint.bootstrap.context.SpanRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.context.TraceId;
import com.navercorp.pinpoint.bootstrap.interceptor.SpanSimpleAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.util.NumberUtils;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.plugin.rabbitmq.client.RabbitMQClientConstants;
import com.navercorp.pinpoint.plugin.rabbitmq.client.RabbitMQClientPluginConfig;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

import java.util.Map;

/**
 * @author Jinkai.Ma
 * @author Jiaqi Feng
 */
public abstract class RabbitMQConsumeInterceptor extends SpanSimpleAroundInterceptor {
    private final Filter<String> excludeExchangeFilter;

    protected RabbitMQConsumeInterceptor(TraceContext traceContext, MethodDescriptor methodDescriptor, Class<? extends RabbitMQConsumeInterceptor> childClazz) {
        super(traceContext, methodDescriptor, childClazz);

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
        if (headers.get(RabbitMQClientConstants.META_SAMPLED) != null) {
            return traceContext.disableSampling();
        }

        Object transactionId = headers.get(RabbitMQClientConstants.META_TRACE_ID);
        // If there's no trasanction id, a new trasaction begins here.
        if (transactionId == null) {
            return traceContext.newTraceObject();
        }

        // otherwise, continue tracing with given data.
        long parentSpanId = NumberUtils.parseLong(headers.get(RabbitMQClientConstants.META_PARENT_SPAN_ID).toString(), SpanId.NULL);
        long spanId = NumberUtils.parseLong(headers.get(RabbitMQClientConstants.META_SPAN_ID).toString(), SpanId.NULL);
        short flags = NumberUtils.parseShort(headers.get(RabbitMQClientConstants.META_FLAGS).toString(), (short) 0);
        TraceId traceId = traceContext.createTraceId(transactionId.toString(), parentSpanId, spanId, flags);

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

        recorder.recordServiceType(RabbitMQClientConstants.RABBITMQ_CLIENT);
        recorder.recordEndPoint("exchange:" + exchange);

        if (headers != null) {
            Object parentApplicationName = headers.get(RabbitMQClientConstants.META_PARENT_APPLICATION_NAME);
            Object parentApplicationType = headers.get(RabbitMQClientConstants.META_PARENT_APPLICATION_TYPE);
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
        recorder.recordAttribute(RabbitMQClientConstants.RABBITMQ_ROUTINGKEY_ANNOTATION_KEY, envelope.getRoutingKey());
        recorder.recordRemoteAddress(connection.getAddress().getHostAddress() + ":" + connection.getPort());

        if (throwable != null) {
            recorder.recordException(throwable);
        }
    }
}
