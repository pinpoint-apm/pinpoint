package com.navercorp.pinpoint.otlp.trace.collector.mapper.message;

import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.trace.ServiceTypeFactory;
import com.navercorp.pinpoint.loader.service.ServiceTypeRegistryService;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class OtlpMessagingTypeResolverTest {

    private static final ServiceTypeRegistryService FULL_REGISTRY = buildRegistry(Map.of(
            "KAFKA_CLIENT",    (short) 8660,
            "RABBITMQ_CLIENT", (short) 8300,
            "PULSAR_CLIENT",   (short) 8670,
            "ROCKETMQ_CLIENT", (short) 8400,
            "ACTIVEMQ_CLIENT", (short) 8310));

    private final OtlpMessagingTypeResolver resolver = new OtlpMessagingTypeResolver(FULL_REGISTRY);

    @Test
    void resolve_kafka() {
        assertThat(resolver.resolveClientServiceType("kafka")).isEqualTo(8660);
    }

    @Test
    void resolve_rabbitmq() {
        assertThat(resolver.resolveClientServiceType("rabbitmq")).isEqualTo(8300);
    }

    @Test
    void resolve_pulsar() {
        assertThat(resolver.resolveClientServiceType("pulsar")).isEqualTo(8670);
    }

    @Test
    void resolve_rocketmq() {
        assertThat(resolver.resolveClientServiceType("rocketmq")).isEqualTo(8400);
    }

    @Test
    void resolve_activemq() {
        assertThat(resolver.resolveClientServiceType("activemq")).isEqualTo(8310);
    }

    @Test
    void resolve_caseInsensitive() {
        assertThat(resolver.resolveClientServiceType("KAFKA")).isEqualTo(8660);
        assertThat(resolver.resolveClientServiceType("Kafka")).isEqualTo(8660);
    }

    @Test
    void resolve_null_returnsOtlpClientFallback() {
        assertThat(resolver.resolveClientServiceType(null))
                .isEqualTo(ServiceType.OPENTELEMETRY_CLIENT.getCode());
    }

    @Test
    void resolve_unknown_returnsOtlpClientFallback() {
        assertThat(resolver.resolveClientServiceType("aws_sqs"))
                .isEqualTo(ServiceType.OPENTELEMETRY_CLIENT.getCode());
    }

    @Test
    void resolve_pluginMissingFromRegistry_fallsBack() {
        // Registry knows kafka only — the rest fall back to OPENTELEMETRY_CLIENT.
        ServiceTypeRegistryService kafkaOnly = buildRegistry(Map.of("KAFKA_CLIENT", (short) 8660));
        OtlpMessagingTypeResolver r = new OtlpMessagingTypeResolver(kafkaOnly);

        assertThat(r.resolveClientServiceType("kafka")).isEqualTo(8660);
        assertThat(r.resolveClientServiceType("rabbitmq"))
                .isEqualTo(ServiceType.OPENTELEMETRY_CLIENT.getCode());
        assertThat(r.resolveClientServiceType("pulsar"))
                .isEqualTo(ServiceType.OPENTELEMETRY_CLIENT.getCode());
    }

    @Test
    void resolve_pluginCodeReassigned_followsName() {
        // Simulate kafka plugin re-mapping its code. Resolver should pick up the new code via
        // the name lookup, without any code change here.
        ServiceTypeRegistryService remapped = buildRegistry(Map.of("KAFKA_CLIENT", (short) 9999));
        OtlpMessagingTypeResolver r = new OtlpMessagingTypeResolver(remapped);

        assertThat(r.resolveClientServiceType("kafka")).isEqualTo(9999);
    }

    private static ServiceTypeRegistryService buildRegistry(Map<String, Short> entries) {
        Map<String, ServiceType> byName = new HashMap<>();
        entries.forEach((name, code) -> byName.put(name, ServiceTypeFactory.of(code, name, name)));
        return new ServiceTypeRegistryService() {
            @Override
            public ServiceType findServiceType(int code) {
                return byName.values().stream()
                        .filter(t -> t.getCode() == code)
                        .findFirst()
                        .orElse(ServiceType.UNDEFINED);
            }

            @Override
            public ServiceType findServiceTypeByName(String typeName) {
                return byName.getOrDefault(typeName, ServiceType.UNDEFINED);
            }

            @Override
            public List<ServiceType> findDesc(String desc) {
                return List.of();
            }
        };
    }
}
