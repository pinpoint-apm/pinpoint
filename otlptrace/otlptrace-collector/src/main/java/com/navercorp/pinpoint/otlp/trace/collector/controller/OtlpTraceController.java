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

package com.navercorp.pinpoint.otlp.trace.collector.controller;

import com.google.rpc.Code;
import com.google.rpc.Status;
import com.navercorp.pinpoint.otlp.trace.collector.service.OtlpTraceExportResult;
import com.navercorp.pinpoint.otlp.trace.collector.service.OtlpTraceExportService;
import com.navercorp.pinpoint.otlp.trace.collector.service.OtlpTraceResponseMapper;
import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceRequest;
import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceResponse;
import io.opentelemetry.proto.trace.v1.ResourceSpans;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;

@RestController
public class OtlpTraceController {

    private final OtlpTraceExportService exportService;

    public OtlpTraceController(OtlpTraceExportService exportService) {
        this.exportService = Objects.requireNonNull(exportService, "exportService");
    }

    // OTLP/HTTP response semantics (M-1), shared with the gRPC path via OtlpTraceResponseMapper:
    // success / client-rejected -> 200 + ExportTraceServiceResponse body (empty or partial success),
    // server error -> retryable 503 + google.rpc.Status body. Content-Type mirrors the protobuf request.
    @PostMapping(value = "/v1/traces",
            consumes = MediaType.APPLICATION_PROTOBUF_VALUE,
            produces = MediaType.APPLICATION_PROTOBUF_VALUE)
    public ResponseEntity<byte[]> export(@RequestBody ExportTraceServiceRequest request) {
        final List<ResourceSpans> resourceSpanList = request.getResourceSpansList();
        final OtlpTraceExportResult result = exportService.export(resourceSpanList);

        if (OtlpTraceResponseMapper.isServerError(result)) {
            // Mirror the gRPC UNAVAILABLE path with a retryable 503 carrying a google.rpc.Status body,
            // so the exporter retries the whole batch instead of silently dropping recoverable data.
            final Status status = Status.newBuilder()
                    .setCode(Code.UNAVAILABLE_VALUE)
                    .setMessage(result.serverMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .contentType(MediaType.APPLICATION_PROTOBUF)
                    .body(status.toByteArray());
        }

        final ExportTraceServiceResponse response = OtlpTraceResponseMapper.toResponse(result);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PROTOBUF)
                .body(response.toByteArray());
    }
}
