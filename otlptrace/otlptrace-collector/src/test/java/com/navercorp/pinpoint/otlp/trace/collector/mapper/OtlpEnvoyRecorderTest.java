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
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class OtlpEnvoyRecorderTest {

    private final OtlpEnvoyRecorder recorder = new OtlpEnvoyRecorder();

    // =======================================================================
    // isEnvoy — detection gate
    // =======================================================================

    @Test
    void isEnvoy_true_whenUpstreamCluster() {
        assertThat(recorder.isEnvoy(Map.of(
                OtlpTraceConstants.ATTRIBUTE_KEY_UPSTREAM_CLUSTER, AttributeValue.of("frontend")))).isTrue();
    }

    @Test
    void isEnvoy_true_whenUpstreamClusterName() {
        assertThat(recorder.isEnvoy(Map.of(
                OtlpTraceConstants.ATTRIBUTE_KEY_UPSTREAM_CLUSTER_NAME, AttributeValue.of("frontend")))).isTrue();
    }

    @Test
    void isEnvoy_true_whenResponseFlags() {
        assertThat(recorder.isEnvoy(Map.of(
                OtlpTraceConstants.ATTRIBUTE_KEY_RESPONSE_FLAGS, AttributeValue.of("-")))).isTrue();
    }

    @Test
    void isEnvoy_false_whenNoEnvoyMarkers() {
        // component=proxy alone is intentionally NOT a discriminator.
        Map<String, AttributeValue> attrs = new HashMap<>();
        attrs.put("component", AttributeValue.of("proxy"));
        attrs.put(OtlpTraceConstants.ATTRIBUTE_KEY_HTTP_URL, AttributeValue.of("http://x/y"));
        assertThat(recorder.isEnvoy(attrs)).isFalse();
    }

    @Test
    void isEnvoy_false_whenEmpty() {
        assertThat(recorder.isEnvoy(Map.of())).isFalse();
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
        Set<String> consumedKeys = new HashSet<>();
        recorder.recordAnnotations(out::add, attrs, false, consumedKeys);

        assertThat(annotationValue(out, OtlpTraceConstants.ANNOTATION_KEY_UPSTREAM_CLUSTER)).isEqualTo("frontend");
        assertThat(annotationValue(out, OtlpTraceConstants.ANNOTATION_KEY_ENVOY_OPERATION)).isEqualTo("Egress");
        // only the consumed cluster key is collected
        assertThat(consumedKeys).containsExactly(OtlpTraceConstants.ATTRIBUTE_KEY_UPSTREAM_CLUSTER_NAME);
    }

    @Test
    void recordAnnotations_ingress_fallsBackToLegacyUpstreamCluster() {
        Map<String, AttributeValue> attrs = Map.of(
                OtlpTraceConstants.ATTRIBUTE_KEY_UPSTREAM_CLUSTER, AttributeValue.of("frontend"));

        List<AnnotationBo> out = new ArrayList<>();
        Set<String> consumedKeys = new HashSet<>();
        recorder.recordAnnotations(out::add, attrs, true, consumedKeys);

        assertThat(annotationValue(out, OtlpTraceConstants.ANNOTATION_KEY_UPSTREAM_CLUSTER)).isEqualTo("frontend");
        assertThat(annotationValue(out, OtlpTraceConstants.ANNOTATION_KEY_ENVOY_OPERATION)).isEqualTo("Ingress");
        assertThat(consumedKeys).containsExactly(OtlpTraceConstants.ATTRIBUTE_KEY_UPSTREAM_CLUSTER);
    }

    @Test
    void recordAnnotations_noCluster_onlyOperationRecorded() {
        // response_flags-only Envoy span: no cluster annotation, but operation is still tagged.
        List<AnnotationBo> out = new ArrayList<>();
        recorder.recordAnnotations(out::add, Map.of(), false, new HashSet<>());

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
}
