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

package com.navercorp.pinpoint.otlp.trace.collector.inspector;

import com.navercorp.pinpoint.inspector.collector.InspectorCollectorConfig;
import com.navercorp.pinpoint.otlp.trace.collector.OtlpTraceCollectorHttpModule;
import com.navercorp.pinpoint.otlp.trace.collector.OtlpTraceCollectorModule;
import com.navercorp.pinpoint.otlp.trace.collector.controller.OtlpTraceDecompressionFilter;
import com.navercorp.pinpoint.otlp.trace.collector.controller.OtlpTraceHttpAdmissionFilter;
import com.navercorp.pinpoint.otlp.trace.collector.inspector.service.OtlpMetricIngestMetrics;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class OtlpMetricInspectorModuleTest {

    /**
     * Two gates: the feature key (otlptrace properties, default off) and the Inspector storage
     * module's key. With only the feature key checked, "feature on, storage off" would fail the
     * OTLPTRACE context on a missing AgentStatService instead of skipping the receiver.
     */
    @Test
    void module_isGatedByTheFeatureKey_andTheInspectorStorageKey_andImportsTheStorageConfig() {
        ConditionalOnProperty gate = OtlpMetricInspectorModule.class.getAnnotation(ConditionalOnProperty.class);
        assertThat(gate).isNotNull();
        assertThat(gate.name()).containsExactlyInAnyOrder(
                "pinpoint.collector.otlptrace.inspector.enabled",
                "pinpoint.modules.collector.inspector.enabled");
        assertThat(gate.havingValue()).isEqualTo("true");
        assertThat(gate.matchIfMissing()).isFalse();

        Import imports = OtlpMetricInspectorModule.class.getAnnotation(Import.class);
        assertThat(imports).isNotNull();
        assertThat(imports.value()).contains(OtlpMetricInspectorGrpcModule.class, OtlpMetricInspectorHttpModule.class,
                InspectorCollectorConfig.class);
    }

    @Test
    void traceModule_importsTheInspectorModule() {
        Import imports = OtlpTraceCollectorModule.class.getAnnotation(Import.class);
        assertThat(imports.value()).contains(OtlpMetricInspectorModule.class);
    }

    /**
     * Three modules register the same filter classes; the servlet container keys filters by name, so
     * the metrics registrations must carry their own names and be scoped to /v1/metrics, or Boot
     * would skip them as "already registered" and the metrics endpoint would run without admission.
     */
    @Test
    void httpFilters_haveDistinctNames_andAreScopedToTheMetricsPath() {
        OtlpMetricIngestMetrics metrics = new OtlpMetricIngestMetrics(new SimpleMeterRegistry());
        OtlpMetricInspectorHttpModule module = new OtlpMetricInspectorHttpModule();

        FilterRegistrationBean<OtlpTraceHttpAdmissionFilter> admission = module.otlpMetricHttpAdmissionFilter(4 << 20, 32 << 20, 16, 1, metrics);
        FilterRegistrationBean<OtlpTraceDecompressionFilter> decompression = module.otlpMetricDecompressionFilter(16 << 20, metrics);

        assertThat(admission.getUrlPatterns()).containsExactly("/v1/metrics");
        assertThat(decompression.getUrlPatterns()).containsExactly("/v1/metrics");
        assertThat(admission.getOrder()).isEqualTo(Ordered.HIGHEST_PRECEDENCE);
        assertThat(decompression.getOrder()).isEqualTo(Ordered.HIGHEST_PRECEDENCE + 1);
        assertThat(OtlpMetricInspectorHttpModule.OTLP_HTTP_METRICS_PATH).isNotEqualTo(OtlpTraceCollectorHttpModule.OTLP_HTTP_TRACES_PATH);

        assertThat(nameOf(admission)).isEqualTo(OtlpMetricInspectorHttpModule.ADMISSION_FILTER_NAME);
        assertThat(nameOf(decompression)).isEqualTo(OtlpMetricInspectorHttpModule.DECOMPRESSION_FILTER_NAME);
        assertThat(nameOf(admission)).isNotEqualTo(nameOf(new FilterRegistrationBean<>(admission.getFilter())));
        assertThat(nameOf(decompression)).isNotEqualTo(nameOf(new FilterRegistrationBean<>(decompression.getFilter())));
    }

    @Test
    void dialects_areParsedInOrder_andUnknownNamesFail() {
        OtlpMetricInspectorModule module = new OtlpMetricInspectorModule();
        assertThat(module.otlpMetricVocabulary(java.util.List.of("otel", "micrometer")).dialectNames()).containsExactly("otel", "micrometer");
        assertThat(module.otlpMetricVocabulary(java.util.List.of(" Micrometer ", "otel")).dialectNames()).containsExactly("micrometer", "otel");
        assertThat(module.otlpMetricVocabulary(java.util.List.of("otel")).dialectNames()).containsExactly("otel");
        assertThat(module.otlpMetricVocabulary(java.util.List.of("otel", "micrometer", "go", "nodejs", "python", "dotnet")).dialectNames()).containsExactly("otel", "micrometer", "go", "nodejs", "python", "dotnet");
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> module.otlpMetricVocabulary(java.util.List.of("otel", "prometheus")))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("prometheus");
    }

    @Test
    void gateReporter_isActiveOnlyWhenBothKeysAreOn() {
        assertThat(new OtlpMetricInspectorGateReporter(env("true", "true")).isActive()).isTrue();
        assertThat(new OtlpMetricInspectorGateReporter(env("true", "false")).isActive()).isFalse();
        assertThat(new OtlpMetricInspectorGateReporter(env("false", "true")).isActive()).isFalse();
        assertThat(new OtlpMetricInspectorGateReporter(new MockEnvironment()).isActive()).isFalse();
    }

    private static MockEnvironment env(String feature, String storage) {
        return new MockEnvironment()
                .withProperty(OtlpMetricInspectorModule.ENABLED_KEY, feature)
                .withProperty(OtlpMetricInspectorModule.INSPECTOR_MODULE_ENABLED_KEY, storage);
    }

    private static String nameOf(FilterRegistrationBean<?> registration) {
        return ReflectionTestUtils.invokeMethod(registration, "getOrDeduceName", registration.getFilter());
    }
}
