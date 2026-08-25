package com.pinpoint.test.boot4otel;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot 4 app instrumented with {@code spring-boot-starter-opentelemetry} (Micrometer Observation -> OTel SDK).
 * <p>
 * Spans are produced by the OTel SDK that Spring Boot configures and exported to Pinpoint through an additional
 * {@code SpanExporter} bean. Do NOT attach the Pinpoint agent or the OpenTelemetry Java Agent to this app.
 */
@SpringBootApplication
public class Boot4OpenTelemetryTestApplication {

    public static void main(String[] args) {
        SpringApplication.run(Boot4OpenTelemetryTestApplication.class, args);
    }
}
