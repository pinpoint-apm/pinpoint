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
 * Kafka-specific helpers. Generic OTel messaging attribute access (system / destination /
 * partition / consumer group / broker address) lives in {@link MessagingAttributeUtils}.
 *
 * <p>Annotation codes emitted here match the codes registered by
 * {@code agent-module/plugins/kafka KafkaMetadataProvider}, so OTel-sourced kafka spans
 * collapse onto the same UI columns as agent-instrumented kafka spans.</p>
 */
public final class KafkaAttributeUtils {

    public static final long OFFSET_UNSET = -1L;

    private KafkaAttributeUtils() {
    }

    /**
     * Returns the kafka message offset, or {@link #OFFSET_UNSET} if missing.
     * Reads the OTel semconv ≥ stable {@code messaging.kafka.offset} first, falling back
     * to legacy {@code messaging.kafka.message.offset}.
     *
     * <p>Negative offsets (e.g. {@code -1001} for uncommitted) are treated as missing by
     * callers via {@code offset >= 0} gating.</p>
     */
    public static long getOffset(Map<String, AttributeValue> attributes) {
        final long current = AttributeUtils.getAttributeIntValue(attributes,
                OtlpTraceConstants.ATTRIBUTE_KEY_MESSAGING_KAFKA_OFFSET, OFFSET_UNSET);
        if (current != OFFSET_UNSET) {
            return current;
        }
        return AttributeUtils.getAttributeIntValue(attributes,
                OtlpTraceConstants.ATTRIBUTE_KEY_MESSAGING_KAFKA_MESSAGE_OFFSET, OFFSET_UNSET);
    }

    /**
     * Emits {@code kafka.topic} (140), {@code kafka.partition} (141), and {@code kafka.offset} (142)
     * annotations to {@code sink} for whichever values are present. Shared between consumer (SpanBo)
     * and producer (SpanEventBo) ─ both expose {@code addAnnotation(AnnotationBo)} as a method reference.
     */
    public static void recordTopicPartitionOffset(Map<String, AttributeValue> attributes,
                                                  Consumer<AnnotationBo> sink) {
        final String topic = MessagingAttributeUtils.getDestinationName(attributes);
        if (topic != null) {
            sink.accept(AnnotationBo.of(OtlpTraceConstants.ANNOTATION_KEY_KAFKA_TOPIC, topic));
        }
        final Long partition = MessagingAttributeUtils.getPartition(attributes);
        if (partition != null) {
            sink.accept(AnnotationBo.of(OtlpTraceConstants.ANNOTATION_KEY_KAFKA_PARTITION, partition.intValue()));
        }
        final long offset = getOffset(attributes);
        if (offset >= 0) {
            sink.accept(AnnotationBo.of(OtlpTraceConstants.ANNOTATION_KEY_KAFKA_OFFSET, offset));
        }
    }

    /**
     * Matches {@code agent-module/plugins/kafka} {@code ConsumerRecordEntryPointInterceptor.createRpcName}:
     * {@code kafka://topic=<destination>?partition=<id>&offset=<n>}.
     */
    public static String buildConsumerRpc(Map<String, AttributeValue> attributes) {
        final String topic = MessagingAttributeUtils.getDestinationName(attributes);
        final StringBuilder rpc = new StringBuilder("kafka://").append("topic=").append(topic != null ? topic : "");
        final Long partition = MessagingAttributeUtils.getPartition(attributes);
        if (partition != null) {
            rpc.append("?partition=").append(partition);
        }
        final long offset = getOffset(attributes);
        if (offset >= 0) {
            rpc.append(partition != null ? '&' : '?').append("offset=").append(offset);
        }
        return rpc.toString();
    }
}
