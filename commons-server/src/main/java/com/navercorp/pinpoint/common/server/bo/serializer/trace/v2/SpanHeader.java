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

/**
 * First byte of the trace qualifier. The code doubles as both the span/chunk
 * discriminator and the source-type discriminator (Pinpoint vs OTel) — this
 * enum is the single owner of that mapping for the encoder, the decoder and
 * the HBase qualifier filters.
 */
public enum SpanHeader {
    SPAN(SpanEncoder.TYPE_SPAN, TraceSourceType.PINPOINT, false),
    SPAN_CHUNK(SpanEncoder.TYPE_SPAN_CHUNK, TraceSourceType.PINPOINT, true),
    OTEL_SPAN(SpanEncoder.TYPE_OTEL_SPAN, TraceSourceType.OPENTELEMETRY, false),
    OTEL_SPAN_CHUNK(SpanEncoder.TYPE_OTEL_SPAN_CHUNK, TraceSourceType.OPENTELEMETRY, true);

    private final byte code;
    private final TraceSourceType traceSourceType;
    private final boolean spanChunk;

    SpanHeader(byte code, TraceSourceType traceSourceType, boolean spanChunk) {
        this.code = code;
        this.traceSourceType = traceSourceType;
        this.spanChunk = spanChunk;
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

    public static SpanHeader span(TraceSourceType traceSourceType) {
        if (traceSourceType == TraceSourceType.OPENTELEMETRY) {
            return OTEL_SPAN;
        }
        return SPAN;
    }

    public static SpanHeader spanChunk(TraceSourceType traceSourceType) {
        if (traceSourceType == TraceSourceType.OPENTELEMETRY) {
            return OTEL_SPAN_CHUNK;
        }
        return SPAN_CHUNK;
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
            default -> null;
        };
    }
}
