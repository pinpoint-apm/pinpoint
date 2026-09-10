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

import com.navercorp.pinpoint.otlp.trace.collector.service.OtlpRequestRejectReason;
import com.navercorp.pinpoint.otlp.trace.collector.service.OtlpTransport;
import io.grpc.Context;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import io.opentelemetry.proto.collector.logs.v1.ExportLogsServiceRequest;
import io.opentelemetry.proto.collector.logs.v1.ExportLogsServiceResponse;
import io.opentelemetry.proto.collector.logs.v1.LogsServiceGrpc;
import io.opentelemetry.proto.logs.v1.ResourceLogs;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.Semaphore;

/**
 * OTLP {@code LogsService/Export}. Same shape as the trace service: reserve the request's wire size
 * from the log receiver's own in-flight byte budget, offload the filter/map/store work to the log
 * worker pool, answer with the OTLP response. Both refusals are {@code UNAVAILABLE} because OTLP
 * exporters retry it unconditionally (RESOURCE_EXHAUSTED only with RetryInfo), so saturation is
 * back-pressure rather than silent loss.
 */
public class GrpcOtlpLogService extends LogsServiceGrpc.LogsServiceImplBase {

    private static final Status EXECUTOR_REJECTED = Status.UNAVAILABLE.withDescription("Executor rejected");
    private static final Status ADMISSION_REJECTED = Status.UNAVAILABLE.withDescription("In-flight byte budget exhausted");

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final OtlpLogExportService exportService;
    private final Executor workerExecutor;
    private final Semaphore admissionBytes;
    private final int maxInFlightBytes;
    private final OtlpLogIngestMetrics ingestMetrics;

    public GrpcOtlpLogService(OtlpLogExportService exportService, Executor workerExecutor, int maxInFlightBytes,
                              OtlpLogIngestMetrics ingestMetrics) {
        this.exportService = Objects.requireNonNull(exportService, "exportService");
        this.workerExecutor = Objects.requireNonNull(workerExecutor, "workerExecutor");
        this.ingestMetrics = Objects.requireNonNull(ingestMetrics, "ingestMetrics");
        this.maxInFlightBytes = maxInFlightBytes;
        this.admissionBytes = new Semaphore(maxInFlightBytes);
        ingestMetrics.registerInFlightBytes(OtlpTransport.GRPC,
                () -> (long) maxInFlightBytes - admissionBytes.availablePermits(), maxInFlightBytes);
    }

    @Override
    public void export(ExportLogsServiceRequest request, StreamObserver<ExportLogsServiceResponse> responseObserver) {
        // The request is already <= the server's inbound_message_size_max (shared with the trace
        // service); this gate bounds the total parsed bytes in flight for logs alone.
        final int requestBytes = request.getSerializedSize();
        if (!admissionBytes.tryAcquire(requestBytes)) {
            logger.warn("Failed to export logs. In-flight byte budget exhausted. requestBytes={}, budget={}", requestBytes, maxInFlightBytes);
            ingestMetrics.requestRejected(OtlpTransport.GRPC, OtlpRequestRejectReason.INFLIGHT_BYTES);
            safeOnError(responseObserver, ADMISSION_REJECTED);
            return;
        }

        ingestMetrics.requestBytes(OtlpTransport.GRPC, requestBytes);

        final List<ResourceLogs> resourceLogsList = request.getResourceLogsList();
        final Context current = Context.current();
        final Runnable exportTask = current.wrap(() -> {
            try {
                if (Context.current().isCancelled()) {
                    // Client gave up (deadline / cancel) before the task ran; skip the wasted work.
                    return;
                }
                final OtlpLogExportResult result = exportService.export(resourceLogsList, OtlpTransport.GRPC);
                safeComplete(responseObserver, OtlpLogResponseMapper.toResponse(result));
            } catch (Throwable t) {
                // INTERNAL is non-retryable: a deterministic (poison-data) fault must not retry-storm.
                logger.warn("Unexpected error while exporting otlp logs", t);
                safeOnError(responseObserver, Status.INTERNAL.withDescription("export failed"));
            } finally {
                admissionBytes.release(requestBytes);
            }
        });
        try {
            workerExecutor.execute(exportTask);
        } catch (RejectedExecutionException e) {
            admissionBytes.release(requestBytes);
            logger.warn("Failed to export logs. Worker executor rejected.");
            ingestMetrics.requestRejected(OtlpTransport.GRPC, OtlpRequestRejectReason.EXECUTOR_REJECTED);
            safeOnError(responseObserver, EXECUTOR_REJECTED);
        }
    }

    // The client may have closed the call (cancel / deadline) while the worker ran; a dead call must
    // not surface as a noisy error.
    private void safeOnError(StreamObserver<ExportLogsServiceResponse> responseObserver, Status status) {
        try {
            responseObserver.onError(status.asRuntimeException());
        } catch (IllegalStateException e) {
            logger.debug("Response already closed (onError); call likely cancelled/expired");
        }
    }

    private void safeComplete(StreamObserver<ExportLogsServiceResponse> responseObserver, ExportLogsServiceResponse response) {
        try {
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (IllegalStateException e) {
            logger.debug("Response already closed (onNext/onCompleted); call likely cancelled/expired");
        }
    }
}
