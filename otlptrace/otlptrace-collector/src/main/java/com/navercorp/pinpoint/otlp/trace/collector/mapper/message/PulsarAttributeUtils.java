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

import com.navercorp.pinpoint.common.server.bo.AnnotationBo;
import com.navercorp.pinpoint.common.trace.attribute.AttributeValue;
import com.navercorp.pinpoint.otlp.trace.collector.mapper.OtlpTraceConstants;

import java.util.Map;
import java.util.function.Consumer;

/**
 * Pulsar-specific helpers. Annotation codes mirror
 * {@code agent-module/plugins/pulsar PulsarMetadataProvider}, so OTel-sourced pulsar spans share
 * the same UI columns as agent-instrumented pulsar spans.
 *
 * <p>OTel mapping: {@code messaging.destination.name} → topic (898),
 * {@code messaging.destination.partition.id} → partition.index (896),
 * {@code messaging.message.id} → message.id (897),
 * {@code server.address[:server.port]} → broker.url (899).</p>
 */
public final class PulsarAttributeUtils {

    private PulsarAttributeUtils() {
    }

    public static void recordTopicPartitionMessage(Map<String, AttributeValue> attributes,
                                                   Consumer<AnnotationBo> sink) {
        final String topic = MessagingAttributeUtils.getDestinationName(attributes);
        if (topic != null) {
            sink.accept(AnnotationBo.of(OtlpTraceConstants.ANNOTATION_KEY_PULSAR_TOPIC, topic));
        }
        final Long partition = MessagingAttributeUtils.getPartition(attributes);
        if (partition != null) {
            sink.accept(AnnotationBo.of(OtlpTraceConstants.ANNOTATION_KEY_PULSAR_PARTITION_INDEX, partition.intValue()));
        }
        final String messageId = MessagingAttributeUtils.getMessageId(attributes);
        if (messageId != null) {
            sink.accept(AnnotationBo.of(OtlpTraceConstants.ANNOTATION_KEY_PULSAR_MESSAGE_ID, messageId));
        }
        final String broker = MessagingAttributeUtils.getBrokerAddress(attributes);
        if (broker != null) {
            sink.accept(AnnotationBo.of(OtlpTraceConstants.ANNOTATION_KEY_PULSAR_BROKER_URL, broker));
        }
    }

    /** {@code pulsar://topic=<destination>?partition=<id>&messageId=<id>} — destination drops to broker when missing. */
    public static String buildConsumerRpc(Map<String, AttributeValue> attributes) {
        final String topic = MessagingAttributeUtils.getDestinationName(attributes);
        final StringBuilder rpc = new StringBuilder("pulsar://").append("topic=").append(topic != null ? topic : "");
        final Long partition = MessagingAttributeUtils.getPartition(attributes);
        if (partition != null) {
            rpc.append("?partition=").append(partition);
        }
        final String messageId = MessagingAttributeUtils.getMessageId(attributes);
        if (messageId != null) {
            rpc.append(partition != null ? '&' : '?').append("messageId=").append(messageId);
        }
        return rpc.toString();
    }
}
