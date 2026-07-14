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

import com.navercorp.pinpoint.common.server.bo.AnnotationBo;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.trace.attribute.AttributeValue;
import com.navercorp.pinpoint.loader.service.ServiceTypeRegistryService;
import com.navercorp.pinpoint.otlp.trace.collector.util.AttributeUtils;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Detects Envoy proxy spans that arrive via OTLP and maps them to the dedicated Envoy
 * ServiceTypes — {@code ENVOY} (1550) for the SERVER-kind ingress node and
 * {@code ENVOY_EGRESS} (9302) for the CLIENT-kind egress call. This complements the native
 * pinpoint-cpp Envoy tracer (which sets SVC_TYPE_ENVOY 1550 on the span and
 * SVC_TYPE_ENVOY_INGRESS 9301 / SVC_TYPE_ENVOY_EGRESS 9302 on its child SpanEvents).
 *
 * <p><b>Detection gate.</b> Envoy is identified by the presence of Envoy-specific span tags
 * ({@code upstream_cluster} / {@code upstream_cluster.name} / {@code response_flags}). These
 * are emitted only by Envoy, so they do not collide with other instrumentations.
 * {@code component=proxy} is intentionally not used — it is deprecated in OTel semconv and
 * emitted by other proxies as well, so it has no reliable discriminating power on its own.</p>
 *
 * <p><b>Direction.</b> The caller decides ingress vs egress from {@code span.kind}
 * (SERVER → ingress, CLIENT → egress), matching how Envoy's OTLP exporter derives span kind
 * from the listener operation name.</p>
 *
 * <p><b>ServiceType category matters.</b> A SERVER-kind Envoy span becomes a root SpanBo — a
 * ServerMap <em>node</em> — so it must carry a SERVER-category type: {@code ENVOY} (1550),
 * NOT {@code ENVOY_INGRESS} (9301, an RPC-category <em>call</em> type used by the native tracer
 * on a child SpanEvent). A CLIENT-kind Envoy span becomes a SpanEvent — an outbound
 * <em>call</em> — so it carries the RPC-category {@code ENVOY_EGRESS} (9302). The ingress
 * direction is otherwise preserved via the {@code envoy.operation} annotation. This mirrors how
 * OTLP server nodes use {@code OPENTELEMETRY_SERVER} (1220) while client events use
 * {@code OPENTELEMETRY_CLIENT} (9310).</p>
 *
 * <p><b>Graceful fallback.</b> ServiceType codes are resolved from
 * {@link ServiceTypeRegistryService} by <em>name</em>. When the {@code envoy-type-provider}
 * is not deployed on the collector classpath the resolver falls back to
 * {@link ServiceType#OPENTELEMETRY_SERVER} / {@link ServiceType#OPENTELEMETRY_CLIENT}, so the
 * mapping degrades to today's behavior instead of failing.</p>
 */
@Component
public class OtlpEnvoyTypeResolver {

    private static final String SERVICE_TYPE_NAME_ENVOY = "ENVOY";
    private static final String SERVICE_TYPE_NAME_ENVOY_EGRESS = "ENVOY_EGRESS";

    // envoy.operation annotation values, mirroring the native tracer's AppendString(...).
    private static final String OPERATION_INGRESS = "Ingress";
    private static final String OPERATION_EGRESS = "Egress";

    private final int nodeCode;
    private final int egressCode;

    public OtlpEnvoyTypeResolver(ServiceTypeRegistryService registry) {
        Objects.requireNonNull(registry, "registry");
        this.nodeCode = resolve(registry, SERVICE_TYPE_NAME_ENVOY, ServiceType.OPENTELEMETRY_SERVER.getCode());
        this.egressCode = resolve(registry, SERVICE_TYPE_NAME_ENVOY_EGRESS, ServiceType.OPENTELEMETRY_CLIENT.getCode());
    }

    private static int resolve(ServiceTypeRegistryService registry, String typeName, int fallback) {
        ServiceType type = registry.findServiceTypeByName(typeName);
        return (type == null || type == ServiceType.UNDEFINED) ? fallback : type.getCode();
    }

    /**
     * Envoy detection gate. Keyed on Envoy-specific tags only (see class Javadoc).
     */
    public boolean isEnvoy(Map<String, AttributeValue> attributes) {
        return attributes.containsKey(OtlpTraceConstants.ATTRIBUTE_KEY_UPSTREAM_CLUSTER)
                || attributes.containsKey(OtlpTraceConstants.ATTRIBUTE_KEY_UPSTREAM_CLUSTER_NAME)
                || attributes.containsKey(OtlpTraceConstants.ATTRIBUTE_KEY_RESPONSE_FLAGS);
    }

    /**
     * Envoy node (SERVER-category) code for a SERVER-kind ingress span, or OPENTELEMETRY_SERVER
     * when the Envoy plugin is not deployed.
     */
    public int resolveNodeServiceType() {
        return nodeCode;
    }

    /** ENVOY_EGRESS code, or OPENTELEMETRY_CLIENT when the Envoy plugin is not deployed. */
    public int resolveEgressServiceType() {
        return egressCode;
    }

    /**
     * Attaches the Envoy annotations (upstream.cluster + envoy.operation) to a Span/SpanEvent.
     * upstream_cluster.name is preferred over the legacy upstream_cluster tag. The consumed
     * cluster key is collected into {@code consumedKeys} so it is excluded from the raw
     * attribute list; response_flags is only a detection gate (never promoted) and stays raw.
     */
    public void recordAnnotations(Consumer<AnnotationBo> sink, Map<String, AttributeValue> attributes, boolean ingress, Set<String> consumedKeys) {
        String cluster = AttributeUtils.getAttributeStringValue(attributes, OtlpTraceConstants.ATTRIBUTE_KEY_UPSTREAM_CLUSTER_NAME, null);
        if (cluster != null) {
            consumedKeys.add(OtlpTraceConstants.ATTRIBUTE_KEY_UPSTREAM_CLUSTER_NAME);
        } else {
            cluster = AttributeUtils.getAttributeStringValue(attributes, OtlpTraceConstants.ATTRIBUTE_KEY_UPSTREAM_CLUSTER, null);
            if (cluster != null) {
                consumedKeys.add(OtlpTraceConstants.ATTRIBUTE_KEY_UPSTREAM_CLUSTER);
            }
        }
        if (cluster != null) {
            sink.accept(AnnotationBo.of(OtlpTraceConstants.ANNOTATION_KEY_UPSTREAM_CLUSTER, cluster));
        }
        sink.accept(AnnotationBo.of(OtlpTraceConstants.ANNOTATION_KEY_ENVOY_OPERATION,
                ingress ? OPERATION_INGRESS : OPERATION_EGRESS));
    }
}
