package com.pinpoint.test.micrometertracing.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "pinpoint.otel.trace")
public class PinpointTraceProperties {

    /**
     * Switch for the Pinpoint exporter. Exporters share one batch queue, so an unreachable backend
     * delays the others as well; keep the exporter switchable.
     */
    private boolean enabled = false;

    /**
     * Pinpoint collector OTLP/gRPC endpoint.
     */
    private String endpoint = "http://localhost:9998";

    private Duration timeout = Duration.ofSeconds(10);

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public Duration getTimeout() {
        return timeout;
    }

    public void setTimeout(Duration timeout) {
        this.timeout = timeout;
    }
}
