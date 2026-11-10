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

package com.navercorp.pinpoint.otlp.trace.collector.inspector.service;

import com.navercorp.pinpoint.otlp.trace.collector.service.OtlpTransport;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.opentelemetry.proto.collector.metrics.v1.ExportMetricsServiceRequest;
import io.opentelemetry.proto.collector.metrics.v1.ExportMetricsServiceResponse;
import io.opentelemetry.proto.metrics.v1.ResourceMetrics;
import io.opentelemetry.proto.resource.v1.Resource;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GrpcOtlpMetricServiceTest {

    private static final Executor DIRECT = Runnable::run;

    private final SimpleMeterRegistry registry = new SimpleMeterRegistry();
    private final OtlpMetricIngestMetrics metrics = new OtlpMetricIngestMetrics(registry);
    private final OtlpMetricExportService exportService = mock(OtlpMetricExportService.class);

    private static final class RecordingObserver implements StreamObserver<ExportMetricsServiceResponse> {
        final List<ExportMetricsServiceResponse> responses = new ArrayList<>();
        Throwable error;
        boolean completed;

        @Override
        public void onNext(ExportMetricsServiceResponse value) {
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

    private static ExportMetricsServiceRequest request() {
        return ExportMetricsServiceRequest.newBuilder()
                .addResourceMetrics(ResourceMetrics.newBuilder().setResource(Resource.newBuilder()))
                .build();
    }

    private static OtlpMetricExportResult result(Map<OtlpMetricResourceRejectReason, Long> rejected) {
        return new OtlpMetricExportResult(1, rejected);
    }

    @Test
    void cleanExport_completesWithEmptyResponse() {
        when(exportService.export(anyList(), eq(OtlpTransport.GRPC))).thenReturn(result(new EnumMap<>(OtlpMetricResourceRejectReason.class)));
        GrpcOtlpMetricService service = new GrpcOtlpMetricService(exportService, DIRECT, 1 << 20, metrics);
        RecordingObserver observer = new RecordingObserver();

        service.export(request(), observer);

        assertThat(observer.completed).isTrue();
        assertThat(observer.error).isNull();
        assertThat(observer.responses).hasSize(1);
        assertThat(observer.responses.get(0).hasPartialSuccess()).isFalse();
    }

    @Test
    void rejectedResources_areReportedAsPartialSuccess() {
        Map<OtlpMetricResourceRejectReason, Long> rejected = new EnumMap<>(OtlpMetricResourceRejectReason.class);
        rejected.put(OtlpMetricResourceRejectReason.INVALID_RESOURCE, 5L);
        when(exportService.export(anyList(), eq(OtlpTransport.GRPC))).thenReturn(result(rejected));
        GrpcOtlpMetricService service = new GrpcOtlpMetricService(exportService, DIRECT, 1 << 20, metrics);
        RecordingObserver observer = new RecordingObserver();

        service.export(request(), observer);

        assertThat(observer.responses.get(0).getPartialSuccess().getRejectedDataPoints()).isEqualTo(5);
        assertThat(observer.responses.get(0).getPartialSuccess().getErrorMessage()).isEqualTo("invalid resource (5)");
    }

    @Test
    void budgetExhausted_isUnavailable_andReleasesNothing() {
        GrpcOtlpMetricService service = new GrpcOtlpMetricService(exportService, DIRECT, 1, metrics);
        RecordingObserver observer = new RecordingObserver();

        service.export(request(), observer);

        assertThat(observer.error).isInstanceOf(StatusRuntimeException.class);
        assertThat(((StatusRuntimeException) observer.error).getStatus().getCode()).isEqualTo(Status.Code.UNAVAILABLE);
        verify(exportService, never()).export(anyList(), eq(OtlpTransport.GRPC));
        assertThat(registry.get(OtlpMetricIngestMetrics.REQUEST_REJECTED).tag("reason", "inflight_bytes").counter().count()).isEqualTo(1);
    }

    @Test
    void executorRejected_isUnavailable_andReleasesTheBudget() {
        Executor rejecting = task -> {
            throw new RejectedExecutionException("full");
        };
        GrpcOtlpMetricService service = new GrpcOtlpMetricService(exportService, rejecting, 1 << 20, metrics);
        RecordingObserver observer = new RecordingObserver();

        service.export(request(), observer);

        assertThat(((StatusRuntimeException) observer.error).getStatus().getCode()).isEqualTo(Status.Code.UNAVAILABLE);
        assertThat(registry.get(OtlpMetricIngestMetrics.ADMISSION_INFLIGHT_BYTES).gauge().value()).isZero();
        assertThat(registry.get(OtlpMetricIngestMetrics.REQUEST_REJECTED).tag("reason", "executor_rejected").counter().count()).isEqualTo(1);
    }

    @Test
    void unexpectedFailure_isInternal_andReleasesTheBudget() {
        when(exportService.export(anyList(), eq(OtlpTransport.GRPC))).thenThrow(new IllegalStateException("boom"));
        GrpcOtlpMetricService service = new GrpcOtlpMetricService(exportService, DIRECT, 1 << 20, metrics);
        RecordingObserver observer = new RecordingObserver();

        service.export(request(), observer);

        assertThat(((StatusRuntimeException) observer.error).getStatus().getCode()).isEqualTo(Status.Code.INTERNAL);
        assertThat(registry.get(OtlpMetricIngestMetrics.ADMISSION_INFLIGHT_BYTES).gauge().value()).isZero();
    }
}
