package com.pinpoint.test.micrometertracing.config;

import io.opentelemetry.exporter.logging.otlp.OtlpJsonLoggingSpanExporter;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Adds a second OTLP/gRPC exporter that points to the Pinpoint collector.
 * <p>
 * Spring Boot collects every {@link SpanExporter} bean and wires them into one BatchSpanProcessor
 * (CompositeSpanExporter), so the existing backend keeps receiving the same spans.
 * <p>
 * The bean method deliberately returns {@link SpanExporter}, not {@link OtlpGrpcSpanExporter}:
 * Boot's auto-configured OTLP exporter is {@code @ConditionalOnMissingBean(OtlpGrpcSpanExporter, OtlpHttpSpanExporter)}
 * and would back off if this bean were declared with the concrete type.
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(PinpointTraceProperties.class)
@ConditionalOnProperty(prefix = "pinpoint.otel.trace", name = "enabled", havingValue = "true")
public class PinpointSpanExporterConfiguration {

    @Bean
    public SpanExporter pinpointSpanExporter(PinpointTraceProperties properties) {
        return OtlpGrpcSpanExporter.builder()
                .setEndpoint(properties.getEndpoint())
                .setTimeout(properties.getTimeout())
                .build();
    }

    /**
     * Debug aid: logs every exported span as OTLP JSON (logger io.opentelemetry.exporter.logging.otlp.OtlpJsonLoggingSpanExporter),
     * i.e. exactly what the Pinpoint collector receives.
     */
    @Bean
    @ConditionalOnProperty(prefix = "pinpoint.otel.trace", name = "debug", havingValue = "true")
    public SpanExporter otlpJsonLoggingSpanExporter() {
        return OtlpJsonLoggingSpanExporter.create();
    }
}
