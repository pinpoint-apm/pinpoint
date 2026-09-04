/*
 * Copyright 2026 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.otlp.trace.collector.service;

import com.navercorp.pinpoint.otlp.trace.collector.OtlpTraceRejectReason;
import com.navercorp.pinpoint.otlp.trace.collector.service.OtlpTraceIngestMetrics.RequestRejectReason;
import com.navercorp.pinpoint.otlp.trace.collector.service.OtlpTraceIngestMetrics.Transport;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OtlpTraceIngestMetricsTest {

    private final SimpleMeterRegistry registry = new SimpleMeterRegistry();
    private final OtlpTraceIngestMetrics metrics = new OtlpTraceIngestMetrics(registry);

    private double counter(String name, String... tags) {
        return registry.get(name).tags(tags).counter().count();
    }

    @Test
    void allSeries_arePreRegisteredAtZero() {
        // Dashboards repeating on the reason tag rely on every combination existing before the
        // first event, so a reason that never fired still renders as a flat zero line.
        List<Counter> spanRejected = registry.get(OtlpTraceIngestMetrics.SPAN_REJECTED).counters().stream().toList();
        assertThat(spanRejected).hasSize(Transport.values().length * OtlpTraceRejectReason.values().length);
        assertThat(spanRejected).allSatisfy(c -> assertThat(c.count()).isZero());

        assertThat(registry.get(OtlpTraceIngestMetrics.SPAN_RECEIVED).counters()).hasSize(2);
        assertThat(registry.get(OtlpTraceIngestMetrics.SPAN_STORED).counters()).hasSize(4);
        // grpc: inflight_bytes, executor_rejected / http: inflight_bytes, concurrency, payload_too_large,
        // unsupported_encoding, parse_error
        assertThat(registry.get(OtlpTraceIngestMetrics.REQUEST_REJECTED).counters()).hasSize(7);
    }

    @Test
    void spanCounters_incrementByCount_perTransport() {
        metrics.spanReceived(Transport.GRPC, 120);
        metrics.spanReceived(Transport.HTTP, 5);
        metrics.spanStored(Transport.GRPC, 30);
        metrics.spanChunkStored(Transport.GRPC, 2);
        metrics.spanRejected(Transport.GRPC, OtlpTraceRejectReason.ORPHAN, 4);

        assertThat(counter(OtlpTraceIngestMetrics.SPAN_RECEIVED, "transport", "grpc")).isEqualTo(120.0);
        assertThat(counter(OtlpTraceIngestMetrics.SPAN_RECEIVED, "transport", "http")).isEqualTo(5.0);
        assertThat(counter(OtlpTraceIngestMetrics.SPAN_STORED, "transport", "grpc", "type", "span")).isEqualTo(30.0);
        assertThat(counter(OtlpTraceIngestMetrics.SPAN_STORED, "transport", "grpc", "type", "spanChunk")).isEqualTo(2.0);
        assertThat(counter(OtlpTraceIngestMetrics.SPAN_REJECTED, "transport", "grpc", "reason", "orphan")).isEqualTo(4.0);
        assertThat(counter(OtlpTraceIngestMetrics.SPAN_REJECTED, "transport", "http", "reason", "orphan")).isZero();
    }

    @Test
    void zeroCounts_leaveCountersUntouched() {
        metrics.spanReceived(Transport.GRPC, 0);
        metrics.spanRejected(Transport.HTTP, OtlpTraceRejectReason.INVALID_ID, 0);

        assertThat(counter(OtlpTraceIngestMetrics.SPAN_RECEIVED, "transport", "grpc")).isZero();
        assertThat(counter(OtlpTraceIngestMetrics.SPAN_REJECTED, "transport", "http", "reason", "invalid_id")).isZero();
    }

    @Test
    void requestRejected_countsOnePerCall_withTransportSpecificReasons() {
        metrics.requestRejected(Transport.GRPC, RequestRejectReason.INFLIGHT_BYTES);
        metrics.requestRejected(Transport.GRPC, RequestRejectReason.INFLIGHT_BYTES);
        metrics.requestRejected(Transport.HTTP, RequestRejectReason.PAYLOAD_TOO_LARGE);

        assertThat(counter(OtlpTraceIngestMetrics.REQUEST_REJECTED, "transport", "grpc", "reason", "inflight_bytes")).isEqualTo(2.0);
        assertThat(counter(OtlpTraceIngestMetrics.REQUEST_REJECTED, "transport", "http", "reason", "payload_too_large")).isEqualTo(1.0);
        assertThat(counter(OtlpTraceIngestMetrics.REQUEST_REJECTED, "transport", "http", "reason", "inflight_bytes")).isZero();
    }

    @Test
    void requestRejected_reasonNotEmittedByTransport_failsFast() {
        // gRPC has no payload-size or encoding gate of its own; wiring such a pair is a bug.
        assertThatThrownBy(() -> metrics.requestRejected(Transport.GRPC, RequestRejectReason.PAYLOAD_TOO_LARGE))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> metrics.requestRejected(Transport.HTTP, RequestRejectReason.EXECUTOR_REJECTED))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void requestBytes_recordsDistributionPerTransport() {
        metrics.requestBytes(Transport.GRPC, 1_000);
        metrics.requestBytes(Transport.GRPC, 3_000);
        metrics.requestBytes(Transport.HTTP, 0); // ignored

        DistributionSummary grpc = registry.get(OtlpTraceIngestMetrics.REQUEST_BYTES).tag("transport", "grpc").summary();
        assertThat(grpc.count()).isEqualTo(2);
        assertThat(grpc.totalAmount()).isEqualTo(4_000.0);
        assertThat(grpc.max()).isEqualTo(3_000.0);
        assertThat(registry.get(OtlpTraceIngestMetrics.REQUEST_BYTES).tag("transport", "http").summary().count()).isZero();
    }

    @Test
    void inFlightGauges_trackSupplierAndLimit() {
        long[] reserved = {0};
        int[] requests = {0};
        metrics.registerInFlightBytes(Transport.HTTP, () -> reserved[0], 256L << 20);
        metrics.registerInFlightRequests(Transport.HTTP, () -> requests[0], 64);

        reserved[0] = 12_345;
        requests[0] = 7;

        assertThat(registry.get(OtlpTraceIngestMetrics.ADMISSION_INFLIGHT_BYTES).tag("transport", "http").gauge().value()).isEqualTo(12_345.0);
        assertThat(registry.get(OtlpTraceIngestMetrics.ADMISSION_LIMIT_BYTES).tag("transport", "http").gauge().value()).isEqualTo((double) (256L << 20));
        assertThat(registry.get(OtlpTraceIngestMetrics.ADMISSION_INFLIGHT_REQUESTS).tag("transport", "http").gauge().value()).isEqualTo(7.0);
        assertThat(registry.get(OtlpTraceIngestMetrics.ADMISSION_LIMIT_REQUESTS).tag("transport", "http").gauge().value()).isEqualTo(64.0);
    }

    @Test
    void tagValues_areStableSnakeCase() {
        assertThat(Transport.GRPC.tagValue()).isEqualTo("grpc");
        assertThat(Transport.HTTP.tagValue()).isEqualTo("http");
        assertThat(RequestRejectReason.INFLIGHT_BYTES.tagValue()).isEqualTo("inflight_bytes");
        assertThat(RequestRejectReason.EXECUTOR_REJECTED.tagValue()).isEqualTo("executor_rejected");
        assertThat(RequestRejectReason.CONCURRENCY.tagValue()).isEqualTo("concurrency");
        assertThat(RequestRejectReason.PAYLOAD_TOO_LARGE.tagValue()).isEqualTo("payload_too_large");
        assertThat(RequestRejectReason.UNSUPPORTED_ENCODING.tagValue()).isEqualTo("unsupported_encoding");
        assertThat(RequestRejectReason.PARSE_ERROR.tagValue()).isEqualTo("parse_error");
    }
}
