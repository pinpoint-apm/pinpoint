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

package com.navercorp.pinpoint.otlp.log.collector.controller;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.rpc.Code;
import com.google.rpc.Status;
import com.navercorp.pinpoint.otlp.log.collector.OtlpLogCollectorHttpModule;
import com.navercorp.pinpoint.otlp.log.collector.service.OtlpLogExportResult;
import com.navercorp.pinpoint.otlp.log.collector.service.OtlpLogExportService;
import com.navercorp.pinpoint.otlp.log.collector.service.OtlpLogIngestMetrics;
import com.navercorp.pinpoint.otlp.log.collector.service.OtlpLogResponseMapper;
import com.navercorp.pinpoint.otlp.trace.collector.service.OtlpRequestRejectReason;
import com.navercorp.pinpoint.otlp.trace.collector.service.OtlpTransport;
import io.opentelemetry.proto.collector.logs.v1.ExportLogsServiceRequest;
import io.opentelemetry.proto.collector.logs.v1.ExportLogsServiceResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

/**
 * OTLP/HTTP logs endpoint, {@code POST /v1/logs}, protobuf only. The Java SDK refuses
 * {@code http/json} for logs and no measured exporter sends it, so OTLP/JSON is out of scope here;
 * an {@code application/json} body is answered 415 by the {@code consumes} declaration. gzip bodies
 * are inflated by the decompression filter in front of this controller.
 *
 * <p>Response semantics mirror the trace endpoint: parse failure &rarr; 400 with a
 * {@code google.rpc.Status} body; otherwise 200 with an {@link ExportLogsServiceResponse} (empty, or
 * a partial success naming the client-visible rejects). There is no 503 path: the only store is a
 * Kafka producer whose failures are counted, not retried by the exporter.
 */
@RestController
public class OtlpLogController {

    private final OtlpLogExportService exportService;
    private final OtlpLogIngestMetrics ingestMetrics;

    public OtlpLogController(OtlpLogExportService exportService, OtlpLogIngestMetrics ingestMetrics) {
        this.exportService = Objects.requireNonNull(exportService, "exportService");
        this.ingestMetrics = Objects.requireNonNull(ingestMetrics, "ingestMetrics");
    }

    @PostMapping(value = OtlpLogCollectorHttpModule.OTLP_HTTP_LOGS_PATH, consumes = MediaType.APPLICATION_PROTOBUF_VALUE)
    public ResponseEntity<byte[]> export(@RequestBody byte[] body) {
        final ExportLogsServiceRequest request;
        try {
            request = ExportLogsServiceRequest.parseFrom(body);
        } catch (InvalidProtocolBufferException e) {
            ingestMetrics.requestRejected(OtlpTransport.HTTP, OtlpRequestRejectReason.PARSE_ERROR);
            final Status status = Status.newBuilder()
                    .setCode(Code.INVALID_ARGUMENT_VALUE)
                    .setMessage(errorMessage(e))
                    .build();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .contentType(MediaType.APPLICATION_PROTOBUF)
                    .body(status.toByteArray());
        }

        final OtlpLogExportResult result = exportService.export(request.getResourceLogsList(), OtlpTransport.HTTP);
        final ExportLogsServiceResponse response = OtlpLogResponseMapper.toResponse(result);
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
