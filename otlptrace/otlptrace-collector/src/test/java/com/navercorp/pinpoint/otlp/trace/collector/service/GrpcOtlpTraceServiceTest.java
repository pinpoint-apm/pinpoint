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

import com.navercorp.pinpoint.otlp.trace.collector.OtlpTraceCollectorRejectedSpan;
import com.navercorp.pinpoint.otlp.trace.collector.service.OtlpTraceIngestMetrics.Transport;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceRequest;
import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceResponse;
import io.opentelemetry.proto.trace.v1.ResourceSpans;
import io.opentelemetry.proto.trace.v1.ScopeSpans;
import io.opentelemetry.proto.trace.v1.Span;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GrpcOtlpTraceServiceTest {

    private static final Executor DIRECT = Runnable::run;
    private static final Executor REJECTING = task -> {
        throw new RejectedExecutionException("queue full");
    };

    private final SimpleMeterRegistry registry = new SimpleMeterRegistry();
    private final OtlpTraceIngestMetrics metrics = new OtlpTraceIngestMetrics(registry);
    private final OtlpTraceExportService exportService = mock(OtlpTraceExportService.class);

    /** Captures the response so the transport outcome (status / completion) can be asserted. */
    private static final class RecordingObserver implements StreamObserver<ExportTraceServiceResponse> {
        final List<ExportTraceServiceResponse> responses = new ArrayList<>();
        Throwable error;
        boolean completed;

        @Override
        public void onNext(ExportTraceServiceResponse value) {
            responses.add(value);
        }

        @Override
        public void onError(Throwable t) {
            error = t;
        }

        @Override
        public void onCompleted() {
            completed = true;
        }
    }

    private static ExportTraceServiceRequest request(int spans) {
        ScopeSpans.Builder scope = ScopeSpans.newBuilder();
        for (int i = 0; i < spans; i++) {
            scope.addSpans(Span.newBuilder().setName("span-" + i));
        }
        return ExportTraceServiceRequest.newBuilder()
                .addResourceSpans(ResourceSpans.newBuilder().addScopeSpans(scope))
                .build();
    }

    private double gauge(String name) {
        return registry.get(name).tag(OtlpTraceIngestMetrics.TAG_TRANSPORT, "grpc").gauge().value();
    }

    @Test
    void admittedRequest_recordsWireBytes_andInFlightGaugeWhileQueued() {
        // An executor that never runs the task keeps the request "in flight" (permit held).
        List<Runnable> parked = new ArrayList<>();
        Executor parking = parked::add;
        when(exportService.export(anyList(), eq(Transport.GRPC)))
                .thenReturn(new OtlpTraceExportResult(new OtlpTraceCollectorRejectedSpan(), 0, ""));
        ExportTraceServiceRequest req = request(5);
        GrpcOtlpTraceService service = new GrpcOtlpTraceService(exportService, parking, 1 << 20, metrics);

        service.export(req, new RecordingObserver());

        assertThat(gauge(OtlpTraceIngestMetrics.ADMISSION_LIMIT_BYTES)).isEqualTo((double) (1 << 20));
        assertThat(gauge(OtlpTraceIngestMetrics.ADMISSION_INFLIGHT_BYTES)).isEqualTo((double) req.getSerializedSize());
        DistributionSummary bytes = registry.get(OtlpTraceIngestMetrics.REQUEST_BYTES).tag(OtlpTraceIngestMetrics.TAG_TRANSPORT, "grpc").summary();
        assertThat(bytes.count()).isEqualTo(1);
        assertThat(bytes.totalAmount()).isEqualTo((double) req.getSerializedSize());

        // Running the parked task releases the reservation.
        parked.forEach(Runnable::run);
        assertThat(gauge(OtlpTraceIngestMetrics.ADMISSION_INFLIGHT_BYTES)).isZero();
    }

    @Test
    void rejectedByAdmission_doesNotRecordWireBytes() {
        GrpcOtlpTraceService service = new GrpcOtlpTraceService(exportService, DIRECT, 1, metrics);

        service.export(request(3), new RecordingObserver());

        assertThat(registry.get(OtlpTraceIngestMetrics.REQUEST_BYTES).tag(OtlpTraceIngestMetrics.TAG_TRANSPORT, "grpc").summary().count()).isZero();
        assertThat(gauge(OtlpTraceIngestMetrics.ADMISSION_INFLIGHT_BYTES)).isZero();
    }

    private double requestRejected(String reason) {
        return registry.get(OtlpTraceIngestMetrics.REQUEST_REJECTED)
                .tag(OtlpTraceIngestMetrics.TAG_TRANSPORT, "grpc")
                .tag(OtlpTraceIngestMetrics.TAG_REASON, reason)
                .counter().count();
    }

    @Test
    void inFlightBudgetExhausted_unavailable_countsInflightBytes() {
        // Budget of 1 byte: any real request is larger than the permit pool.
        GrpcOtlpTraceService service = new GrpcOtlpTraceService(exportService, DIRECT, 1, metrics);
        RecordingObserver observer = new RecordingObserver();

        service.export(request(3), observer);

        assertThat(Status.fromThrowable(observer.error).getCode()).isEqualTo(Status.Code.UNAVAILABLE);
        assertThat(observer.completed).isFalse();
        assertThat(requestRejected("inflight_bytes")).isEqualTo(1.0);
        assertThat(requestRejected("executor_rejected")).isZero();
        verify(exportService, never()).export(anyList(), eq(Transport.GRPC));
    }

    @Test
    void workerExecutorRejects_unavailable_countsExecutorRejected_andReleasesPermit() {
        ExportTraceServiceRequest req = request(3);
        // Budget exactly one request: if the permit were leaked on rejection, the second call would
        // fail with inflight_bytes instead of executor_rejected.
        GrpcOtlpTraceService service = new GrpcOtlpTraceService(exportService, REJECTING, req.getSerializedSize(), metrics);

        RecordingObserver first = new RecordingObserver();
        service.export(req, first);
        RecordingObserver second = new RecordingObserver();
        service.export(req, second);

        assertThat(Status.fromThrowable(first.error).getCode()).isEqualTo(Status.Code.UNAVAILABLE);
        assertThat(Status.fromThrowable(second.error).getCode()).isEqualTo(Status.Code.UNAVAILABLE);
        assertThat(requestRejected("executor_rejected")).isEqualTo(2.0);
        assertThat(requestRejected("inflight_bytes")).isZero();
        verify(exportService, never()).export(anyList(), eq(Transport.GRPC));
    }

    @Test
    void accepted_exportsWithGrpcTransport_andCountsNoRequestRejection() {
        when(exportService.export(anyList(), eq(Transport.GRPC)))
                .thenReturn(new OtlpTraceExportResult(new OtlpTraceCollectorRejectedSpan(), 0, ""));
        GrpcOtlpTraceService service = new GrpcOtlpTraceService(exportService, DIRECT, 1 << 20, metrics);
        RecordingObserver observer = new RecordingObserver();

        service.export(request(3), observer);

        assertThat(observer.error).isNull();
        assertThat(observer.completed).isTrue();
        assertThat(observer.responses).hasSize(1);
        verify(exportService).export(anyList(), eq(Transport.GRPC));
        assertThat(requestRejected("inflight_bytes")).isZero();
        assertThat(requestRejected("executor_rejected")).isZero();
    }

    private void assertNoRequestRejection() {
        assertThat(registry.get(OtlpTraceIngestMetrics.REQUEST_REJECTED).counters())
                .allSatisfy(c -> assertThat(c.count()).isZero());
    }

    @Test
    void serverErrorResult_unavailable_isNotARequestRejection_andReleasesPermit() {
        // Storage-side failures are reported by the insert error counters, not request.rejected.
        when(exportService.export(anyList(), eq(Transport.GRPC)))
                .thenReturn(new OtlpTraceExportResult(new OtlpTraceCollectorRejectedSpan(), 3, "insert error (3)"));
        GrpcOtlpTraceService service = new GrpcOtlpTraceService(exportService, DIRECT, 1 << 20, metrics);
        RecordingObserver observer = new RecordingObserver();

        service.export(request(3), observer);

        assertThat(Status.fromThrowable(observer.error).getCode()).isEqualTo(Status.Code.UNAVAILABLE);
        assertThat(observer.completed).isFalse();
        assertNoRequestRejection();
        assertThat(gauge(OtlpTraceIngestMetrics.ADMISSION_INFLIGHT_BYTES)).isZero();
    }

    @Test
    void exportThrows_internal_isNotARequestRejection_andReleasesPermit() {
        ExportTraceServiceRequest req = request(3);
        when(exportService.export(anyList(), eq(Transport.GRPC)))
                .thenThrow(new IllegalStateException("mapper fault"))
                .thenReturn(new OtlpTraceExportResult(new OtlpTraceCollectorRejectedSpan(), 0, ""));
        // Budget of exactly one request: a permit leaked by the failing call would turn the second
        // call into an inflight_bytes rejection instead of a clean success.
        GrpcOtlpTraceService service = new GrpcOtlpTraceService(exportService, DIRECT, req.getSerializedSize(), metrics);

        RecordingObserver failed = new RecordingObserver();
        service.export(req, failed);
        RecordingObserver next = new RecordingObserver();
        service.export(req, next);

        assertThat(Status.fromThrowable(failed.error).getCode()).isEqualTo(Status.Code.INTERNAL);
        assertThat(next.error).isNull();
        assertThat(next.completed).isTrue();
        assertNoRequestRejection();
        assertThat(gauge(OtlpTraceIngestMetrics.ADMISSION_INFLIGHT_BYTES)).isZero();
        // Both calls were admitted, so both are measured.
        assertThat(registry.get(OtlpTraceIngestMetrics.REQUEST_BYTES).tag(OtlpTraceIngestMetrics.TAG_TRANSPORT, "grpc").summary().count()).isEqualTo(2);
    }
}
