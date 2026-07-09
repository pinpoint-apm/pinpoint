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
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.trace.attribute.AttributeValue;

import java.util.Map;
import java.util.Objects;

/**
 * A {@link MessagingConsumerHandler} bound with the shared collaborators needed to record a
 * CONSUMER span. It owns the recording flow that is identical across messaging systems
 * (broker/endpoint/acceptorHost and ServiceType resolution) and delegates the system-specific
 * parts — rpc string, entry-point name, and extra annotations — to its handler.
 *
 * <p>One instance is created per handler by {@link OtlpMessagingConsumerResolver} and cached in its
 * lookup map, so {@code resolve()} returns a recorder ready to apply.
 */
public class MessageConsumerRecorder {

    private final MessagingConsumerHandler handler;
    private final OtlpMessagingTypeResolver messagingTypeResolver;

    public MessageConsumerRecorder(MessagingConsumerHandler handler, OtlpMessagingTypeResolver messagingTypeResolver) {
        this.handler = Objects.requireNonNull(handler, "handler");
        this.messagingTypeResolver = Objects.requireNonNull(messagingTypeResolver, "messagingTypeResolver");
    }

    /**
     * The canonical {@code messaging.system} value this recorder serves (delegates to the handler).
     */
    public String system() {
        return handler.system();
    }

    /**
     * Records request-side fields and annotations for a CONSUMER span whose {@code messaging.system}
     * maps to a Pinpoint ServiceType. Mirrors the agent's
     * {@code ConsumerRecordEntryPointInterceptor.recordRootSpan}: acceptorHost is set even when the
     * span is a trace root, because every consumer has an upstream broker.
     */
    public void recordMessagingConsumer(SpanBo spanBo, Map<String, AttributeValue> attributes) {
        final String broker = MessagingAttributeUtils.getBrokerAddress(attributes);
        spanBo.setRpc(handler.buildConsumerRpc(attributes));
        spanBo.setEndPoint(MessagingAttributeUtils.resolveEndPoint(attributes));
        spanBo.setRemoteAddr(broker);

        final String acceptor = spanBo.getRemoteAddr() != null ? spanBo.getRemoteAddr() : spanBo.getEndPoint();
        if (acceptor != null) {
            spanBo.setAcceptorHost(acceptor);
        }

        spanBo.setServiceType(messagingTypeResolver.resolveClientServiceType(handler.system()));
        spanBo.addAnnotation(AnnotationBo.of(AnnotationKey.API.getCode(), handler.entryPointName()));
        handler.addAnnotations(attributes, spanBo::addAnnotation);
    }
}
