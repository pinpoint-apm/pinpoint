package com.navercorp.pinpoint.otlp.trace.collector.mapper;

import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.trace.ServiceTypeFactory;
import com.navercorp.pinpoint.loader.service.ServiceTypeRegistryService;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class OtlpServerTypeResolverTest {

    private static final ServiceTypeRegistryService FULL_REGISTRY = buildRegistry(Map.of(
            "GRPC_SERVER",           (short) 1130,
            "APACHE_DUBBO_PROVIDER", (short) 1999));

    private final OtlpServerTypeResolver resolver = new OtlpServerTypeResolver(FULL_REGISTRY);

    @Test
    void resolve_grpc() {
        assertThat(resolver.resolveServerServiceType("grpc")).isEqualTo(1130);
    }

    @Test
    void resolve_apacheDubbo() {
        assertThat(resolver.resolveServerServiceType("apache_dubbo")).isEqualTo(1999);
    }

    @Test
    void resolve_caseInsensitive() {
        assertThat(resolver.resolveServerServiceType("GRPC")).isEqualTo(1130);
        assertThat(resolver.resolveServerServiceType("Apache_Dubbo")).isEqualTo(1999);
    }

    @Test
    void resolve_null_returnsOtlpServerFallback() {
        assertThat(resolver.resolveServerServiceType(null))
                .isEqualTo(ServiceType.OPENTELEMETRY_SERVER.getCode());
    }

    @Test
    void resolve_unknown_returnsOtlpServerFallback() {
        // OTel rpc.system values without a Pinpoint counterpart.
        assertThat(resolver.resolveServerServiceType("java_rmi"))
                .isEqualTo(ServiceType.OPENTELEMETRY_SERVER.getCode());
        assertThat(resolver.resolveServerServiceType("connect_rpc"))
                .isEqualTo(ServiceType.OPENTELEMETRY_SERVER.getCode());
        assertThat(resolver.resolveServerServiceType("dotnet_wcf"))
                .isEqualTo(ServiceType.OPENTELEMETRY_SERVER.getCode());
    }

    @Test
    void resolve_pluginMissingFromRegistry_fallsBack() {
        // Registry knows grpc only — apache_dubbo falls back to OPENTELEMETRY_SERVER.
        ServiceTypeRegistryService grpcOnly = buildRegistry(Map.of("GRPC_SERVER", (short) 1130));
        OtlpServerTypeResolver r = new OtlpServerTypeResolver(grpcOnly);

        assertThat(r.resolveServerServiceType("grpc")).isEqualTo(1130);
        assertThat(r.resolveServerServiceType("apache_dubbo"))
                .isEqualTo(ServiceType.OPENTELEMETRY_SERVER.getCode());
    }

    @Test
    void resolve_pluginCodeReassigned_followsName() {
        // Simulate grpc plugin re-mapping its code. Resolver picks up the new code via
        // the name lookup, without any code change here.
        ServiceTypeRegistryService remapped = buildRegistry(Map.of("GRPC_SERVER", (short) 9999));
        OtlpServerTypeResolver r = new OtlpServerTypeResolver(remapped);

        assertThat(r.resolveServerServiceType("grpc")).isEqualTo(9999);
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
