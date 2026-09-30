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
import com.navercorp.pinpoint.otlp.trace.collector.service.OtlpRequestRejectReason;
import com.navercorp.pinpoint.otlp.trace.collector.service.OtlpTraceExportResult;
import com.navercorp.pinpoint.otlp.trace.collector.service.OtlpTraceExportService;
import com.navercorp.pinpoint.otlp.trace.collector.service.OtlpTraceIngestMetrics;
import com.navercorp.pinpoint.otlp.trace.collector.service.OtlpTraceResponseMapper;
import com.navercorp.pinpoint.otlp.trace.collector.service.OtlpTransport;
import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceRequest;
import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceResponse;
import io.opentelemetry.proto.trace.v1.ResourceSpans;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

@RestController
public class OtlpTraceController {

    private static final JsonFormat.Printer JSON_PRINTER = JsonFormat.printer().omittingInsignificantWhitespace();

    private final OtlpTraceExportService exportService;
    private final OtlpTraceIngestMetrics ingestMetrics;
    private final OtlpJsonTraceParser jsonParser = new OtlpJsonTraceParser();

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
    //
    // The body is taken as a raw InputStream (no @RequestBody, so no HttpMessageConverter): a byte[]
    // parameter would hold the whole wire body — transiently twice, through readAllBytes — on top of
    // the parsed message. Protobuf is parsed straight off the stream instead, so only the parsed
    // message is ever in heap. Two consequences, both matching the gRPC path:
    // - an empty body is an empty ExportTraceServiceRequest (200), not Spring's "body missing" 400;
    // - a body read failure — the admission/decompression filters' size-limit IOException, or a
    //   truncated upload — reaches this method (the generated parser wraps it in
    //   InvalidProtocolBufferException) and is answered as a 400 parse error with a Status body and
    //   a parse_error rejection count, instead of Spring's bare 400 that counted nothing.
    @PostMapping(value = "/v1/traces",
            consumes = {MediaType.APPLICATION_PROTOBUF_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<byte[]> export(InputStream body,
                                         @RequestHeader(HttpHeaders.CONTENT_TYPE) String contentType) {
        final boolean json = isJson(contentType);
        final MediaType responseType = json ? MediaType.APPLICATION_JSON : MediaType.APPLICATION_PROTOBUF;

        final ExportTraceServiceRequest request;
        try {
            request = parseRequest(body, json);
        } catch (IOException | OtlpTraceParseException e) {
            ingestMetrics.requestRejected(OtlpTransport.HTTP, OtlpRequestRejectReason.PARSE_ERROR);
            final Status status = Status.newBuilder()
                    .setCode(Code.INVALID_ARGUMENT_VALUE)
                    .setMessage(errorMessage(e))
                    .build();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .contentType(responseType)
                    .body(serialize(status, json));
        }

        final List<ResourceSpans> resourceSpanList = request.getResourceSpansList();
        final OtlpTraceExportResult result = exportService.export(resourceSpanList, OtlpTransport.HTTP);

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

    private ExportTraceServiceRequest parseRequest(InputStream body, boolean json) throws IOException {
        if (json) {
            // OTLP/JSON still materializes the body: the hex->base64 ID rewrite and JsonFormat's Gson
            // tree dominate its footprint, so the raw copy is not the lever there.
            return jsonParser.parse(body.readAllBytes());
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
