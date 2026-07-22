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
import com.navercorp.pinpoint.otlp.trace.collector.mapper.message.OtlpMessagingTypeResolver;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Maps OTel {@code rpc.system} attribute values on SERVER-kind spans to Pinpoint server
 * ServiceType codes. Mirrors {@link OtlpMessagingTypeResolver}: codes are resolved from
 * {@link ServiceTypeRegistryService} by ServiceType <em>name</em>, so a plugin re-mapping
 * its code does not desync this resolver. If the corresponding plugin is not deployed,
 * the resolver gracefully falls back to {@link ServiceType#OPENTELEMETRY_SERVER}.
 *
 * <p>Only {@code rpc.system} is used as the dispatch key — verified against the OTel Java
 * agent source (grpc-1.6 GrpcRpcAttributesGetter emits {@code "grpc"},
 * apache-dubbo-2.7 DubboRpcAttributesGetter emits {@code "apache_dubbo"}).
 * {@code network.protocol.name} is intentionally not used: the OTel agent emits
 * {@code "http"} for both gRPC (over HTTP/2) and plain HTTP servers, so it has no
 * discriminating power for ServiceType selection.</p>
 *
 * <p>HTTP server framework (Tomcat / Jetty / Undertow / Netty) cannot be derived from any
 * OTel span attribute — confirmed against the OTel Java agent which emits only generic
 * {@code http.*} / {@code url.*} / {@code network.*} attributes. Such spans stay on
 * {@link ServiceType#OPENTELEMETRY_SERVER}.</p>
 */
@Component
public class OtlpServerTypeResolver {

    // rpc.system value → registered ServiceType name. Names mirror the agent plugin
    // constants (GrpcConstants GRPC_SERVER, ApacheDubboConstants APACHE_DUBBO_PROVIDER)
    // and are registered on the collector via SPI TraceMetadataProvider / type-provider.yml.
    // "dubbo" is the RC rpc.system.name spelling of the deprecated "apache_dubbo".
    private static final Map<String, String> NAME_MAP = Map.of(
            OtlpTraceConstants.RPC_SYSTEM_GRPC,         "GRPC_SERVER",
            OtlpTraceConstants.RPC_SYSTEM_APACHE_DUBBO, "APACHE_DUBBO_PROVIDER",
            OtlpTraceConstants.RPC_SYSTEM_DUBBO,        "APACHE_DUBBO_PROVIDER");

    private static final int DEFAULT_SERVER = ServiceType.OPENTELEMETRY_SERVER.getCode();

    // rpc.system → resolved code, eagerly populated at construction.
    private final Map<String, Integer> codeMap;

    public OtlpServerTypeResolver(ServiceTypeRegistryService registry) {
        Objects.requireNonNull(registry, "registry");
        Map<String, Integer> resolved = new HashMap<>();
        NAME_MAP.forEach((rpcSystem, typeName) -> {
            ServiceType type = registry.findServiceTypeByName(typeName);
            int code = (type == null || type == ServiceType.UNDEFINED)
                    ? DEFAULT_SERVER : type.getCode();
            resolved.put(rpcSystem, code);
        });
        this.codeMap = Map.copyOf(resolved);
    }

    /**
     * Returns the Pinpoint server ServiceType code for the given {@code rpc.system}
     * (case-insensitive), or {@link ServiceType#OPENTELEMETRY_SERVER} when the system is
     * unknown / unsupported / its plugin is not deployed / {@code rpcSystem} is {@code null}.
     */
    public int resolveServerServiceType(String rpcSystem) {
        if (rpcSystem == null) {
            return DEFAULT_SERVER;
        }
        Integer code = codeMap.get(rpcSystem.toLowerCase());
        return code != null ? code : DEFAULT_SERVER;
    }
}
