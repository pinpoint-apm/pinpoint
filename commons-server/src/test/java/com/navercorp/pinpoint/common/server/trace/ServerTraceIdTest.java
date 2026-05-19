package com.navercorp.pinpoint.common.server.trace;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ServerTraceIdTest {

    @Test
    void isOpenTelemetry_otel() {
        ServerTraceId traceId = new OtelServerTraceId(new byte[16]);
        assertThat(ServerTraceId.isOpenTelemetry(traceId)).isTrue();
    }

    @Test
    void isOpenTelemetry_pinpoint() {
        ServerTraceId traceId = new PinpointServerTraceId("test", 1L, 2L);
        assertThat(ServerTraceId.isOpenTelemetry(traceId)).isFalse();
    }

    @Test
    void isOpenTelemetry_null() {
        assertThat(ServerTraceId.isOpenTelemetry(null)).isFalse();
    }
}
