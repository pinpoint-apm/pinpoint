package com.pinpoint.test.boot4otel.config;

import io.opentelemetry.exporter.logging.otlp.OtlpJsonLoggingSpanExporter;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Additional Pinpoint exporter. Boot collects every {@link SpanExporter} bean into one BatchSpanProcessor, so the
 * existing backend ({@code management.opentelemetry.tracing.export.otlp.endpoint}) and Pinpoint receive the same spans.
 * <p>
 * The bean type must be {@link SpanExporter}, not {@code OtlpGrpcSpanExporter}: the OTLP exporter auto-configured
 * by Boot is {@code @ConditionalOnMissingBean(OtlpGrpcSpanExporter, OtlpHttpSpanExporter)} and would back off otherwise.
 */
@Configuration
@EnableConfigurationProperties(PinpointTraceProperties.class)
public class PinpointSpanExporterConfiguration {

    @Bean
    @ConditionalOnProperty(name = "pinpoint.otel.trace.enabled", havingValue = "true", matchIfMissing = true)
    public SpanExporter pinpointSpanExporter(PinpointTraceProperties properties) {
        return OtlpGrpcSpanExporter.builder()
                .setEndpoint(properties.getEndpoint())
                .build();
    }

    @Bean
    @ConditionalOnProperty(name = "pinpoint.otel.trace.debug", havingValue = "true")
    public SpanExporter otlpJsonLoggingSpanExporter() {
        return OtlpJsonLoggingSpanExporter.create();
    }
}
