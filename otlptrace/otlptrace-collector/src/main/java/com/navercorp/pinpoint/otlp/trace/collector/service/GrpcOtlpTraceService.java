/*
 * Copyright 2025 NAVER Corp.
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

package com.navercorp.pinpoint.otlp.trace.collector.service;

import com.navercorp.pinpoint.collector.service.ExceptionMetaDataService;
import com.navercorp.pinpoint.collector.service.TraceService;
import com.navercorp.pinpoint.common.cache.LRUCache;
import com.navercorp.pinpoint.common.server.bo.AgentInfoBo;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.bo.SpanChunkBo;
import com.navercorp.pinpoint.common.server.bo.exception.ExceptionMetaDataBo;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.otlp.trace.collector.OtlpTraceCollectorRejectedSpan;
import com.navercorp.pinpoint.otlp.trace.collector.mapper.OtlpTraceMapper;
import com.navercorp.pinpoint.otlp.trace.collector.mapper.OtlpTraceMapperData;
import io.grpc.Context;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import io.opentelemetry.proto.collector.trace.v1.ExportTracePartialSuccess;
import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceRequest;
import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceResponse;
import io.opentelemetry.proto.collector.trace.v1.TraceServiceGrpc;
import io.opentelemetry.proto.trace.v1.ResourceSpans;
import jakarta.validation.constraints.NotNull;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.Semaphore;
import java.util.function.Supplier;

public class GrpcOtlpTraceService extends TraceServiceGrpc.TraceServiceImplBase {
    public static final Supplier<ServiceUid> DEFAULT_SERVICE_UID = () -> ServiceUid.DEFAULT;

    // UNAVAILABLE (not RESOURCE_EXHAUSTED): OTLP exporters retry UNAVAILABLE unconditionally,
    // whereas RESOURCE_EXHAUSTED is retried only when a RetryInfo is attached. Using UNAVAILABLE
    // turns a saturated worker queue into retryable backpressure rather than silent data loss.
    private static final Status EXECUTOR_REJECTED = Status.UNAVAILABLE.withDescription("Executor rejected");
    private static final Status ADMISSION_REJECTED = Status.UNAVAILABLE.withDescription("In-flight byte budget exhausted");

    private final Logger logger = LogManager.getLogger(this.getClass());

    @NotNull
    private final TraceService[] traceServiceList;
    private final HbaseOtlpAgentInfoService agentInfoService;
    private final HbaseOtlpApplicationIndexV2Service applicationIndexV2Service;
    @NotNull
    private final OtlpTraceMapper otlpTraceMapper;
    private final ExceptionMetaDataService exceptionMetaDataService;
    // Worker pool that runs the mapping/insert work, keeping it off the gRPC handler (server.executor).
    private final Executor workerExecutor;
    // Global byte-based admission: caps the total in-flight (queued + processing) request wire bytes
    // so concurrent requests cannot exhaust the heap regardless of request count or connection count.
    private final Semaphore admissionBytes;
    private final int maxInFlightBytes;
    private final LRUCache<String, Boolean> agentIdCache = new LRUCache<>(10000);

    public GrpcOtlpTraceService(TraceService[] traceServiceList, HbaseOtlpAgentInfoService agentInfoService, HbaseOtlpApplicationIndexV2Service applicationIndexV2Service, OtlpTraceMapper otlpTraceMapper, ExceptionMetaDataService exceptionMetaDataService, Executor workerExecutor, int maxInFlightBytes) {
        this.traceServiceList = traceServiceList;
        this.agentInfoService = agentInfoService;
        this.applicationIndexV2Service = applicationIndexV2Service;
        this.otlpTraceMapper = otlpTraceMapper;
        this.exceptionMetaDataService = exceptionMetaDataService;
        this.workerExecutor = workerExecutor;
        this.maxInFlightBytes = maxInFlightBytes;
        this.admissionBytes = new Semaphore(maxInFlightBytes);
    }

    @Override
    public void export(ExportTraceServiceRequest request, StreamObserver<ExportTraceServiceResponse> responseObserver) {
        // Global byte-based admission: reserve this request's wire size from the in-flight budget
        // before queuing. When the budget is exhausted, reject with UNAVAILABLE (retryable) so the
        // exporter backs off, bounding total memory regardless of request size / connection count.
        // (request size is already <= maxInboundMessageSize, enforced by gRPC before this handler.)
        final int requestBytes = request.getSerializedSize();
        if (!admissionBytes.tryAcquire(requestBytes)) {
            logger.warn("Failed to export. In-flight byte budget exhausted. requestBytes={}, budget={}", requestBytes, maxInFlightBytes);
            safeOnError(responseObserver, ADMISSION_REJECTED);
            return;
        }

        final List<ResourceSpans> resourceSpanList = request.getResourceSpansList();
        // Offload the mapping/insert work onto the worker pool so the gRPC handler thread
        // (server.executor) is not blocked. A saturated worker queue surfaces to the client
        // as UNAVAILABLE (retryable), providing backpressure instead of dropping spans.
        final Context current = Context.current();
        final Runnable exportTask = current.wrap(() -> {
            try {
                if (Context.current().isCancelled()) {
                    // Client already gave up (deadline exceeded / cancelled) before this task ran.
                    // Skip the wasted mapping/insert; the admission reservation is freed in finally.
                    return;
                }
                handleExport(resourceSpanList, responseObserver);
            } catch (Throwable t) {
                // Unexpected failure (e.g. mapping fault on malformed input). Without this catch the
                // exception would escape to the worker thread: the response would never be closed
                // (client hangs until deadline) and the worker thread would die on an uncaught error.
                // INTERNAL is non-retryable, avoiding a retry storm on deterministic (poison-data) faults.
                logger.warn("Unexpected error while exporting otlp trace", t);
                safeOnError(responseObserver, Status.INTERNAL.withDescription("export failed"));
            } finally {
                admissionBytes.release(requestBytes);
            }
        });
        try {
            workerExecutor.execute(exportTask);
        } catch (RejectedExecutionException e) {
            admissionBytes.release(requestBytes);
            logger.warn("Failed to export. Worker executor rejected.");
            safeOnError(responseObserver, EXECUTOR_REJECTED);
        }
    }

    private void handleExport(List<ResourceSpans> resourceSpanList, StreamObserver<ExportTraceServiceResponse> responseObserver) {
        final ExportResult result = export(resourceSpanList);

        if (result.serverErrorCount() > 0) {
            // Server-side / transient failures (HBase insert, agentInfo): ask the client to retry
            // the whole batch via UNAVAILABLE (retryable) instead of dropping recoverable data.
            // INVALID_ARGUMENT here would be treated as non-retryable and silently lost.
            safeOnError(responseObserver, Status.UNAVAILABLE.withDescription(result.serverMessage()));
            return;
        }

        final OtlpTraceCollectorRejectedSpan clientRejected = result.clientRejected();
        if (clientRejected.count() > 0) {
            // Client-side data faults (invalid/missing identifiers, unlinkable spans) are
            // deterministic, so report them via OTLP partial success rather than a retryable error.
            final ExportTracePartialSuccess partialSuccess = ExportTracePartialSuccess.newBuilder()
                    .setErrorMessage(clientRejected.getMessage())
                    .setRejectedSpans(clientRejected.count())
                    .build();
            safeComplete(responseObserver, ExportTraceServiceResponse.newBuilder().setPartialSuccess(partialSuccess).build());
        } else {
            // success
            safeComplete(responseObserver, ExportTraceServiceResponse.getDefaultInstance());
        }
    }

    // Guards response calls against IllegalStateException when the client already closed the call
    // (cancellation / deadline) while the worker task was running, so a dead call does not surface
    // as a noisy error.
    private void safeOnError(StreamObserver<ExportTraceServiceResponse> responseObserver, Status status) {
        try {
            responseObserver.onError(status.asRuntimeException());
        } catch (IllegalStateException e) {
            logger.debug("Response already closed (onError); call likely cancelled/expired");
        }
    }

    private void safeComplete(StreamObserver<ExportTraceServiceResponse> responseObserver, ExportTraceServiceResponse response) {
        try {
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (IllegalStateException e) {
            logger.debug("Response already closed (onNext/onCompleted); call likely cancelled/expired");
        }
    }

    private ExportResult export(List<ResourceSpans> resourceSpanList) {
        final OtlpTraceMapperData otlpTraceMapperData = otlpTraceMapper.map(resourceSpanList);
        // Mapping rejects (invalid ids, unlinkable/orphan spans) are client-side data faults.
        final OtlpTraceCollectorRejectedSpan clientRejected = otlpTraceMapperData.getRejectedSpan();

        int insertErrorCount = 0;
        for (SpanBo spanBo : otlpTraceMapperData.getSpanBoList()) {
            for (TraceService traceService : traceServiceList) {
                try {
                    traceService.insertSpan(spanBo);
                } catch (Exception e) {
                    insertErrorCount++;
                    logger.warn("Failed to insert spanBo", e);
                }
            }
        }

        for (SpanChunkBo spanChunkBo : otlpTraceMapperData.getSpanChunkBoList()) {
            for (TraceService traceService : traceServiceList) {
                try {
                    traceService.insertSpanChunk(spanChunkBo);
                } catch (Exception e) {
                    insertErrorCount++;
                    logger.warn("Failed to insert spanChunkBo", e);
                }
            }
        }

        int agentInfoErrorCount = 0;
        for (AgentInfoBo agentInfoBo : otlpTraceMapperData.getAgentInfoBoList()) {
            if (agentIdCache.get(agentInfoBo.getAgentId()) == null) {
                try {
                    agentInfoService.insert(agentInfoBo);
                    applicationIndexV2Service.insert(DEFAULT_SERVICE_UID, agentInfoBo);
                    agentIdCache.put(agentInfoBo.getAgentId(), true);
                } catch (Exception e) {
                    agentInfoErrorCount++;
                    logger.warn("Failed to insert agentInfoBo", e);
                }
            }
        }

        if (exceptionMetaDataService != null) {
            for (ExceptionMetaDataBo exceptionMetaDataBo : otlpTraceMapperData.getExceptionMetaDataBoList()) {
                try {
                    exceptionMetaDataService.save(exceptionMetaDataBo);
                } catch (Exception e) {
                    logger.warn("Failed to insert exceptionMetaData", e);
                }
            }
        }

        final int serverErrorCount = insertErrorCount + agentInfoErrorCount;
        return new ExportResult(clientRejected, serverErrorCount, buildServerMessage(insertErrorCount, agentInfoErrorCount));
    }

    private static String buildServerMessage(int insertErrorCount, int agentInfoErrorCount) {
        final StringBuilder sb = new StringBuilder();
        if (insertErrorCount > 0) {
            sb.append("insert error (").append(insertErrorCount).append(')');
        }
        if (agentInfoErrorCount > 0) {
            if (!sb.isEmpty()) {
                sb.append(", ");
            }
            sb.append("agentInfo error (").append(agentInfoErrorCount).append(')');
        }
        return sb.toString();
    }

    private record ExportResult(OtlpTraceCollectorRejectedSpan clientRejected, int serverErrorCount, String serverMessage) {
    }
}