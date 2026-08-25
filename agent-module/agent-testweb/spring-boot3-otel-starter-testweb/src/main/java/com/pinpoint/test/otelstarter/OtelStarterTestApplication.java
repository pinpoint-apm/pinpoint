package com.pinpoint.test.otelstarter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot 3 app instrumented with the OpenTelemetry Spring Boot Starter (OTel SDK autoconfigure, no javaagent).
 * <p>
 * The starter's own exporter is configured with {@code otel.exporter.otlp.*}; Pinpoint is added as a second exporter
 * through an {@code AutoConfigurationCustomizerProvider} bean. Do NOT attach the Pinpoint agent or the
 * OpenTelemetry Java Agent to this app.
 */
@SpringBootApplication
public class OtelStarterTestApplication {

    public static void main(String[] args) {
        SpringApplication.run(OtelStarterTestApplication.class, args);
    }
}
