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

package com.navercorp.pinpoint.otlp.trace.collector.util;

import com.google.protobuf.ByteString;
import com.google.protobuf.util.JsonFormat;
import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceRequest;
import io.opentelemetry.proto.trace.v1.ResourceSpans;
import io.opentelemetry.proto.trace.v1.ScopeSpans;
import io.opentelemetry.proto.trace.v1.Span;

import java.util.Base64;
import java.util.HexFormat;

/**
 * Recovers the hex-encoded IDs of an OTLP/JSON request after a {@link JsonFormat} parse.
 * <p>
 * OTLP/JSON deviates from proto3 JSON in one place: {@code trace_id}/{@code span_id}/
 * {@code parent_span_id} are hex strings, while JsonFormat decodes every {@code bytes} field as
 * base64 and offers no hook to change that. Every hex digit is also a base64 digit and a 32/16-char
 * hex ID is a whole number of base64 quartets, so JsonFormat's base64 decoding of a hex ID is
 * lossless: re-encoding the 24/12 bytes it produced gives the original hex text back, which is then
 * decoded as hex into the 16/8-byte ID. This is one walk over the parsed builders instead of a
 * second pass over the JSON text; zipkin-otel's OTLP/HTTP collector takes the same approach
 * ({@code zipkin2.collector.otel.http.ProtoUtils#fixJsonIds}).
 * <p>
 * ID length (16/8 bytes) is not validated here; {@code OtlpIdValidator} owns that, identically to
 * the gRPC path. A value that does not round-trip to hex digits (a base64 ID, an odd-length or
 * non-hex string, a non-string value coerced by JsonFormat) fails with
 * {@link IllegalArgumentException} naming the offending character.
 */
public final class OtlpJsonIds {

    private static final Base64.Encoder BASE64 = Base64.getEncoder().withoutPadding();

    private OtlpJsonIds() {
    }

    /**
     * Rewrites the Span and Span.Link ID fields of a parsed trace request in place.
     *
     * @throws IllegalArgumentException if an ID does not round-trip to hex digits
     */
    public static void decodeHexIds(ExportTraceServiceRequest.Builder request) {
        for (ResourceSpans.Builder resourceSpans : request.getResourceSpansBuilderList()) {
            for (ScopeSpans.Builder scopeSpans : resourceSpans.getScopeSpansBuilderList()) {
                for (Span.Builder span : scopeSpans.getSpansBuilderList()) {
                    span.setTraceId(hexIdFromBase64Bytes(span.getTraceId()));
                    span.setSpanId(hexIdFromBase64Bytes(span.getSpanId()));
                    span.setParentSpanId(hexIdFromBase64Bytes(span.getParentSpanId()));
                    for (Span.Link.Builder link : span.getLinksBuilderList()) {
                        link.setTraceId(hexIdFromBase64Bytes(link.getTraceId()));
                        link.setSpanId(hexIdFromBase64Bytes(link.getSpanId()));
                    }
                }
            }
        }
    }

    /**
     * Undoes JsonFormat's base64 decoding of a hex ID: re-encode to recover the hex text, then
     * decode that as hex. Empty stays empty (absent {@code parent_span_id}). Anything that does not
     * come back as hex digits fails in {@link HexFormat#parseHex} with the offending character.
     */
    public static ByteString hexIdFromBase64Bytes(ByteString value) {
        if (value.isEmpty()) {
            return value;
        }
        final String hex = BASE64.encodeToString(value.toByteArray());
        return ByteString.copyFrom(HexFormat.of().parseHex(hex));
    }
}
