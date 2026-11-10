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

import com.navercorp.pinpoint.otlp.trace.collector.service.OtlpRequestRejectReason;
import com.navercorp.pinpoint.otlp.trace.collector.service.OtlpTransport;
import io.grpc.Context;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import io.opentelemetry.proto.collector.metrics.v1.ExportMetricsServiceRequest;
import io.opentelemetry.proto.collector.metrics.v1.ExportMetricsServiceResponse;
import io.opentelemetry.proto.collector.metrics.v1.MetricsServiceGrpc;
import io.opentelemetry.proto.metrics.v1.ResourceMetrics;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.Semaphore;

/**
 * OTLP {@code MetricsService/Export}. Same shape as the trace and log services: reserve the
 * request's wire size from the metrics receiver's own in-flight byte budget, offload the map/store
 * work to the metrics worker pool, answer with the OTLP response. Both refusals are
 * {@code UNAVAILABLE} because OTLP exporters retry it unconditionally, so saturation is back-pressure
 * rather than silent loss.
 */
public class GrpcOtlpMetricService extends MetricsServiceGrpc.MetricsServiceImplBase {

    private static final Status EXECUTOR_REJECTED = Status.UNAVAILABLE.withDescription("Executor rejected");
    private static final Status ADMISSION_REJECTED = Status.UNAVAILABLE.withDescription("In-flight byte budget exhausted");

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final OtlpMetricExportService exportService;
    private final Executor workerExecutor;
    private final Semaphore admissionBytes;
    private final int maxInFlightBytes;
    private final OtlpMetricIngestMetrics ingestMetrics;

    public GrpcOtlpMetricService(OtlpMetricExportService exportService, Executor workerExecutor, int maxInFlightBytes,
                                 OtlpMetricIngestMetrics ingestMetrics) {
        this.exportService = Objects.requireNonNull(exportService, "exportService");
        this.workerExecutor = Objects.requireNonNull(workerExecutor, "workerExecutor");
        this.ingestMetrics = Objects.requireNonNull(ingestMetrics, "ingestMetrics");
        this.maxInFlightBytes = maxInFlightBytes;
        this.admissionBytes = new Semaphore(maxInFlightBytes);
        ingestMetrics.registerInFlightBytes(OtlpTransport.GRPC,
                () -> (long) maxInFlightBytes - admissionBytes.availablePermits(), maxInFlightBytes);
    }

    @Override
    public void export(ExportMetricsServiceRequest request, StreamObserver<ExportMetricsServiceResponse> responseObserver) {
        // The request is already <= the server's inbound_message_size_max (shared with the trace
        // service); this gate bounds the total parsed bytes in flight for metrics alone.
        final int requestBytes = request.getSerializedSize();
        if (!admissionBytes.tryAcquire(requestBytes)) {
            logger.warn("Failed to export metrics. In-flight byte budget exhausted. requestBytes={}, budget={}", requestBytes, maxInFlightBytes);
            ingestMetrics.requestRejected(OtlpTransport.GRPC, OtlpRequestRejectReason.INFLIGHT_BYTES);
            safeOnError(responseObserver, ADMISSION_REJECTED);
            return;
        }

        ingestMetrics.requestBytes(OtlpTransport.GRPC, requestBytes);

        final List<ResourceMetrics> resourceMetricsList = request.getResourceMetricsList();
        final Context current = Context.current();
        final Runnable exportTask = current.wrap(() -> {
            try {
                if (Context.current().isCancelled()) {
                    // Client gave up (deadline / cancel) before the task ran; skip the wasted work.
                    return;
                }
                final OtlpMetricExportResult result = exportService.export(resourceMetricsList, OtlpTransport.GRPC);
                safeComplete(responseObserver, OtlpMetricResponseMapper.toResponse(result));
            } catch (Throwable t) {
                // INTERNAL is non-retryable: a deterministic (poison-data) fault must not retry-storm.
                logger.warn("Unexpected error while exporting otlp metrics", t);
                safeOnError(responseObserver, Status.INTERNAL.withDescription("export failed"));
            } finally {
                admissionBytes.release(requestBytes);
            }
        });
        try {
            workerExecutor.execute(exportTask);
        } catch (RejectedExecutionException e) {
            admissionBytes.release(requestBytes);
            logger.warn("Failed to export metrics. Worker executor rejected.");
            ingestMetrics.requestRejected(OtlpTransport.GRPC, OtlpRequestRejectReason.EXECUTOR_REJECTED);
            safeOnError(responseObserver, EXECUTOR_REJECTED);
        }
    }

    // The client may have closed the call (cancel / deadline) while the worker ran; a dead call must
    // not surface as a noisy error.
    private void safeOnError(StreamObserver<ExportMetricsServiceResponse> responseObserver, Status status) {
        try {
            responseObserver.onError(status.asRuntimeException());
        } catch (IllegalStateException e) {
            logger.debug("Response already closed (onError); call likely cancelled/expired");
        }
    }

    private void safeComplete(StreamObserver<ExportMetricsServiceResponse> responseObserver, ExportMetricsServiceResponse response) {
        try {
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (IllegalStateException e) {
            logger.debug("Response already closed (onNext/onCompleted); call likely cancelled/expired");
        }
    }
}
