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
 * Maps OTel {@code rpc.system} attribute values on CLIENT-kind spans to Pinpoint client
 * ServiceType codes. Mirrors {@link OtlpServerTypeResolver} but targets the client-side
 * counterpart ServiceTypes (GRPC instead of GRPC_SERVER, APACHE_DUBBO_CONSUMER instead of
 * APACHE_DUBBO_PROVIDER). Codes are resolved from {@link ServiceTypeRegistryService} by
 * ServiceType <em>name</em> so plugin re-mapping is followed automatically; missing
 * plugins fall back to {@link ServiceType#OPENTELEMETRY_CLIENT}.
 *
 * <p>Only {@code rpc.system} is used as the dispatch key — verified against the OTel Java
 * agent source (grpc-1.6 GrpcRpcAttributesGetter emits {@code "grpc"},
 * apache-dubbo-2.7 DubboRpcAttributesGetter emits {@code "apache_dubbo"} on the client
 * instrumenter). {@code network.protocol.name} is not used: gRPC client spans emit it as
 * {@code "http"}, identical to plain HTTP clients, so it has no discriminating power.</p>
 *
 * <p>HTTP client framework (Apache HttpClient / OkHttp / java-http-client / async-http-client)
 * cannot be derived from any OTel span attribute — verified against OTel Java agent which
 * emits only generic {@code http.*} / {@code url.*} / {@code network.*} attributes. Such
 * spans stay on {@link ServiceType#OPENTELEMETRY_CLIENT}.</p>
 */
@Component
public class OtlpClientTypeResolver {

    // rpc.system value → registered ServiceType name. Names mirror the agent plugin
    // constants (GrpcConstants GRPC, ApacheDubboConstants APACHE_DUBBO_CONSUMER) and are
    // registered on the collector via SPI TraceMetadataProvider / type-provider.yml.
    // "dubbo" is the RC rpc.system.name spelling of the deprecated "apache_dubbo".
    private static final Map<String, String> NAME_MAP = Map.of(
            OtlpTraceConstants.RPC_SYSTEM_GRPC,         "GRPC",
            OtlpTraceConstants.RPC_SYSTEM_APACHE_DUBBO, "APACHE_DUBBO_CONSUMER",
            OtlpTraceConstants.RPC_SYSTEM_DUBBO,        "APACHE_DUBBO_CONSUMER");

    private static final int DEFAULT_CLIENT = ServiceType.OPENTELEMETRY_CLIENT.getCode();

    // rpc.system → resolved code, eagerly populated at construction.
    private final Map<String, Integer> codeMap;

    public OtlpClientTypeResolver(ServiceTypeRegistryService registry) {
        Objects.requireNonNull(registry, "registry");
        Map<String, Integer> resolved = new HashMap<>();
        NAME_MAP.forEach((rpcSystem, typeName) -> {
            ServiceType type = registry.findServiceTypeByName(typeName);
            int code = (type == null || type == ServiceType.UNDEFINED)
                    ? DEFAULT_CLIENT : type.getCode();
            resolved.put(rpcSystem, code);
        });
        this.codeMap = Map.copyOf(resolved);
    }

    /**
     * Returns the Pinpoint client ServiceType code for the given {@code rpc.system}
     * (case-insensitive), or {@link ServiceType#OPENTELEMETRY_CLIENT} when the system is
     * unknown / unsupported / its plugin is not deployed / {@code rpcSystem} is {@code null}.
     */
    public int resolveClientServiceType(String rpcSystem) {
        if (rpcSystem == null) {
            return DEFAULT_CLIENT;
        }
        Integer code = codeMap.get(rpcSystem.toLowerCase());
        return code != null ? code : DEFAULT_CLIENT;
    }
}
