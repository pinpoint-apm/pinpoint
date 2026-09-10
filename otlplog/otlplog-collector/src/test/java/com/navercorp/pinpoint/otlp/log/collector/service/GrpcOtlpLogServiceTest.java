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
import com.navercorp.pinpoint.otlp.log.collector.OtlpLogRejectedRecords;
import com.navercorp.pinpoint.otlp.trace.collector.service.OtlpTransport;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.opentelemetry.proto.collector.logs.v1.ExportLogsServiceRequest;
import io.opentelemetry.proto.collector.logs.v1.ExportLogsServiceResponse;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;

import static com.navercorp.pinpoint.otlp.log.collector.LogRecordFixtures.SPAN_ID;
import static com.navercorp.pinpoint.otlp.log.collector.LogRecordFixtures.plainRecord;
import static com.navercorp.pinpoint.otlp.log.collector.LogRecordFixtures.resourceLogs;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GrpcOtlpLogServiceTest {

    private static final Executor DIRECT = Runnable::run;
    private static final Executor REJECTING = task -> {
        throw new RejectedExecutionException("queue full");
    };

    private final SimpleMeterRegistry registry = new SimpleMeterRegistry();
    private final OtlpLogIngestMetrics metrics = new OtlpLogIngestMetrics(registry);
    private final OtlpLogExportService exportService = mock(OtlpLogExportService.class);

    private static final class RecordingObserver implements StreamObserver<ExportLogsServiceResponse> {
        final List<ExportLogsServiceResponse> responses = new ArrayList<>();
        Throwable error;
        boolean completed;

        @Override
        public void onNext(ExportLogsServiceResponse value) {
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

    private static ExportLogsServiceRequest request(int records) {
        io.opentelemetry.proto.logs.v1.LogRecord[] array = new io.opentelemetry.proto.logs.v1.LogRecord[records];
        for (int i = 0; i < records; i++) {
            array[i] = plainRecord(SPAN_ID, "line-" + i).build();
        }
        return ExportLogsServiceRequest.newBuilder().addResourceLogs(resourceLogs(array)).build();
    }

    private static OtlpLogExportResult cleanResult() {
        return new OtlpLogExportResult(0, new OtlpLogRejectedRecords());
    }

    private double gauge(String name) {
        return registry.get(name).tag(OtlpLogIngestMetrics.TAG_TRANSPORT, "grpc").gauge().value();
    }

    private double requestRejected(String reason) {
        return registry.get(OtlpLogIngestMetrics.REQUEST_REJECTED)
                .tag(OtlpLogIngestMetrics.TAG_TRANSPORT, "grpc")
                .tag(OtlpLogIngestMetrics.TAG_REASON, reason)
                .counter().count();
    }

    @Test
    void cleanExport_completesWithEmptyResponse() {
        when(exportService.export(anyList(), eq(OtlpTransport.GRPC))).thenReturn(cleanResult());
        GrpcOtlpLogService service = new GrpcOtlpLogService(exportService, DIRECT, 1 << 20, metrics);
        RecordingObserver observer = new RecordingObserver();

        service.export(request(3), observer);

        assertThat(observer.completed).isTrue();
        assertThat(observer.error).isNull();
        assertThat(observer.responses).containsExactly(ExportLogsServiceResponse.getDefaultInstance());
        assertThat(gauge(OtlpLogIngestMetrics.ADMISSION_INFLIGHT_BYTES)).isZero();
    }

    @Test
    void clientVisibleRejects_becomePartialSuccess() {
        OtlpLogRejectedRecords rejected = new OtlpLogRejectedRecords();
        rejected.add(OtlpLogRejectReason.NO_TRACE_CONTEXT, 2);
        rejected.add(OtlpLogRejectReason.NO_EXCEPTION, 50); // receiver-side, must not be reported
        when(exportService.export(anyList(), eq(OtlpTransport.GRPC))).thenReturn(new OtlpLogExportResult(1, rejected));
        GrpcOtlpLogService service = new GrpcOtlpLogService(exportService, DIRECT, 1 << 20, metrics);
        RecordingObserver observer = new RecordingObserver();

        service.export(request(3), observer);

        assertThat(observer.completed).isTrue();
        assertThat(observer.responses).hasSize(1);
        assertThat(observer.responses.get(0).getPartialSuccess().getRejectedLogRecords()).isEqualTo(2);
        assertThat(observer.responses.get(0).getPartialSuccess().getErrorMessage()).isEqualTo("no trace context (2)");
    }

    @Test
    void admittedRequest_recordsWireBytes_andHoldsTheReservationWhileQueued() {
        List<Runnable> parked = new ArrayList<>();
        when(exportService.export(anyList(), eq(OtlpTransport.GRPC))).thenReturn(cleanResult());
        ExportLogsServiceRequest req = request(5);
        GrpcOtlpLogService service = new GrpcOtlpLogService(exportService, parked::add, 1 << 20, metrics);

        service.export(req, new RecordingObserver());

        assertThat(gauge(OtlpLogIngestMetrics.ADMISSION_LIMIT_BYTES)).isEqualTo((double) (1 << 20));
        assertThat(gauge(OtlpLogIngestMetrics.ADMISSION_INFLIGHT_BYTES)).isEqualTo((double) req.getSerializedSize());
        assertThat(registry.get(OtlpLogIngestMetrics.REQUEST_BYTES).tag(OtlpLogIngestMetrics.TAG_TRANSPORT, "grpc").summary().totalAmount())
                .isEqualTo((double) req.getSerializedSize());

        parked.forEach(Runnable::run);
        assertThat(gauge(OtlpLogIngestMetrics.ADMISSION_INFLIGHT_BYTES)).isZero();
    }

    @Test
    void inFlightBudgetExhausted_unavailable_counted_noBytesRecorded() {
        GrpcOtlpLogService service = new GrpcOtlpLogService(exportService, DIRECT, 1, metrics);
        RecordingObserver observer = new RecordingObserver();

        service.export(request(3), observer);

        assertThat(observer.error).isInstanceOf(StatusRuntimeException.class);
        assertThat(((StatusRuntimeException) observer.error).getStatus().getCode()).isEqualTo(Status.Code.UNAVAILABLE);
        assertThat(requestRejected("inflight_bytes")).isEqualTo(1.0);
        assertThat(registry.get(OtlpLogIngestMetrics.REQUEST_BYTES).tag(OtlpLogIngestMetrics.TAG_TRANSPORT, "grpc").summary().count()).isZero();
        verify(exportService, never()).export(anyList(), eq(OtlpTransport.GRPC));
    }

    @Test
    void workerRejected_unavailable_counted_reservationReleased() {
        GrpcOtlpLogService service = new GrpcOtlpLogService(exportService, REJECTING, 1 << 20, metrics);
        RecordingObserver observer = new RecordingObserver();

        service.export(request(3), observer);

        assertThat(((StatusRuntimeException) observer.error).getStatus().getCode()).isEqualTo(Status.Code.UNAVAILABLE);
        assertThat(requestRejected("executor_rejected")).isEqualTo(1.0);
        assertThat(gauge(OtlpLogIngestMetrics.ADMISSION_INFLIGHT_BYTES)).isZero();
    }

    @Test
    void unexpectedFailure_isInternal_notRetryable_reservationReleased() {
        when(exportService.export(anyList(), eq(OtlpTransport.GRPC))).thenThrow(new IllegalStateException("poison"));
        GrpcOtlpLogService service = new GrpcOtlpLogService(exportService, DIRECT, 1 << 20, metrics);
        RecordingObserver observer = new RecordingObserver();

        service.export(request(3), observer);

        assertThat(((StatusRuntimeException) observer.error).getStatus().getCode()).isEqualTo(Status.Code.INTERNAL);
        assertThat(gauge(OtlpLogIngestMetrics.ADMISSION_INFLIGHT_BYTES)).isZero();
    }
}
