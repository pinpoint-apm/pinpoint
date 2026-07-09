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

package com.navercorp.pinpoint.otlp.trace.collector.mapper.message;

import com.navercorp.pinpoint.common.plugin.util.HostAndPort;
import com.navercorp.pinpoint.common.trace.attribute.AttributeValue;
import com.navercorp.pinpoint.otlp.trace.collector.mapper.OtlpTraceConstants;
import com.navercorp.pinpoint.otlp.trace.collector.util.AttributeUtils;

import java.util.Map;

/**
 * Generic accessors for OTel messaging semantic-convention attributes that apply to all
 * messaging systems (kafka, rabbitmq, rocketmq, sqs, …). Per-system specifics live in their
 * own utility class (e.g. {@link KafkaAttributeUtils}).
 *
 * <p>Spec reference: <a href="https://opentelemetry.io/docs/specs/semconv/messaging/">
 * OpenTelemetry messaging conventions</a>.</p>
 */
public final class MessagingAttributeUtils {

    private static final long PARTITION_UNSET = Long.MIN_VALUE;

    private MessagingAttributeUtils() {
    }

    /** {@code messaging.system} — kafka / rabbitmq / rocketmq / aws_sqs / ... */
    public static String getSystem(Map<String, AttributeValue> attributes) {
        return AttributeUtils.getAttributeStringValue(attributes,
                OtlpTraceConstants.ATTRIBUTE_KEY_MESSAGING_SYSTEM, null);
    }

    /** {@code messaging.destination.name} — topic / queue / exchange:routing_key. */
    public static String getDestinationName(Map<String, AttributeValue> attributes) {
        return AttributeUtils.getAttributeStringValue(attributes,
                OtlpTraceConstants.ATTRIBUTE_KEY_MESSAGING_DESTINATION_NAME, null);
    }

    /** {@code messaging.message.id} — system-assigned message identifier (pulsar/jms/activemq/…). */
    public static String getMessageId(Map<String, AttributeValue> attributes) {
        return AttributeUtils.getAttributeStringValue(attributes,
                OtlpTraceConstants.ATTRIBUTE_KEY_MESSAGING_MESSAGE_ID, null);
    }

    /**
     * Broker (server) endpoint reachable from the client:
     * {@code server.address}(:{@code server.port}) ▸ {@code network.peer.address}(:port).
     */
    public static String getBrokerAddress(Map<String, AttributeValue> attributes) {
        final String serverAddress = AttributeUtils.getAttributeStringValue(attributes,
                OtlpTraceConstants.ATTRIBUTE_KEY_SERVER_ADDRESS, null);
        if (serverAddress != null) {
            final long serverPort = AttributeUtils.getAttributeIntValue(attributes,
                    OtlpTraceConstants.ATTRIBUTE_KEY_SERVER_PORT, 0L);
            return HostAndPort.toHostAndPortString(serverAddress, (int) serverPort, 0);
        }
        final String networkPeerIp = AttributeUtils.getAttributeStringValue(attributes,
                OtlpTraceConstants.ATTRIBUTE_KEY_NETWORK_PEER_IP, null);
        if (networkPeerIp != null) {
            final long networkPeerPort = AttributeUtils.getAttributeIntValue(attributes,
                    OtlpTraceConstants.ATTRIBUTE_KEY_NETWORK_PEER_PORT, 0L);
            return HostAndPort.toHostAndPortString(networkPeerIp, (int) networkPeerPort, 0);
        }
        return null;
    }

    /**
     * Messaging endpoint with {@code messaging.client_id} fallback when no broker address
     * is available. Shared between producer (SpanEvent) and consumer (Span) paths so both
     * sides resolve the same value from the same OTel attributes.
     */
    public static String resolveEndPoint(Map<String, AttributeValue> attributes) {
        final String broker = getBrokerAddress(attributes);
        if (broker != null) {
            return broker;
        }
        return AttributeUtils.getAttributeStringValue(attributes,
                OtlpTraceConstants.ATTRIBUTE_KEY_MESSAGING_CLIENT_ID, null);
    }

    /**
     * Producer destinationId derived from OTel attributes: {@code messaging.destination.name}
     * (topic / exchange / queue, system-agnostic per OTel semconv) falling back to the resolved
     * endPoint when the destination attribute is absent.
     */
    public static String resolveProducerDestinationId(Map<String, AttributeValue> attributes,
                                                      String endPointFallback) {
        final String destinationName = getDestinationName(attributes);
        if (destinationName != null) {
            return destinationName;
        }
        return endPointFallback;
    }

    /**
     * Partition id. OTel SDKs emit either of:
     * <ul>
     *   <li>{@code messaging.kafka.destination.partition} (int_value, legacy kafka namespace)</li>
     *   <li>{@code messaging.destination.partition.id}    (string_value, generic semconv ≥ stable)</li>
     * </ul>
     * Returns the partition as {@link Long} or {@code null} when missing/unparsable.
     */
    public static Long getPartition(Map<String, AttributeValue> attributes) {
        final long intPartition = AttributeUtils.getAttributeIntValue(attributes,
                OtlpTraceConstants.ATTRIBUTE_KEY_MESSAGING_KAFKA_DESTINATION_PARTITION, PARTITION_UNSET);
        if (intPartition != PARTITION_UNSET) {
            return intPartition;
        }
        final String strPartition = AttributeUtils.getAttributeStringValue(attributes,
                OtlpTraceConstants.ATTRIBUTE_KEY_MESSAGING_DESTINATION_PARTITION_ID, null);
        if (strPartition != null) {
            try {
                return Long.parseLong(strPartition);
            } catch (NumberFormatException ignore) {
                return null;
            }
        }
        return null;
    }

    /**
     * Operation kind on a messaging span. Generic semconv ≥ stable uses
     * {@code messaging.operation.type}; legacy 1.x uses {@code messaging.operation}. Returns the
     * first match with the newer key taking precedence; {@code null} when missing.
     *
     * <p>Consumer-relevant values: {@code "receive"} (poll bookkeeping span) and
     * {@code "process"} (per-record processing — the actual unit of consumer work).</p>
     */
    public static String getOperationType(Map<String, AttributeValue> attributes) {
        final String generic = AttributeUtils.getAttributeStringValue(attributes,
                OtlpTraceConstants.ATTRIBUTE_KEY_MESSAGING_OPERATION_TYPE, null);
        if (generic != null) {
            return generic;
        }
        return AttributeUtils.getAttributeStringValue(attributes,
                OtlpTraceConstants.ATTRIBUTE_KEY_MESSAGING_OPERATION, null);
    }

    /**
     * Consumer group name. Generic semconv ≥ stable uses {@code messaging.consumer.group.name};
     * legacy java agent emits {@code messaging.kafka.consumer.group}. Returns the first match
     * with the newer key taking precedence.
     */
    public static String getConsumerGroup(Map<String, AttributeValue> attributes) {
        final String generic = AttributeUtils.getAttributeStringValue(attributes,
                OtlpTraceConstants.ATTRIBUTE_KEY_MESSAGING_CONSUMER_GROUP_NAME, null);
        if (generic != null) {
            return generic;
        }
        return AttributeUtils.getAttributeStringValue(attributes,
                OtlpTraceConstants.ATTRIBUTE_KEY_MESSAGING_KAFKA_CONSUMER_GROUP, null);
    }
}
