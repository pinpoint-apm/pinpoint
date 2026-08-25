package com.pinpoint.test.micronautotel;

import io.micronaut.runtime.Micronaut;

/**
 * Micronaut app instrumented with {@code micronaut-tracing-opentelemetry-http} (OTel SDK autoconfigure, stable HTTP
 * semconv on the HTTP server and HTTP client spans). Spans go to Pinpoint through {@code otel.exporter.otlp.*}.
 * Do NOT attach the Pinpoint agent or the OpenTelemetry Java Agent to this app.
 */
public class MicronautOpenTelemetryTestApplication {

    public static void main(String[] args) {
        Micronaut.run(MicronautOpenTelemetryTestApplication.class, args);
    }
}
