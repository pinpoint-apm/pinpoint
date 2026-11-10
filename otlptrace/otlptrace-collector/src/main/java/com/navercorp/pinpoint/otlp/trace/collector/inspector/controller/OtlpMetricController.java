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

package com.navercorp.pinpoint.otlp.trace.collector.inspector.controller;

import com.google.rpc.Code;
import com.google.rpc.Status;
import com.navercorp.pinpoint.otlp.trace.collector.inspector.OtlpMetricInspectorHttpModule;
import com.navercorp.pinpoint.otlp.trace.collector.inspector.service.OtlpMetricExportResult;
import com.navercorp.pinpoint.otlp.trace.collector.inspector.service.OtlpMetricExportService;
import com.navercorp.pinpoint.otlp.trace.collector.inspector.service.OtlpMetricIngestMetrics;
import com.navercorp.pinpoint.otlp.trace.collector.inspector.service.OtlpMetricResponseMapper;
import com.navercorp.pinpoint.otlp.trace.collector.service.OtlpRequestRejectReason;
import com.navercorp.pinpoint.otlp.trace.collector.service.OtlpTransport;
import io.opentelemetry.proto.collector.metrics.v1.ExportMetricsServiceRequest;
import io.opentelemetry.proto.collector.metrics.v1.ExportMetricsServiceResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

/**
 * OTLP/HTTP metrics endpoint, {@code POST /v1/metrics}, protobuf only ({@code application/json} is
 * answered 415 by the {@code consumes} declaration). gzip bodies are inflated by the decompression
 * filter in front of this controller.
 *
 * <p>Response semantics mirror the trace and log endpoints: parse failure &rarr; 400 with a
 * {@code google.rpc.Status} body; otherwise 200 with an {@link ExportMetricsServiceResponse} (empty,
 * or a partial success naming the rejected Resources' points). There is no 503 path: store
 * failures are counted, not retried by the exporter.
 *
 * <p>The body is taken as a raw {@link InputStream} (no {@code @RequestBody}, so no
 * HttpMessageConverter), as {@code OtlpTraceController} does: a {@code byte[]} parameter would hold the
 * whole wire body on top of the parsed message, and the protobuf is parsed straight off the stream
 * instead. Two consequences, both matching the gRPC path:
 * <ul>
 *   <li>an empty body is an empty {@link ExportMetricsServiceRequest} (200), not Spring's
 *   "body missing" 400;</li>
 *   <li>a body read failure — the admission/decompression filters' size-limit {@link IOException},
 *   or a truncated upload — reaches this method and is answered as a 400 parse error with a Status
 *   body and a {@code parse_error} rejection count, instead of Spring's bare 400 that counted
 *   nothing.</li>
 * </ul>
 */
@RestController
public class OtlpMetricController {

    private final OtlpMetricExportService exportService;
    private final OtlpMetricIngestMetrics ingestMetrics;

    public OtlpMetricController(OtlpMetricExportService exportService, OtlpMetricIngestMetrics ingestMetrics) {
        this.exportService = Objects.requireNonNull(exportService, "exportService");
        this.ingestMetrics = Objects.requireNonNull(ingestMetrics, "ingestMetrics");
    }

    @PostMapping(value = OtlpMetricInspectorHttpModule.OTLP_HTTP_METRICS_PATH, consumes = MediaType.APPLICATION_PROTOBUF_VALUE)
    public ResponseEntity<byte[]> export(InputStream body) {
        final ExportMetricsServiceRequest request;
        try {
            request = ExportMetricsServiceRequest.parseFrom(body);
        } catch (IOException e) {
            ingestMetrics.requestRejected(OtlpTransport.HTTP, OtlpRequestRejectReason.PARSE_ERROR);
            final Status status = Status.newBuilder()
                    .setCode(Code.INVALID_ARGUMENT_VALUE)
                    .setMessage(errorMessage(e))
                    .build();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .contentType(MediaType.APPLICATION_PROTOBUF)
                    .body(status.toByteArray());
        }

        final OtlpMetricExportResult result = exportService.export(request.getResourceMetricsList(), OtlpTransport.HTTP);
        final ExportMetricsServiceResponse response = OtlpMetricResponseMapper.toResponse(result);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PROTOBUF)
                .body(response.toByteArray());
    }

    /** Bound for the 400 message so a parser error never echoes an attacker-sized value. */
    static final int MAX_ERROR_MESSAGE_LENGTH = 256;

    static String errorMessage(Exception e) {
        final String message = e.getMessage();
        if (message == null) {
            return e.getClass().getSimpleName();
        }
        if (message.length() > MAX_ERROR_MESSAGE_LENGTH) {
            return message.substring(0, MAX_ERROR_MESSAGE_LENGTH) + "...";
        }
        return message;
    }
}
