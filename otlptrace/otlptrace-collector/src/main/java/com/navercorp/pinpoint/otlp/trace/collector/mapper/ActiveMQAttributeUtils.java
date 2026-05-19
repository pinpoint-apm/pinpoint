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
 * ActiveMQ-specific helpers. Annotation codes mirror
 * {@code agent-module/plugins/activemq-client ActiveMQClientConstants} (plus the built-in
 * {@link com.navercorp.pinpoint.common.trace.AnnotationKey#MESSAGE_QUEUE_URI} for the queue name).
 *
 * <p>OTel mapping: {@code messaging.destination.name} → message.queue.url (100),
 * {@code server.address[:server.port]} → activemq.broker.address (101).</p>
 */
public final class ActiveMQAttributeUtils {

    private ActiveMQAttributeUtils() {
    }

    public static void recordQueueBroker(Map<String, AttributeValue> attributes,
                                         Consumer<AnnotationBo> sink) {
        final String queue = MessagingAttributeUtils.getDestinationName(attributes);
        if (queue != null) {
            sink.accept(AnnotationBo.of(OtlpTraceConstants.ANNOTATION_KEY_MESSAGE_QUEUE_URI, queue));
        }
        final String broker = MessagingAttributeUtils.getBrokerAddress(attributes);
        if (broker != null) {
            sink.accept(AnnotationBo.of(OtlpTraceConstants.ANNOTATION_KEY_ACTIVEMQ_BROKER_ADDRESS, broker));
        }
    }

    /** {@code activemq://queue=<destination>}. */
    public static String buildConsumerRpc(Map<String, AttributeValue> attributes) {
        final String queue = MessagingAttributeUtils.getDestinationName(attributes);
        return "activemq://queue=" + (queue != null ? queue : "");
    }
}
