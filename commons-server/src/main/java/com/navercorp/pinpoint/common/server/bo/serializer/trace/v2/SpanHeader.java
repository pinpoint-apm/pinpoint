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

package com.navercorp.pinpoint.common.server.bo.serializer.trace.v2;

import com.navercorp.pinpoint.common.server.bo.TraceSourceType;

import java.util.Objects;

/**
 * First byte of the trace qualifier. The code doubles as both the span/chunk
 * discriminator and the source-type discriminator (Pinpoint vs OTel). This
 * enum owns the reverse mapping ({@link #of(byte)}) used by the decoder and
 * the HBase qualifier filters; the write-path variant selection is owned by
 * {@link SpanHeaderFactory}.
 */
public enum SpanHeader {
    SPAN(SpanEncoder.TYPE_SPAN, TraceSourceType.PINPOINT, false, false),
    SPAN_CHUNK(SpanEncoder.TYPE_SPAN_CHUNK, TraceSourceType.PINPOINT, true, false),
    OTEL_SPAN(SpanEncoder.TYPE_OTEL_SPAN, TraceSourceType.OPENTELEMETRY, false, false),
    OTEL_SPAN_CHUNK(SpanEncoder.TYPE_OTEL_SPAN_CHUNK, TraceSourceType.OPENTELEMETRY, true, false),

    SPAN_UID(SpanEncoder.TYPE_SPAN_UID, TraceSourceType.PINPOINT, false, true),
    SPAN_CHUNK_UID(SpanEncoder.TYPE_SPAN_CHUNK_UID, TraceSourceType.PINPOINT, true, true),
    OTEL_SPAN_UID(SpanEncoder.TYPE_OTEL_SPAN_UID, TraceSourceType.OPENTELEMETRY, false, true),
    OTEL_SPAN_CHUNK_UID(SpanEncoder.TYPE_OTEL_SPAN_CHUNK_UID, TraceSourceType.OPENTELEMETRY, true, true);

    private final byte code;
    private final TraceSourceType traceSourceType;
    private final boolean spanChunk;
    private final boolean serviceUid;

    SpanHeader(byte code, TraceSourceType traceSourceType, boolean spanChunk, boolean serviceUid) {
        this.code = code;
        this.traceSourceType = Objects.requireNonNull(traceSourceType, "traceSourceType");
        this.spanChunk = spanChunk;
        this.serviceUid = serviceUid;
    }

    public byte getCode() {
        return code;
    }

    public TraceSourceType getTraceSourceType() {
        return traceSourceType;
    }

    public boolean isSpanChunk() {
        return spanChunk;
    }

    /**
     * @return {@code true} if the qualifier carries a serviceUid.
     */
    public boolean hasServiceUid() {
        return serviceUid;
    }

    /**
     * Resolves the header for the qualifier's first byte; {@code null} for
     * unknown or reserved codes ({@code TYPE_PASSIVE_SPAN}, {@code TYPE_INDEX})
     * so decoders can skip them leniently.
     */
    public static SpanHeader of(byte code) {
        return switch (code) {
            case SpanEncoder.TYPE_SPAN -> SPAN;
            case SpanEncoder.TYPE_SPAN_CHUNK -> SPAN_CHUNK;
            case SpanEncoder.TYPE_OTEL_SPAN -> OTEL_SPAN;
            case SpanEncoder.TYPE_OTEL_SPAN_CHUNK -> OTEL_SPAN_CHUNK;
            case SpanEncoder.TYPE_SPAN_UID -> SPAN_UID;
            case SpanEncoder.TYPE_SPAN_CHUNK_UID -> SPAN_CHUNK_UID;
            case SpanEncoder.TYPE_OTEL_SPAN_UID -> OTEL_SPAN_UID;
            case SpanEncoder.TYPE_OTEL_SPAN_CHUNK_UID -> OTEL_SPAN_CHUNK_UID;
            default -> null;
        };
    }
}
