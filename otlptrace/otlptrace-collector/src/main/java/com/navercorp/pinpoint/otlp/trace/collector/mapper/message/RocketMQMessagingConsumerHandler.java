/*
 * Copyright 2025 NAVER Corp.
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
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.function.Consumer;

@Component
public class RocketMQMessagingConsumerHandler implements MessagingConsumerHandler {

    @Override
    public String system() {
        return OtlpTraceConstants.MESSAGING_SYSTEM_ROCKETMQ;
    }

    @Override
    public String buildConsumerRpc(Map<String, AttributeValue> attributes) {
        return RocketMQAttributeUtils.buildConsumerRpc(attributes);
    }

    @Override
    public String entryPointName() {
        return "RocketMQ Consumer Invocation";
    }

    @Override
    public void addAnnotations(Map<String, AttributeValue> attributes, Consumer<AnnotationBo> sink) {
        RocketMQAttributeUtils.recordTopicQueueBroker(attributes, sink);
    }
}
