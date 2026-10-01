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
import com.google.protobuf.util.JsonFormat;
import com.navercorp.pinpoint.otlp.trace.collector.util.OtlpJsonIds;
import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceRequest;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

/**
 * Parses an OTLP/JSON {@code ExportTraceServiceRequest} body.
 * <p>
 * OTLP/JSON is the proto3 JSON mapping with one deviation this parser must undo: the OTLP spec
 * encodes {@code trace_id}/{@code span_id}/{@code parent_span_id} (and the same fields on
 * {@code Span.Link}) as hex strings, while proto3 JSON — and therefore {@link JsonFormat} —
 * decodes every {@code bytes} field as base64. The IDs are recovered after the parse by
 * {@link OtlpJsonIds#decodeHexIds}; a value that does not round-trip to hex digits is a parse
 * error. The remaining OTLP/JSON tolerances are standard {@link JsonFormat} behavior: camelCase and
 * snake_case field names, enums by name or number, int64 as string or number, and unknown fields
 * skipped via {@code ignoringUnknownFields()}.
 */
public final class OtlpJsonTraceParser {

    private final JsonFormat.Parser protoJsonParser = JsonFormat.parser().ignoringUnknownFields();

    public OtlpJsonTraceParser() {
    }

    /**
     * Parses the body off the stream as UTF-8. A failure of the stream itself (e.g. the request
     * size guards) surfaces with the stream's own message, like any other parse failure.
     */
    public ExportTraceServiceRequest parse(InputStream body) {
        final ExportTraceServiceRequest.Builder builder = ExportTraceServiceRequest.newBuilder();
        try {
            InputStreamReader reader = new InputStreamReader(body, StandardCharsets.UTF_8);
            protoJsonParser.merge(reader, builder);
            OtlpJsonIds.decodeHexIds(builder);
            return builder.build();
        } catch (InvalidProtocolBufferException e) {
            throw new OtlpTraceParseException(errorMessage(e), e);
        } catch (IOException | IllegalArgumentException e) {
            throw new OtlpTraceParseException(e.getMessage(), e);
        }
    }

    /**
     * JsonFormat reports a JSON syntax error as the wrapped Gson exception's {@code toString()},
     * e.g. {@code com.google.gson.stream.MalformedJsonException: Expected value at line 1 column 19
     * path $.resourceSpans[0]}, and Gson appends a troubleshooting-guide link on its own line.
     * Neither adds anything a client can act on; keep the cause, position and path only.
     */
    static String errorMessage(InvalidProtocolBufferException e) {
        String message = e.getMessage();
        if (message == null) {
            return e.getClass().getSimpleName();
        }
        message = EXCEPTION_CLASS_PREFIX.matcher(message).replaceFirst("");
        final int link = message.indexOf(TROUBLESHOOTING_LINK);
        if (link > 0) {
            message = message.substring(0, link);
        }
        return message;
    }

    private static final Pattern EXCEPTION_CLASS_PREFIX = Pattern.compile("^(?:[a-z][\\w$]*\\.)+[A-Z][\\w$]*(?:Exception|Error): ");
    private static final String TROUBLESHOOTING_LINK = "\nSee https://github.com/google/gson/";
}
