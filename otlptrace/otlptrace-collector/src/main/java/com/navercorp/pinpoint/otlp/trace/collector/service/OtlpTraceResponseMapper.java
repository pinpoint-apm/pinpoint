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

package com.navercorp.pinpoint.otlp.trace.collector.service;

import com.navercorp.pinpoint.otlp.trace.collector.OtlpTraceCollectorRejectedSpan;
import io.opentelemetry.proto.collector.trace.v1.ExportTracePartialSuccess;
import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceResponse;

/**
 * Maps a transport-agnostic {@link OtlpTraceExportResult} to the OTLP response semantics shared by
 * the gRPC ({@code GrpcOtlpTraceService}) and HTTP ({@code OtlpTraceController}) transports, so both
 * paths classify an export outcome identically.
 * <p>
 * Only the two non-error outcomes have a common on-the-wire representation (an
 * {@link ExportTraceServiceResponse}); the retryable server-error outcome is rendered per transport
 * (gRPC {@code UNAVAILABLE} status vs HTTP 5xx + {@code google.rpc.Status} body), so callers branch
 * on {@link #isServerError(OtlpTraceExportResult)} first and render the error themselves.
 */
public final class OtlpTraceResponseMapper {

    private OtlpTraceResponseMapper() {
    }

    /**
     * Whether the result carries retryable server-side / transient failures (HBase insert, agentInfo).
     * Callers must render this as a retryable error (gRPC UNAVAILABLE / HTTP 5xx) before calling
     * {@link #toResponse(OtlpTraceExportResult)}.
     */
    public static boolean isServerError(OtlpTraceExportResult result) {
        return result.serverErrorCount() > 0;
    }

    /**
     * Builds the OTLP response for a non-server-error result: an {@link ExportTracePartialSuccess}
     * body when the client sent deterministically rejected spans, otherwise the empty success response.
     */
    public static ExportTraceServiceResponse toResponse(OtlpTraceExportResult result) {
        final OtlpTraceCollectorRejectedSpan clientRejected = result.clientRejected();
        if (clientRejected.count() > 0) {
            // Client-side data faults (invalid/missing identifiers, unlinkable spans) are
            // deterministic, so report them via OTLP partial success rather than a retryable error.
            final ExportTracePartialSuccess partialSuccess = ExportTracePartialSuccess.newBuilder()
                    .setErrorMessage(clientRejected.getMessage())
                    .setRejectedSpans(clientRejected.count())
                    .build();
            return ExportTraceServiceResponse.newBuilder().setPartialSuccess(partialSuccess).build();
        }
        return ExportTraceServiceResponse.getDefaultInstance();
    }
}
