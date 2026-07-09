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

import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * Resolves a CONSUMER span's {@code messaging.system} to a ready-to-apply {@link
 * MessageConsumerRecorder}. Each registered {@link MessagingConsumerHandler} is wrapped with the
 * shared {@link OtlpMessagingTypeResolver} into a recorder at construction and looked up by {@link
 * MessagingConsumerHandler#system()}; a {@code null} result means the system is unsupported
 * (Pinpoint has no ServiceType for it) and the span should be mapped as a plain server span.
 */
@Component
public class OtlpMessagingConsumerResolver {

    // messaging.system (lower-cased) → recorder, eagerly populated at construction for O(1) lookup.
    private final Map<String, MessageConsumerRecorder> recorderMap;

    public OtlpMessagingConsumerResolver(List<MessagingConsumerHandler> handlers,
                                         OtlpMessagingTypeResolver messagingTypeResolver) {
        Objects.requireNonNull(handlers, "handlers");
        Objects.requireNonNull(messagingTypeResolver, "messagingTypeResolver");
        Map<String, MessageConsumerRecorder> map = new HashMap<>();
        for (MessagingConsumerHandler handler : handlers) {
            final String system = Objects.requireNonNull(handler.system(), "handler.system()").toLowerCase(Locale.ROOT);
            final MessageConsumerRecorder prev = map.put(system, new MessageConsumerRecorder(handler, messagingTypeResolver));
            if (prev != null) {
                throw new IllegalStateException("Duplicate MessagingConsumerHandler for messaging.system=" + system
                        + ": " + prev.system() + " vs " + handler.getClass().getName());
            }
        }
        this.recorderMap = Map.copyOf(map);
    }

    /**
     * Returns the recorder for the given {@code messaging.system} (case-insensitive), or
     * {@code null} when the system is {@code null} / unknown / unsupported.
     */
    public @Nullable MessageConsumerRecorder resolve(String messagingSystem) {
        if (messagingSystem == null) {
            return null;
        }
        return recorderMap.get(messagingSystem.toLowerCase(Locale.ROOT));
    }
}
