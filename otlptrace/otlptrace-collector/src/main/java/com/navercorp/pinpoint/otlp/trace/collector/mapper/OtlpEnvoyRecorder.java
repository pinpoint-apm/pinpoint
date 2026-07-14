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
import com.navercorp.pinpoint.common.trace.attribute.AttributeValue;
import com.navercorp.pinpoint.otlp.trace.collector.util.AttributeUtils;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Detects Envoy proxy spans that arrive via OTLP and records the Envoy identification
 * annotations ({@code envoy.operation} + {@code upstream.cluster}) on the Span/SpanEvent.
 *
 * <p><b>No ServiceType override.</b> Envoy spans keep the regular OTLP types
 * (OPENTELEMETRY_SERVER / OPENTELEMETRY_CLIENT): re-typing them to ENVOY (1550) /
 * ENVOY_EGRESS (9302) mixed two node types under a single applicationName whenever the
 * tag-based detection missed a span (the gate depends on Envoy's deployment-specific tag
 * configuration), causing ApplicationIndex/ServerMap type conflicts. The dedicated Envoy
 * ServiceType codes remain registered in envoy-type-provider.yml for the native pinpoint-cpp
 * Envoy tracer, which emits them directly. Envoy identification on the OTLP path is carried
 * by the annotations only.</p>
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
 */
@Component
public class OtlpEnvoyRecorder {

    // envoy.operation annotation values, mirroring the native tracer's AppendString(...).
    private static final String OPERATION_INGRESS = "Ingress";
    private static final String OPERATION_EGRESS = "Egress";

    /**
     * Envoy detection gate. Keyed on Envoy-specific tags only (see class Javadoc).
     */
    public boolean isEnvoy(Map<String, AttributeValue> attributes) {
        return attributes.containsKey(OtlpTraceConstants.ATTRIBUTE_KEY_UPSTREAM_CLUSTER)
                || attributes.containsKey(OtlpTraceConstants.ATTRIBUTE_KEY_UPSTREAM_CLUSTER_NAME)
                || attributes.containsKey(OtlpTraceConstants.ATTRIBUTE_KEY_RESPONSE_FLAGS);
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
