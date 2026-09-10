/*
 * Copyright 2026 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.otlp.log.collector.service;

import com.navercorp.pinpoint.otlp.log.collector.OtlpLogRejectReason;
import com.navercorp.pinpoint.otlp.trace.collector.service.OtlpRequestRejectReason;
import com.navercorp.pinpoint.otlp.trace.collector.service.OtlpTransport;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OtlpLogIngestMetricsTest {

    private final SimpleMeterRegistry registry = new SimpleMeterRegistry();
    private final OtlpLogIngestMetrics metrics = new OtlpLogIngestMetrics(registry);

    @Test
    void everyRecordSeries_isRegisteredAtZero_forBothTransports() {
        List<Meter> dropped = registry.get(OtlpLogIngestMetrics.RECORD_DROPPED).meters().stream().map(m -> (Meter) m).toList();
        assertThat(dropped).hasSize(OtlpTransport.values().length * OtlpLogRejectReason.values().length);

        for (OtlpTransport transport : OtlpTransport.values()) {
            assertThat(registry.get(OtlpLogIngestMetrics.RECORD_RECEIVED).tag("transport", transport.tagValue()).counter().count()).isZero();
            assertThat(registry.get(OtlpLogIngestMetrics.RECORD_STORED).tag("transport", transport.tagValue()).counter().count()).isZero();
            assertThat(registry.get(OtlpLogIngestMetrics.RECORD_UNSAMPLED_CONTEXT).tag("transport", transport.tagValue()).counter().count()).isZero();
            assertThat(registry.get(OtlpLogIngestMetrics.REQUEST_BYTES).tag("transport", transport.tagValue()).summary().count()).isZero();
        }
        assertThat(registry.get(OtlpLogIngestMetrics.STORE_ERROR).counter().count()).isZero();
    }

    @Test
    void namesLiveInTheOtlplogNamespace_notTheTraceOne() {
        assertThat(registry.getMeters()).allSatisfy(meter ->
                assertThat(meter.getId().getName()).startsWith("collector.otlplog."));
    }

    @Test
    void recordCounters_incrementByCount_ignoringNonPositive() {
        metrics.recordReceived(OtlpTransport.GRPC, 120);
        metrics.recordReceived(OtlpTransport.HTTP, 0);
        metrics.recordStored(OtlpTransport.GRPC, 2);
        metrics.recordUnsampledContext(OtlpTransport.GRPC, 1);
        metrics.recordDropped(OtlpTransport.GRPC, OtlpLogRejectReason.NO_EXCEPTION, 117);
        metrics.recordDropped(OtlpTransport.GRPC, OtlpLogRejectReason.DUPLICATE, 0);
        metrics.storeError();

        assertThat(registry.get(OtlpLogIngestMetrics.RECORD_RECEIVED).tag("transport", "grpc").counter().count()).isEqualTo(120.0);
        assertThat(registry.get(OtlpLogIngestMetrics.RECORD_RECEIVED).tag("transport", "http").counter().count()).isZero();
        assertThat(registry.get(OtlpLogIngestMetrics.RECORD_STORED).tag("transport", "grpc").counter().count()).isEqualTo(2.0);
        assertThat(registry.get(OtlpLogIngestMetrics.RECORD_UNSAMPLED_CONTEXT).tag("transport", "grpc").counter().count()).isEqualTo(1.0);
        assertThat(registry.get(OtlpLogIngestMetrics.RECORD_DROPPED).tag("transport", "grpc").tag("reason", "no_exception").counter().count()).isEqualTo(117.0);
        assertThat(registry.get(OtlpLogIngestMetrics.RECORD_DROPPED).tag("transport", "grpc").tag("reason", "duplicate").counter().count()).isZero();
        assertThat(registry.get(OtlpLogIngestMetrics.STORE_ERROR).counter().count()).isEqualTo(1.0);
    }

    @Test
    void requestRejected_onlyAcceptsReasonsTheTransportEmits() {
        metrics.requestRejected(OtlpTransport.GRPC, OtlpRequestRejectReason.INFLIGHT_BYTES);
        metrics.requestRejected(OtlpTransport.HTTP, OtlpRequestRejectReason.PARSE_ERROR);

        assertThat(registry.get(OtlpLogIngestMetrics.REQUEST_REJECTED).tag("transport", "grpc").tag("reason", "inflight_bytes").counter().count()).isEqualTo(1.0);
        assertThat(registry.get(OtlpLogIngestMetrics.REQUEST_REJECTED).tag("transport", "http").tag("reason", "parse_error").counter().count()).isEqualTo(1.0);
        assertThatThrownBy(() -> metrics.requestRejected(OtlpTransport.GRPC, OtlpRequestRejectReason.PAYLOAD_TOO_LARGE))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> metrics.requestRejected(OtlpTransport.HTTP, OtlpRequestRejectReason.EXECUTOR_REJECTED))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void admissionGauges_reflectTheSuppliers() {
        long[] reserved = {0};
        int[] requests = {0};
        metrics.registerInFlightBytes(OtlpTransport.HTTP, () -> reserved[0], 64L << 20);
        metrics.registerInFlightRequests(OtlpTransport.HTTP, () -> requests[0], 32);
        metrics.requestBytes(OtlpTransport.HTTP, 1_000);
        metrics.requestBytes(OtlpTransport.HTTP, 0); // ignored

        reserved[0] = 4096;
        requests[0] = 3;

        assertThat(registry.get(OtlpLogIngestMetrics.ADMISSION_INFLIGHT_BYTES).tag("transport", "http").gauge().value()).isEqualTo(4096.0);
        assertThat(registry.get(OtlpLogIngestMetrics.ADMISSION_LIMIT_BYTES).tag("transport", "http").gauge().value()).isEqualTo((double) (64L << 20));
        assertThat(registry.get(OtlpLogIngestMetrics.ADMISSION_INFLIGHT_REQUESTS).tag("transport", "http").gauge().value()).isEqualTo(3.0);
        assertThat(registry.get(OtlpLogIngestMetrics.ADMISSION_LIMIT_REQUESTS).tag("transport", "http").gauge().value()).isEqualTo(32.0);
        assertThat(registry.get(OtlpLogIngestMetrics.REQUEST_BYTES).tag("transport", "http").summary().count()).isEqualTo(1);
    }

    @Test
    void rejectReasonTagValues_areStable() {
        assertThat(OtlpLogRejectReason.NO_EXCEPTION.tagValue()).isEqualTo("no_exception");
        assertThat(OtlpLogRejectReason.BLACKLISTED.tagValue()).isEqualTo("blacklisted");
        assertThat(OtlpLogRejectReason.INVALID_RESOURCE.tagValue()).isEqualTo("invalid_resource");
        assertThat(OtlpLogRejectReason.NO_TRACE_CONTEXT.tagValue()).isEqualTo("no_trace_context");
        assertThat(OtlpLogRejectReason.UNSAMPLED_CONTEXT.tagValue()).isEqualTo("unsampled_context");
        assertThat(OtlpLogRejectReason.DUPLICATE.tagValue()).isEqualTo("duplicate");
        assertThat(OtlpLogRejectReason.NO_EXCEPTION_TYPE.tagValue()).isEqualTo("no_exception_type");
        assertThat(OtlpLogRejectReason.MAPPING_ERROR.tagValue()).isEqualTo("mapping_error");
        assertThat(OtlpLogRejectReason.STORAGE_UNAVAILABLE.tagValue()).isEqualTo("storage_unavailable");
    }

    @Test
    void clientVisibility_splitsClientFaultsFromReceiverSideDrops() {
        assertThat(OtlpLogRejectReason.values()).filteredOn(OtlpLogRejectReason::isClientVisible)
                .containsExactlyInAnyOrder(OtlpLogRejectReason.INVALID_RESOURCE, OtlpLogRejectReason.NO_TRACE_CONTEXT,
                        OtlpLogRejectReason.NO_EXCEPTION_TYPE, OtlpLogRejectReason.MAPPING_ERROR);
    }
}
