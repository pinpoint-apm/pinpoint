package com.pinpoint.test.micrometertracing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot app that is already instrumented with micrometer-tracing (OpenTelemetry bridge).
 * <p>
 * Spans are produced by Spring Boot's own OTel SDK and exported to Pinpoint through an additional
 * {@code SpanExporter} bean. Do NOT attach the Pinpoint agent or the OpenTelemetry Java Agent to this app.
 */
@SpringBootApplication
public class MicrometerTracingTestApplication {

    public static void main(String[] args) {
        SpringApplication.run(MicrometerTracingTestApplication.class, args);
    }
}
