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

import java.util.Map;
import java.util.function.Consumer;

/**
 * Per-{@code messaging.system} strategy supplying the system-specific parts of a CONSUMER span.
 * One implementation per messaging system Pinpoint has a ServiceType for; {@link
 * OtlpMessagingConsumerResolver} selects the matching handler by {@link #system()} and {@link
 * MessageConsumerRecorder} applies the shared recording flow around it.
 */
public interface MessagingConsumerHandler {

    /**
     * The canonical {@code messaging.system} value this handler serves (e.g. {@code "kafka"}),
     * matching the {@code OtlpTraceConstants.MESSAGING_SYSTEM_*} constants.
     */
    String system();

    /**
     * System-specific consumer rpc string, or {@code null} when it cannot be derived.
     */
    String buildConsumerRpc(Map<String, AttributeValue> attributes);

    /**
     * Entry-point display name shown on the Call Tree. Mirrors the agent-side
     * {@code *EntryMethodDescriptor.getApiDescriptor()} value so OTel-sourced consumer spans
     * render identically to agent-instrumented ones.
     */
    String entryPointName();

    /**
     * Emits system-specific annotations for the consumer span (topic/partition/queue etc.).
     */
    void addAnnotations(Map<String, AttributeValue> attributes, Consumer<AnnotationBo> sink);
}
