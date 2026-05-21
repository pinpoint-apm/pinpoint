package com.navercorp.pinpoint.otlp.trace.collector.mapper;

import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.trace.ServiceTypeFactory;
import com.navercorp.pinpoint.loader.service.ServiceTypeRegistryService;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class OtlpClientTypeResolverTest {

    private static final ServiceTypeRegistryService FULL_REGISTRY = buildRegistry(Map.of(
            "GRPC",                  (short) 9160,
            "APACHE_DUBBO_CONSUMER", (short) 9997));

    private final OtlpClientTypeResolver resolver = new OtlpClientTypeResolver(FULL_REGISTRY);

    @Test
    void resolve_grpc() {
        assertThat(resolver.resolveClientServiceType("grpc")).isEqualTo(9160);
    }

    @Test
    void resolve_apacheDubbo() {
        assertThat(resolver.resolveClientServiceType("apache_dubbo")).isEqualTo(9997);
    }

    @Test
    void resolve_caseInsensitive() {
        assertThat(resolver.resolveClientServiceType("GRPC")).isEqualTo(9160);
        assertThat(resolver.resolveClientServiceType("Apache_Dubbo")).isEqualTo(9997);
    }

    @Test
    void resolve_null_returnsOtlpClientFallback() {
        assertThat(resolver.resolveClientServiceType(null))
                .isEqualTo(ServiceType.OPENTELEMETRY_CLIENT.getCode());
    }

    @Test
    void resolve_unknown_returnsOtlpClientFallback() {
        // OTel rpc.system values without a Pinpoint counterpart.
        assertThat(resolver.resolveClientServiceType("java_rmi"))
                .isEqualTo(ServiceType.OPENTELEMETRY_CLIENT.getCode());
        assertThat(resolver.resolveClientServiceType("connect_rpc"))
                .isEqualTo(ServiceType.OPENTELEMETRY_CLIENT.getCode());
        assertThat(resolver.resolveClientServiceType("dotnet_wcf"))
                .isEqualTo(ServiceType.OPENTELEMETRY_CLIENT.getCode());
    }

    @Test
    void resolve_pluginMissingFromRegistry_fallsBack() {
        // Registry knows grpc only — apache_dubbo falls back to OPENTELEMETRY_CLIENT.
        ServiceTypeRegistryService grpcOnly = buildRegistry(Map.of("GRPC", (short) 9160));
        OtlpClientTypeResolver r = new OtlpClientTypeResolver(grpcOnly);

        assertThat(r.resolveClientServiceType("grpc")).isEqualTo(9160);
        assertThat(r.resolveClientServiceType("apache_dubbo"))
                .isEqualTo(ServiceType.OPENTELEMETRY_CLIENT.getCode());
    }

    @Test
    void resolve_pluginCodeReassigned_followsName() {
        ServiceTypeRegistryService remapped = buildRegistry(Map.of("GRPC", (short) 9999));
        OtlpClientTypeResolver r = new OtlpClientTypeResolver(remapped);

        assertThat(r.resolveClientServiceType("grpc")).isEqualTo(9999);
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
