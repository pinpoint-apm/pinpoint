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

import java.util.Map;
import java.util.function.Consumer;

/**
 * RocketMQ-specific helpers. Annotation codes mirror
 * {@code agent-module/plugins/rocketmq RocketMQMetadataProvider}.
 *
 * <p>OTel mapping: {@code messaging.destination.name} → topic (800),
 * {@code messaging.destination.partition.id} → message.queue (801),
 * {@code server.address[:server.port]} → broker.server (805).</p>
 */
public final class RocketMQAttributeUtils {

    private RocketMQAttributeUtils() {
    }

    public static void recordTopicQueueBroker(Map<String, AttributeValue> attributes,
                                              Consumer<AnnotationBo> sink) {
        final String topic = MessagingAttributeUtils.getDestinationName(attributes);
        if (topic != null) {
            sink.accept(AnnotationBo.of(OtlpTraceConstants.ANNOTATION_KEY_ROCKETMQ_TOPIC, topic));
        }
        final Long queue = MessagingAttributeUtils.getPartition(attributes);
        if (queue != null) {
            sink.accept(AnnotationBo.of(OtlpTraceConstants.ANNOTATION_KEY_ROCKETMQ_MESSAGE_QUEUE, queue.intValue()));
        }
        final String broker = MessagingAttributeUtils.getBrokerAddress(attributes);
        if (broker != null) {
            sink.accept(AnnotationBo.of(OtlpTraceConstants.ANNOTATION_KEY_ROCKETMQ_BROKER_SERVER, broker));
        }
    }

    /** {@code rocketmq://topic=<destination>?queue=<id>}. */
    public static String buildConsumerRpc(Map<String, AttributeValue> attributes) {
        final String topic = MessagingAttributeUtils.getDestinationName(attributes);
        final StringBuilder rpc = new StringBuilder("rocketmq://").append("topic=").append(topic != null ? topic : "");
        final Long queue = MessagingAttributeUtils.getPartition(attributes);
        if (queue != null) {
            rpc.append("?queue=").append(queue);
        }
        return rpc.toString();
    }
}
