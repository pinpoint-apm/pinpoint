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
 * Resolves the {@link SpanHeader} for an encoder that emits qualifiers with a
 * fixed serviceUid mode. Owning the serviceUid flag here keeps it out of every
 * {@link #span}/{@link #spanChunk} call site.
 */
public class SpanHeaderFactory {

    private final boolean serviceUid;

    public SpanHeaderFactory(boolean serviceUid) {
        this.serviceUid = serviceUid;
    }

    public SpanHeader span(TraceSourceType traceSourceType) {
        Objects.requireNonNull(traceSourceType, "traceSourceType");
        if (traceSourceType == TraceSourceType.OPENTELEMETRY) {
            return serviceUid ? SpanHeader.OTEL_SPAN_UID : SpanHeader.OTEL_SPAN;
        }
        return serviceUid ? SpanHeader.SPAN_UID : SpanHeader.SPAN;
    }

    public SpanHeader spanChunk(TraceSourceType traceSourceType) {
        Objects.requireNonNull(traceSourceType, "traceSourceType");
        if (traceSourceType == TraceSourceType.OPENTELEMETRY) {
            return serviceUid ? SpanHeader.OTEL_SPAN_CHUNK_UID : SpanHeader.OTEL_SPAN_CHUNK;
        }
        return serviceUid ? SpanHeader.SPAN_CHUNK_UID : SpanHeader.SPAN_CHUNK;
    }
}