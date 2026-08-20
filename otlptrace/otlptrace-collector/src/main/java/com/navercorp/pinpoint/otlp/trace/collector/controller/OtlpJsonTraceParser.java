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

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.google.protobuf.util.JsonFormat;
import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceRequest;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Parses an OTLP/JSON {@code ExportTraceServiceRequest} body.
 * <p>
 * OTLP/JSON is the proto3 JSON mapping with one deviation this parser must undo: the OTLP spec
 * encodes {@code trace_id}/{@code span_id}/{@code parent_span_id} (and the same fields on
 * {@code Span.Link}) as hex strings, while proto3 JSON — and therefore {@link JsonFormat} —
 * decodes every {@code bytes} field as base64. A 32-char hex trace ID is also valid base64, so
 * feeding it to {@link JsonFormat} silently yields 24 bytes and the downstream ID validator would
 * reject every span. The pipeline is therefore two stages: a Jackson streaming rewrite that
 * re-encodes only those ID string values from hex to base64, then the standard proto3 JSON parse.
 * <p>
 * The rewrite matches ID field names at any depth. This is safe because in OTLP JSON user data can
 * only appear as attribute values ({@code {"key": ..., "value": {...}}}) — a user string is never a
 * JSON field name. ID length (16/8 bytes) is deliberately not validated here; the existing
 * {@code OtlpIdValidator} owns that, identically to the gRPC path.
 * <p>
 * The remaining OTLP/JSON tolerances are standard {@link JsonFormat} behavior: camelCase and
 * snake_case field names, enums by name or number, int64 as string or number, and unknown fields
 * skipped via {@code ignoringUnknownFields()}.
 */
public final class OtlpJsonTraceParser {

    private static final Set<String> ID_FIELD_NAMES = Set.of(
            "traceId", "trace_id",
            "spanId", "span_id",
            "parentSpanId", "parent_span_id");

    private static final JsonFactory JSON_FACTORY = new JsonFactory();
    private static final JsonFormat.Parser PROTO_JSON_PARSER = JsonFormat.parser().ignoringUnknownFields();

    private OtlpJsonTraceParser() {
    }

    public static ExportTraceServiceRequest parse(byte[] body) {
        try {
            final String protoJson = rewriteIdsToBase64(body);
            final ExportTraceServiceRequest.Builder builder = ExportTraceServiceRequest.newBuilder();
            PROTO_JSON_PARSER.merge(protoJson, builder);
            return builder.build();
        } catch (JsonProcessingException e) {
            throw new OtlpTraceParseException(syntaxErrorMessage(e), e);
        } catch (IOException | IllegalArgumentException e) {
            throw new OtlpTraceParseException(e.getMessage(), e);
        }
    }

    /**
     * Jackson's {@link JsonProcessingException#getMessage()} appends the full {@code JsonLocation}
     * ({@code [Source: REDACTED (`StreamReadFeature.INCLUDE_SOURCE_IN_LOCATION` disabled); line: 1,
     * column: 19]}), sometimes twice, which more than doubles the message without adding anything a
     * client can act on. Keep the cause and the position only — in the shape the OTel collector's
     * own JSON errors take.
     */
    static String syntaxErrorMessage(JsonProcessingException e) {
        // The original message can embed a location too ("(for Array starting at [Source: ...])").
        final String original = SOURCE_LOCATION.matcher(e.getOriginalMessage()).replaceAll("line $1, column $2");
        final JsonLocation location = e.getLocation();
        if (location == null) {
            return original;
        }
        return original + " (line " + location.getLineNr() + ", column " + location.getColumnNr() + ")";
    }

    private static final Pattern SOURCE_LOCATION = Pattern.compile("\\[Source: [^\\]]*?; line: (\\d+), column: (\\d+)\\]");

    private static String rewriteIdsToBase64(byte[] body) throws IOException {
        final StringWriter out = new StringWriter(Math.max(body.length, 32));
        try (JsonParser parser = JSON_FACTORY.createParser(body);
             JsonGenerator generator = JSON_FACTORY.createGenerator(out)) {
            JsonToken token;
            while ((token = parser.nextToken()) != null) {
                generator.copyCurrentEvent(parser);
                if (token == JsonToken.FIELD_NAME && ID_FIELD_NAMES.contains(parser.currentName())) {
                    final JsonToken valueToken = parser.nextToken();
                    if (valueToken == JsonToken.VALUE_STRING) {
                        generator.writeString(hexToBase64(parser.getText()));
                    } else {
                        // Not a hex ID (null, object, ...); pass through and let JsonFormat judge it.
                        generator.copyCurrentEvent(parser);
                    }
                }
            }
        }
        return out.toString();
    }

    private static String hexToBase64(String hex) {
        final byte[] bytes = HexFormat.of().parseHex(hex);
        return Base64.getEncoder().encodeToString(bytes);
    }
}
