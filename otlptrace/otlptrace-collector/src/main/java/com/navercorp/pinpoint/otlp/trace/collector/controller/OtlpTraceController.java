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

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;
import com.google.rpc.Code;
import com.google.rpc.Status;
import com.navercorp.pinpoint.otlp.trace.collector.service.OtlpTraceExportResult;
import com.navercorp.pinpoint.otlp.trace.collector.service.OtlpTraceExportService;
import com.navercorp.pinpoint.otlp.trace.collector.service.OtlpTraceIngestMetrics;
import com.navercorp.pinpoint.otlp.trace.collector.service.OtlpTraceResponseMapper;
import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceRequest;
import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceResponse;
import io.opentelemetry.proto.trace.v1.ResourceSpans;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

@RestController
public class OtlpTraceController {

    private static final JsonFormat.Printer JSON_PRINTER = JsonFormat.printer().omittingInsignificantWhitespace();

    private final OtlpTraceExportService exportService;
    private final OtlpTraceIngestMetrics ingestMetrics;

    public OtlpTraceController(OtlpTraceExportService exportService, OtlpTraceIngestMetrics ingestMetrics) {
        this.exportService = Objects.requireNonNull(exportService, "exportService");
        this.ingestMetrics = Objects.requireNonNull(ingestMetrics, "ingestMetrics");
    }

    // OTLP/HTTP response semantics (M-1), shared with the gRPC path via OtlpTraceResponseMapper:
    // parse failure -> 400 + google.rpc.Status body,
    // success / client-rejected -> 200 + ExportTraceServiceResponse body (empty or partial success),
    // server error -> retryable 503 + google.rpc.Status body.
    // The response encoding mirrors the request Content-Type (protobuf or JSON), not Accept — OTLP
    // exporters do not negotiate, and Spring's negotiation would not guarantee the mirror.
    @PostMapping(value = "/v1/traces",
            consumes = {MediaType.APPLICATION_PROTOBUF_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<byte[]> export(@RequestBody byte[] body,
                                         @RequestHeader(HttpHeaders.CONTENT_TYPE) String contentType) {
        final boolean json = isJson(contentType);
        final MediaType responseType = json ? MediaType.APPLICATION_JSON : MediaType.APPLICATION_PROTOBUF;

        final ExportTraceServiceRequest request;
        try {
            request = parseRequest(body, json);
        } catch (InvalidProtocolBufferException | OtlpTraceParseException e) {
            ingestMetrics.requestRejected(OtlpTraceIngestMetrics.Transport.HTTP, OtlpTraceIngestMetrics.RequestRejectReason.PARSE_ERROR);
            final Status status = Status.newBuilder()
                    .setCode(Code.INVALID_ARGUMENT_VALUE)
                    .setMessage(errorMessage(e))
                    .build();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .contentType(responseType)
                    .body(serialize(status, json));
        }

        final List<ResourceSpans> resourceSpanList = request.getResourceSpansList();
        final OtlpTraceExportResult result = exportService.export(resourceSpanList, OtlpTraceIngestMetrics.Transport.HTTP);

        if (OtlpTraceResponseMapper.isServerError(result)) {
            // Mirror the gRPC UNAVAILABLE path with a retryable 503 carrying a google.rpc.Status body,
            // so the exporter retries the whole batch instead of silently dropping recoverable data.
            final Status status = Status.newBuilder()
                    .setCode(Code.UNAVAILABLE_VALUE)
                    .setMessage(result.serverMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .contentType(responseType)
                    .body(serialize(status, json));
        }

        final ExportTraceServiceResponse response = OtlpTraceResponseMapper.toResponse(result);
        return ResponseEntity.ok()
                .contentType(responseType)
                .body(serialize(response, json));
    }

    private static boolean isJson(String contentType) {
        // parseMediaType drops parameters (charset etc.) from the comparison.
        return MediaType.parseMediaType(contentType).equalsTypeAndSubtype(MediaType.APPLICATION_JSON);
    }

    private static ExportTraceServiceRequest parseRequest(byte[] body, boolean json) throws InvalidProtocolBufferException {
        if (json) {
            return OtlpJsonTraceParser.parse(body);
        }
        return ExportTraceServiceRequest.parseFrom(body);
    }

    /**
     * Upper bound for the {@code google.rpc.Status.message} of a 400. Every message the parsers
     * produce on their own is shorter (the longest fixed protobuf text is ~200 chars); the cap only
     * bites when a JsonFormat type error echoes an attacker-sized value ("Invalid value: {...}"),
     * which would otherwise be reflected back at body size.
     */
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

    private static byte[] serialize(Message message, boolean json) {
        if (!json) {
            return message.toByteArray();
        }
        try {
            return JSON_PRINTER.print(message).getBytes(StandardCharsets.UTF_8);
        } catch (InvalidProtocolBufferException e) {
            // Only reachable for Any fields without a type registry; our responses carry none.
            throw new IllegalStateException("OTLP/JSON response serialization failed", e);
        }
    }
}
