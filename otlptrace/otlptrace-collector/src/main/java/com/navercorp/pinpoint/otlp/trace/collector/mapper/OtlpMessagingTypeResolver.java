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

import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.loader.service.ServiceTypeRegistryService;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Maps OTel {@code messaging.system} attribute values to Pinpoint queue {@code *_CLIENT}
 * ServiceType codes. The same code is applied to both producer SpanEvents and consumer
 * root Spans, mirroring the agent-side plugins where {@code KafkaConstants.KAFKA_CLIENT}
 * etc. (QUEUE property) are reused across the producer/consumer pair.
 *
 * <p>Codes are resolved from {@link ServiceTypeRegistryService} by ServiceType <em>name</em>
 * (e.g. {@code "KAFKA_CLIENT"}), so a plugin re-mapping its code does not desync this
 * resolver. If a messaging plugin is not deployed, its name is absent from the registry and
 * the resolver gracefully falls back to {@link ServiceType#OPENTELEMETRY_CLIENT}.
 * Resolution happens eagerly at construction so per-span lookups stay {@code O(1)}.</p>
 */
@Component
public class OtlpMessagingTypeResolver {

    // messaging.system value → registered ServiceType name. Names mirror the agent plugin
    // constants (KafkaConstants.KAFKA_CLIENT, RabbitMQClientConstants.RABBITMQ_CLIENT, ...)
    // and are registered on the collector via SPI TraceMetadataProvider / type-provider.yml.
    private static final Map<String, String> NAME_MAP = Map.of(
            OtlpTraceConstants.MESSAGING_SYSTEM_KAFKA,    "KAFKA_CLIENT",
            OtlpTraceConstants.MESSAGING_SYSTEM_RABBITMQ, "RABBITMQ_CLIENT",
            OtlpTraceConstants.MESSAGING_SYSTEM_PULSAR,   "PULSAR_CLIENT",
            OtlpTraceConstants.MESSAGING_SYSTEM_ROCKETMQ, "ROCKETMQ_CLIENT",
            OtlpTraceConstants.MESSAGING_SYSTEM_ACTIVEMQ, "ACTIVEMQ_CLIENT");

    private static final int DEFAULT_CLIENT = ServiceType.OPENTELEMETRY_CLIENT.getCode();

    // messaging.system → resolved code, eagerly populated at construction.
    private final Map<String, Integer> codeMap;

    public OtlpMessagingTypeResolver(ServiceTypeRegistryService registry) {
        Objects.requireNonNull(registry, "registry");
        Map<String, Integer> resolved = new HashMap<>();
        NAME_MAP.forEach((system, typeName) -> {
            ServiceType type = registry.findServiceTypeByName(typeName);
            int code = (type == null || type == ServiceType.UNDEFINED)
                    ? DEFAULT_CLIENT : type.getCode();
            resolved.put(system, code);
        });
        this.codeMap = Map.copyOf(resolved);
    }

    /**
     * Returns the Pinpoint queue ServiceType code for the given {@code messaging.system}
     * (case-insensitive), or {@link ServiceType#OPENTELEMETRY_CLIENT} when the system is
     * unknown / unsupported / its plugin is not deployed.
     */
    public int resolveClientServiceType(String messagingSystem) {
        if (messagingSystem == null) {
            return DEFAULT_CLIENT;
        }
        Integer code = codeMap.get(messagingSystem.toLowerCase());
        return code != null ? code : DEFAULT_CLIENT;
    }
}
