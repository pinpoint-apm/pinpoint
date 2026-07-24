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

package com.navercorp.pinpoint.otlp.trace.collector;

import com.navercorp.pinpoint.otlp.trace.collector.controller.OtlpTraceDecompressionFilter;
import com.navercorp.pinpoint.otlp.trace.collector.controller.OtlpTraceHttpAdmissionFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.protobuf.ProtobufHttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class OtlpTraceCollectorHttpModule implements WebMvcConfigurer {

    public static final String OTLP_HTTP_TRACES_PATH = "/v1/traces";

    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        ProtobufHttpMessageConverter protobufHttpMessageConverter = new ProtobufHttpMessageConverter();
        converters.add(protobufHttpMessageConverter);
    }

    /**
     * Admission control for OTLP/HTTP trace ingestion, scoped to {@value #OTLP_HTTP_TRACES_PATH} so
     * the actuator/management endpoints on the same port are unaffected. Runs first (highest
     * precedence) to reject before the protobuf body is buffered/parsed. Mirrors the gRPC path's
     * in-flight byte budget; kept as an independent (HTTP-only) budget for now.
     */
    @Bean
    public FilterRegistrationBean<OtlpTraceHttpAdmissionFilter> otlpTraceHttpAdmissionFilter(
            @Value("${pinpoint.collector.otlptrace.http.max-request-bytes:4194304}") int maxRequestBytes,
            @Value("${pinpoint.collector.otlptrace.http.admission.max-in-flight-bytes:268435456}") int maxInFlightBytes,
            @Value("${pinpoint.collector.otlptrace.http.max-concurrent-requests:64}") int maxConcurrentRequests,
            @Value("${pinpoint.collector.otlptrace.http.rejected.retry-after-seconds:1}") int retryAfterSeconds) {
        OtlpTraceHttpAdmissionFilter filter =
                new OtlpTraceHttpAdmissionFilter(maxRequestBytes, maxInFlightBytes, maxConcurrentRequests, retryAfterSeconds);
        FilterRegistrationBean<OtlpTraceHttpAdmissionFilter> registration = new FilterRegistrationBean<>(filter);
        registration.addUrlPatterns(OTLP_HTTP_TRACES_PATH);
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registration;
    }

    /**
     * Decompresses {@code Content-Encoding: gzip} bodies for {@value #OTLP_HTTP_TRACES_PATH} (the
     * OTel {@code otlphttp} exporter's default). Ordered just after the admission filter so the
     * compressed-size gates apply to the raw request first, then the inflated size is capped to guard
     * against decompression bombs.
     */
    @Bean
    public FilterRegistrationBean<OtlpTraceDecompressionFilter> otlpTraceDecompressionFilter(
            @Value("${pinpoint.collector.otlptrace.http.max-decompressed-request-bytes:16777216}") int maxDecompressedBytes) {
        OtlpTraceDecompressionFilter filter = new OtlpTraceDecompressionFilter(maxDecompressedBytes);
        FilterRegistrationBean<OtlpTraceDecompressionFilter> registration = new FilterRegistrationBean<>(filter);
        registration.addUrlPatterns(OTLP_HTTP_TRACES_PATH);
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 1);
        return registration;
    }
}