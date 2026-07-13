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
import com.navercorp.pinpoint.common.trace.ServiceTypeFactory;
import com.navercorp.pinpoint.common.trace.attribute.AttributeValue;
import com.navercorp.pinpoint.loader.service.ServiceTypeRegistryService;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class OtlpEnvoyTypeResolverTest {

    private static final short ENVOY_NODE_CODE = 1550;
    private static final short ENVOY_EGRESS_CODE = 9302;

    private static final ServiceTypeRegistryService FULL_REGISTRY = buildRegistry(Map.of(
            "ENVOY", ENVOY_NODE_CODE,
            "ENVOY_EGRESS", ENVOY_EGRESS_CODE));

    private final OtlpEnvoyTypeResolver resolver = new OtlpEnvoyTypeResolver(FULL_REGISTRY);

    // =======================================================================
    // isEnvoy — detection gate
    // =======================================================================

    @Test
    void isEnvoy_true_whenUpstreamCluster() {
        assertThat(resolver.isEnvoy(Map.of(
                OtlpTraceConstants.ATTRIBUTE_KEY_UPSTREAM_CLUSTER, AttributeValue.of("frontend")))).isTrue();
    }

    @Test
    void isEnvoy_true_whenUpstreamClusterName() {
        assertThat(resolver.isEnvoy(Map.of(
                OtlpTraceConstants.ATTRIBUTE_KEY_UPSTREAM_CLUSTER_NAME, AttributeValue.of("frontend")))).isTrue();
    }

    @Test
    void isEnvoy_true_whenResponseFlags() {
        assertThat(resolver.isEnvoy(Map.of(
                OtlpTraceConstants.ATTRIBUTE_KEY_RESPONSE_FLAGS, AttributeValue.of("-")))).isTrue();
    }

    @Test
    void isEnvoy_false_whenNoEnvoyMarkers() {
        // component=proxy alone is intentionally NOT a discriminator.
        Map<String, AttributeValue> attrs = new HashMap<>();
        attrs.put("component", AttributeValue.of("proxy"));
        attrs.put(OtlpTraceConstants.ATTRIBUTE_KEY_HTTP_URL, AttributeValue.of("http://x/y"));
        assertThat(resolver.isEnvoy(attrs)).isFalse();
    }

    @Test
    void isEnvoy_false_whenEmpty() {
        assertThat(resolver.isEnvoy(Map.of())).isFalse();
    }

    // =======================================================================
    // resolve — ServiceType codes
    // =======================================================================

    @Test
    void resolveNode_returnsEnvoy() {
        // SERVER-kind ingress span → SERVER-category ENVOY (1550) node type, not ENVOY_INGRESS.
        assertThat(resolver.resolveNodeServiceType()).isEqualTo(ENVOY_NODE_CODE);
    }

    @Test
    void resolveEgress_returnsEnvoyEgress() {
        assertThat(resolver.resolveEgressServiceType()).isEqualTo(ENVOY_EGRESS_CODE);
    }

    @Test
    void resolve_pluginMissingFromRegistry_fallsBackToOtel() {
        // Collector without the envoy-type-provider on its classpath: degrade to the generic
        // OpenTelemetry server/client types instead of failing.
        OtlpEnvoyTypeResolver r = new OtlpEnvoyTypeResolver(buildRegistry(Map.of()));
        assertThat(r.resolveNodeServiceType()).isEqualTo(ServiceType.OPENTELEMETRY_SERVER.getCode());
        assertThat(r.resolveEgressServiceType()).isEqualTo(ServiceType.OPENTELEMETRY_CLIENT.getCode());
    }

    // =======================================================================
    // recordAnnotations
    // =======================================================================

    @Test
    void recordAnnotations_egress_upstreamClusterNamePreferred() {
        Map<String, AttributeValue> attrs = new HashMap<>();
        attrs.put(OtlpTraceConstants.ATTRIBUTE_KEY_UPSTREAM_CLUSTER_NAME, AttributeValue.of("frontend"));
        attrs.put(OtlpTraceConstants.ATTRIBUTE_KEY_UPSTREAM_CLUSTER, AttributeValue.of("legacy"));

        List<AnnotationBo> out = new ArrayList<>();
        resolver.recordAnnotations(out::add, attrs, false);

        assertThat(annotationValue(out, OtlpTraceConstants.ANNOTATION_KEY_UPSTREAM_CLUSTER)).isEqualTo("frontend");
        assertThat(annotationValue(out, OtlpTraceConstants.ANNOTATION_KEY_ENVOY_OPERATION)).isEqualTo("Egress");
    }

    @Test
    void recordAnnotations_ingress_fallsBackToLegacyUpstreamCluster() {
        Map<String, AttributeValue> attrs = Map.of(
                OtlpTraceConstants.ATTRIBUTE_KEY_UPSTREAM_CLUSTER, AttributeValue.of("frontend"));

        List<AnnotationBo> out = new ArrayList<>();
        resolver.recordAnnotations(out::add, attrs, true);

        assertThat(annotationValue(out, OtlpTraceConstants.ANNOTATION_KEY_UPSTREAM_CLUSTER)).isEqualTo("frontend");
        assertThat(annotationValue(out, OtlpTraceConstants.ANNOTATION_KEY_ENVOY_OPERATION)).isEqualTo("Ingress");
    }

    @Test
    void recordAnnotations_noCluster_onlyOperationRecorded() {
        // response_flags-only Envoy span: no cluster annotation, but operation is still tagged.
        List<AnnotationBo> out = new ArrayList<>();
        resolver.recordAnnotations(out::add, Map.of(), false);

        assertThat(annotationValue(out, OtlpTraceConstants.ANNOTATION_KEY_UPSTREAM_CLUSTER)).isNull();
        assertThat(annotationValue(out, OtlpTraceConstants.ANNOTATION_KEY_ENVOY_OPERATION)).isEqualTo("Egress");
    }

    private static Object annotationValue(List<AnnotationBo> annotations, int key) {
        return annotations.stream()
                .filter(a -> a.getKey() == key)
                .map(AnnotationBo::getValue)
                .findFirst()
                .orElse(null);
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
