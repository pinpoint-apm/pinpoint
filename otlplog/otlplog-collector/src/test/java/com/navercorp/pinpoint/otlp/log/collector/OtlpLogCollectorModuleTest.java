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

package com.navercorp.pinpoint.otlp.log.collector;

import com.navercorp.pinpoint.otlp.log.collector.service.OtlpLogIngestMetrics;
import com.navercorp.pinpoint.otlp.trace.collector.OtlpTraceCollectorHttpModule;
import com.navercorp.pinpoint.otlp.trace.collector.controller.OtlpTraceDecompressionFilter;
import com.navercorp.pinpoint.otlp.trace.collector.controller.OtlpTraceHttpAdmissionFilter;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class OtlpLogCollectorModuleTest {

    /**
     * The gate needs both flags: {@code otlplog.enabled} is the opt-in, and {@code otlptrace.enabled} is
     * the host module whose beans ({@code OtlpExceptionMapper}, the gRPC servers) this module wires
     * into. With only the opt-in checked, "trace off, logs on" would fail the OTLPTRACE context on a
     * missing bean instead of skipping the co-hosted receiver.
     */
    @Test
    void module_isGatedByTheOtlplogFlag_andTheHostTraceFlag_andImportsItsOwnPropertySources() {
        ConditionalOnProperty gate = OtlpLogCollectorModule.class.getAnnotation(ConditionalOnProperty.class);
        assertThat(gate).isNotNull();
        assertThat(gate.name()).containsExactlyInAnyOrder(
                "pinpoint.modules.collector.otlptrace.enabled",
                "pinpoint.modules.collector.otlplog.enabled");
        assertThat(gate.havingValue()).isEqualTo("true");
        assertThat(gate.matchIfMissing()).isFalse();

        Import imports = OtlpLogCollectorModule.class.getAnnotation(Import.class);
        assertThat(imports).isNotNull();
        assertThat(imports.value()).contains(OtlpLogCollectorPropertySources.class,
                OtlpLogCollectorGrpcModule.class, OtlpLogCollectorHttpModule.class);
    }

    /**
     * Both modules register the same filter classes; the servlet container keys filters by name, so
     * the log registrations must carry their own names and be scoped to /v1/logs, or Boot would skip
     * them as "already registered" and the logs endpoint would run without admission control.
     */
    @Test
    void httpFilters_haveDistinctNames_andAreScopedToTheLogsPath() {
        OtlpLogIngestMetrics metrics = new OtlpLogIngestMetrics(new SimpleMeterRegistry());
        OtlpLogCollectorHttpModule module = new OtlpLogCollectorHttpModule();

        FilterRegistrationBean<OtlpTraceHttpAdmissionFilter> admission = module.otlpLogHttpAdmissionFilter(4 << 20, 64 << 20, 32, 1, metrics);
        FilterRegistrationBean<OtlpTraceDecompressionFilter> decompression = module.otlpLogDecompressionFilter(16 << 20, metrics);

        assertThat(admission.getUrlPatterns()).containsExactly("/v1/logs");
        assertThat(decompression.getUrlPatterns()).containsExactly("/v1/logs");
        assertThat(admission.getOrder()).isEqualTo(Ordered.HIGHEST_PRECEDENCE);
        assertThat(decompression.getOrder()).isEqualTo(Ordered.HIGHEST_PRECEDENCE + 1);
        assertThat(OtlpLogCollectorHttpModule.OTLP_HTTP_LOGS_PATH).isNotEqualTo(OtlpTraceCollectorHttpModule.OTLP_HTTP_TRACES_PATH);

        // The registered name is what the servlet container keys on; when unset Boot deduces it from
        // the filter class, which is exactly the collision this guards against.
        assertThat(nameOf(admission)).isEqualTo(OtlpLogCollectorHttpModule.ADMISSION_FILTER_NAME);
        assertThat(nameOf(decompression)).isEqualTo(OtlpLogCollectorHttpModule.DECOMPRESSION_FILTER_NAME);
        assertThat(nameOf(admission)).isNotEqualTo(nameOf(new FilterRegistrationBean<>(admission.getFilter())));
        assertThat(nameOf(decompression)).isNotEqualTo(nameOf(new FilterRegistrationBean<>(decompression.getFilter())));
    }

    private static String nameOf(FilterRegistrationBean<?> registration) {
        // getOrDeduceName(Object) is protected in DynamicRegistrationBean.
        return ReflectionTestUtils.invokeMethod(registration, "getOrDeduceName", registration.getFilter());
    }
}
