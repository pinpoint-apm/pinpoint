package com.pinpoint.test.otelstarter.config;

import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.sdk.autoconfigure.spi.AutoConfigurationCustomizerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Adds a Pinpoint OTLP/gRPC exporter next to the exporter configured by {@code otel.exporter.otlp.*}.
 * The starter picks up every {@link AutoConfigurationCustomizerProvider} bean, so both backends receive the same spans.
 */
@Configuration
public class PinpointExporterConfiguration {

    @Bean
    @ConditionalOnProperty(name = "pinpoint.otel.trace.enabled", havingValue = "true", matchIfMissing = true)
    public AutoConfigurationCustomizerProvider pinpointExporterCustomizer() {
        return customizer -> customizer.addTracerProviderCustomizer((builder, config) -> {
            String endpoint = config.getString("pinpoint.otel.trace.endpoint", "http://localhost:9998");
            OtlpGrpcSpanExporter exporter = OtlpGrpcSpanExporter.builder().setEndpoint(endpoint).build();
            return builder.addSpanProcessor(BatchSpanProcessor.builder(exporter).build());
        });
    }
}
