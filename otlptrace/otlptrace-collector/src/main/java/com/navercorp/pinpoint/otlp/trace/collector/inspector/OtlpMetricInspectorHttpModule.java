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

import com.navercorp.pinpoint.otlp.trace.collector.controller.OtlpTraceDecompressionFilter;
import com.navercorp.pinpoint.otlp.trace.collector.controller.OtlpTraceHttpAdmissionFilter;
import com.navercorp.pinpoint.otlp.trace.collector.inspector.service.OtlpMetricIngestMetrics;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

/**
 * HTTP side of the metrics receiver: {@code POST /v1/metrics} on the trace collector's servlet
 * port, fronted by its own instances of the trace path's admission and decompression filters. Same
 * filter classes, separate budgets and metrics ({@link OtlpMetricIngestMetrics}), scoped to the
 * metrics path only.
 */
@Configuration
public class OtlpMetricInspectorHttpModule {

    public static final String OTLP_HTTP_METRICS_PATH = "/v1/metrics";

    // Explicit registration names: the servlet container keys filters by name and Spring Boot
    // deduces the name from the filter class when none is set. The trace and log modules register
    // the same two classes for their paths, so without these names this registration of each class
    // would be silently skipped ("possibly already registered").
    public static final String ADMISSION_FILTER_NAME = "otlpMetricHttpAdmissionFilter";
    public static final String DECOMPRESSION_FILTER_NAME = "otlpMetricDecompressionFilter";

    /**
     * Per-request size cap (413), concurrent-request and in-flight byte gates (503 + Retry-After) for
     * {@value #OTLP_HTTP_METRICS_PATH}, enforced before the protobuf body is buffered. Budgets are the
     * metrics receiver's own and independent of the gRPC metrics budget.
     */
    @Bean
    public FilterRegistrationBean<OtlpTraceHttpAdmissionFilter> otlpMetricHttpAdmissionFilter(
            @Value("${pinpoint.collector.otlptrace.inspector.http.max-request-bytes:4194304}") int maxRequestBytes,
            @Value("${pinpoint.collector.otlptrace.inspector.http.admission.max-in-flight-bytes:33554432}") int maxInFlightBytes,
            @Value("${pinpoint.collector.otlptrace.inspector.http.max-concurrent-requests:16}") int maxConcurrentRequests,
            @Value("${pinpoint.collector.otlptrace.inspector.http.rejected.retry-after-seconds:1}") int retryAfterSeconds,
            OtlpMetricIngestMetrics ingestMetrics) {
        OtlpTraceHttpAdmissionFilter filter =
                new OtlpTraceHttpAdmissionFilter(maxRequestBytes, maxInFlightBytes, maxConcurrentRequests, retryAfterSeconds, ingestMetrics);
        FilterRegistrationBean<OtlpTraceHttpAdmissionFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setName(ADMISSION_FILTER_NAME);
        registration.addUrlPatterns(OTLP_HTTP_METRICS_PATH);
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registration;
    }

    /**
     * Inflates {@code Content-Encoding: gzip} bodies for {@value #OTLP_HTTP_METRICS_PATH} with a
     * decompression-bomb cap; other encodings are refused with 415. Ordered right after the admission
     * filter so the compressed-size gates apply first.
     */
    @Bean
    public FilterRegistrationBean<OtlpTraceDecompressionFilter> otlpMetricDecompressionFilter(
            @Value("${pinpoint.collector.otlptrace.inspector.http.max-decompressed-request-bytes:16777216}") int maxDecompressedBytes,
            OtlpMetricIngestMetrics ingestMetrics) {
        OtlpTraceDecompressionFilter filter = new OtlpTraceDecompressionFilter(maxDecompressedBytes, ingestMetrics);
        FilterRegistrationBean<OtlpTraceDecompressionFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setName(DECOMPRESSION_FILTER_NAME);
        registration.addUrlPatterns(OTLP_HTTP_METRICS_PATH);
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 1);
        return registration;
    }
}
