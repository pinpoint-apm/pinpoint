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

package com.navercorp.pinpoint.otlp.trace.collector.mapper;

import com.google.protobuf.ByteString;
import com.navercorp.pinpoint.common.PinpointConstants;
import com.navercorp.pinpoint.common.server.util.ByteStringUtils;
import com.navercorp.pinpoint.common.util.BytesUtils;

/**
 * Validates OTLP trace/span identifiers at the ingestion boundary, per the OTel Trace API: a trace ID
 * is exactly 16 bytes and a span ID exactly 8 bytes, and neither may be all-zero.
 *
 * <p>Without this, the previous conversion accepted invalid IDs: an over-8-byte span ID was silently
 * truncated to its first 8 bytes (distinct IDs could collide) and an all-zero trace/span ID passed
 * through. Validating here rejects those as client-data faults.
 *
 * <p>The throwing {@code validate*} forms raise {@link IllegalArgumentException} so the caller counts
 * the span as a client-side reject (reflected in {@code rejected_spans}), never as a server error.
 * The boolean {@code isValid*} forms are for callers that filter/skip without throwing (span map
 * gate, link drop).
 *
 * <p>Kept separate from {@link com.navercorp.pinpoint.common.server.trace.OtelServerTraceId}, which is
 * a storage/read-back type that must stay permissive; validation belongs only on the ingestion path.
 */
public final class OtlpIdValidator {

    private static final int TRACE_ID_LEN = PinpointConstants.OPENTELEMETRY_TRACE_ID_LEN; // 16
    private static final int SPAN_ID_LEN = BytesUtils.LONG_BYTE_LENGTH; // 8

    private OtlpIdValidator() {
    }

    public static boolean isValidTraceId(ByteString traceId) {
        return traceIdError(traceId) == null;
    }

    public static boolean isValidSpanId(ByteString spanId) {
        return spanIdError(spanId) == null;
    }

    /**
     * A parent span reference is valid when it is either absent (a root span) or itself a valid span ID.
     * A present-but-malformed parent is treated as invalid so the owning span can be rejected.
     */
    public static boolean isValidParentSpanId(ByteString parentSpanId) {
        return ByteStringUtils.isEmpty(parentSpanId) || isValidSpanId(parentSpanId);
    }

    /**
     * @return the 16-byte trace ID
     * @throws IllegalArgumentException if empty, not 16 bytes, or all-zero
     */
    public static byte[] validateTraceId(ByteString traceId) {
        final String error = traceIdError(traceId);
        if (error != null) {
            throw new IllegalArgumentException("invalid traceId: " + error);
        }
        return traceId.toByteArray();
    }

    /**
     * @return the span ID as a big-endian long
     * @throws IllegalArgumentException if empty, not 8 bytes, or all-zero
     */
    public static long validateSpanId(ByteString spanId) {
        final String error = spanIdError(spanId);
        if (error != null) {
            throw new IllegalArgumentException("invalid spanId: " + error);
        }
        return ByteStringUtils.parseLong(spanId);
    }

    private static String traceIdError(ByteString traceId) {
        return idError(traceId, TRACE_ID_LEN);
    }

    private static String spanIdError(ByteString spanId) {
        return idError(spanId, SPAN_ID_LEN);
    }

    private static String idError(ByteString id, int expectedLen) {
        if (ByteStringUtils.isEmpty(id)) {
            return "empty";
        }
        if (id.size() != expectedLen) {
            return "length " + id.size() + " (expected " + expectedLen + ")";
        }
        if (isAllZero(id)) {
            return "all-zero";
        }
        return null;
    }

    private static boolean isAllZero(ByteString bytes) {
        for (int i = 0; i < bytes.size(); i++) {
            if (bytes.byteAt(i) != 0) {
                return false;
            }
        }
        return true;
    }
}
