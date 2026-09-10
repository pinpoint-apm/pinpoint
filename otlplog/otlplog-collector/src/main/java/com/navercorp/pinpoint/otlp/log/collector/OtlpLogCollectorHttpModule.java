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
import com.navercorp.pinpoint.otlp.trace.collector.controller.OtlpTraceDecompressionFilter;
import com.navercorp.pinpoint.otlp.trace.collector.controller.OtlpTraceHttpAdmissionFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

/**
 * HTTP side of the log receiver: {@code POST /v1/logs} on the trace collector's servlet port, fronted
 * by its own instances of the trace path's admission and decompression filters. Same filter classes,
 * separate budgets and metrics ({@link OtlpLogIngestMetrics}), scoped to the logs path only.
 */
@Configuration
public class OtlpLogCollectorHttpModule {

    public static final String OTLP_HTTP_LOGS_PATH = "/v1/logs";

    // Explicit registration names: the servlet container keys filters by name and Spring Boot
    // deduces the name from the filter class when none is set. The trace module registers the
    // same two classes for /v1/traces, so without these names the second registration of each
    // class would be silently skipped ("possibly already registered").
    public static final String ADMISSION_FILTER_NAME = "otlpLogHttpAdmissionFilter";
    public static final String DECOMPRESSION_FILTER_NAME = "otlpLogDecompressionFilter";

    /**
     * Per-request size cap (413), concurrent-request and in-flight byte gates (503 + Retry-After) for
     * {@value #OTLP_HTTP_LOGS_PATH}, enforced before the protobuf body is buffered. Budgets are the
     * log receiver's own (smaller than the trace path's by default) and independent of the gRPC
     * log budget.
     */
    @Bean
    public FilterRegistrationBean<OtlpTraceHttpAdmissionFilter> otlpLogHttpAdmissionFilter(
            @Value("${pinpoint.collector.otlplog.http.max-request-bytes:4194304}") int maxRequestBytes,
            @Value("${pinpoint.collector.otlplog.http.admission.max-in-flight-bytes:67108864}") int maxInFlightBytes,
            @Value("${pinpoint.collector.otlplog.http.max-concurrent-requests:32}") int maxConcurrentRequests,
            @Value("${pinpoint.collector.otlplog.http.rejected.retry-after-seconds:1}") int retryAfterSeconds,
            OtlpLogIngestMetrics ingestMetrics) {
        OtlpTraceHttpAdmissionFilter filter =
                new OtlpTraceHttpAdmissionFilter(maxRequestBytes, maxInFlightBytes, maxConcurrentRequests, retryAfterSeconds, ingestMetrics);
        FilterRegistrationBean<OtlpTraceHttpAdmissionFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setName(ADMISSION_FILTER_NAME);
        registration.addUrlPatterns(OTLP_HTTP_LOGS_PATH);
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registration;
    }

    /**
     * Inflates {@code Content-Encoding: gzip} bodies for {@value #OTLP_HTTP_LOGS_PATH} with a
     * decompression-bomb cap; other encodings are refused with 415. Ordered right after the admission
     * filter so the compressed-size gates apply first.
     */
    @Bean
    public FilterRegistrationBean<OtlpTraceDecompressionFilter> otlpLogDecompressionFilter(
            @Value("${pinpoint.collector.otlplog.http.max-decompressed-request-bytes:16777216}") int maxDecompressedBytes,
            OtlpLogIngestMetrics ingestMetrics) {
        OtlpTraceDecompressionFilter filter = new OtlpTraceDecompressionFilter(maxDecompressedBytes, ingestMetrics);
        FilterRegistrationBean<OtlpTraceDecompressionFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setName(DECOMPRESSION_FILTER_NAME);
        registration.addUrlPatterns(OTLP_HTTP_LOGS_PATH);
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 1);
        return registration;
    }
}
