/*
 * Copyright 2026 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.otlp.trace.collector.mapper;

import com.navercorp.pinpoint.common.server.bo.AnnotationBo;
import com.navercorp.pinpoint.common.trace.attribute.AttributeValue;
import com.navercorp.pinpoint.otlp.trace.collector.util.AttributeUtils;

import java.util.Map;
import java.util.function.Consumer;

/**
 * RabbitMQ-specific helpers. Generic OTel messaging attribute access (system / destination /
 * broker address) lives in {@link MessagingAttributeUtils}.
 *
 * <p>Annotation codes emitted here match the codes registered by
 * {@code agent-module/plugins/rabbitmq RabbitMQClientTraceMetadataProvider}, so OTel-sourced
 * rabbitmq spans collapse onto the same UI columns as agent-instrumented rabbitmq spans.</p>
 *
 * <p>OTel mapping: {@code messaging.destination.name} → exchange (annotation 130),
 * {@code messaging.rabbitmq.destination.routing_key} → routing key (annotation 131).</p>
 */
public final class RabbitMQAttributeUtils {

    private RabbitMQAttributeUtils() {
    }

    /** {@code messaging.rabbitmq.destination.routing_key} — AMQP routing key used by the exchange. */
    public static String getRoutingKey(Map<String, AttributeValue> attributes) {
        return AttributeUtils.getAttributeStringValue(attributes,
                OtlpTraceConstants.ATTRIBUTE_KEY_MESSAGING_RABBITMQ_DESTINATION_ROUTING_KEY, null);
    }

    /**
     * Emits {@code rabbitmq.exchange} (130) and {@code rabbitmq.routingkey} (131) annotations to
     * {@code sink} for whichever values are present. Shared between consumer (SpanBo) and producer
     * (SpanEventBo) ─ both expose {@code addAnnotation(AnnotationBo)} as a method reference.
     */
    public static void recordExchangeRoutingKey(Map<String, AttributeValue> attributes,
                                                Consumer<AnnotationBo> sink) {
        final String exchange = MessagingAttributeUtils.getDestinationName(attributes);
        if (exchange != null) {
            sink.accept(AnnotationBo.of(OtlpTraceConstants.ANNOTATION_KEY_RABBITMQ_EXCHANGE, exchange));
        }
        final String routingKey = getRoutingKey(attributes);
        if (routingKey != null) {
            sink.accept(AnnotationBo.of(OtlpTraceConstants.ANNOTATION_KEY_RABBITMQ_ROUTING_KEY, routingKey));
        }
    }

    /**
     * Mirrors the {@link KafkaAttributeUtils#buildConsumerRpc} shape for rabbitmq:
     * {@code rabbitmq://exchange=<destination>?routingkey=<key>}. Either component is omitted
     * (left empty / suppressed) when the corresponding attribute is missing.
     */
    public static String buildConsumerRpc(Map<String, AttributeValue> attributes) {
        final String exchange = MessagingAttributeUtils.getDestinationName(attributes);
        final StringBuilder rpc = new StringBuilder("rabbitmq://").append("exchange=").append(exchange != null ? exchange : "");
        final String routingKey = getRoutingKey(attributes);
        if (routingKey != null) {
            rpc.append("?routingkey=").append(routingKey);
        }
        return rpc.toString();
    }
}
