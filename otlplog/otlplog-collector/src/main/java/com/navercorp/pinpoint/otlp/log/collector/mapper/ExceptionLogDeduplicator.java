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

package com.navercorp.pinpoint.otlp.log.collector.mapper;

import com.google.protobuf.ByteString;
import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.otlp.trace.collector.mapper.ExceptionAttributeUtils;
import com.navercorp.pinpoint.otlp.trace.collector.mapper.OtlpTraceConstants;
import io.opentelemetry.proto.common.v1.KeyValue;
import io.opentelemetry.proto.logs.v1.LogRecord;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * In-request de-duplication of exception LogRecords. One exception is typically emitted twice by the
 * same SDK — once by the instrumentation ({@code recordException} routed to logs) and once by the
 * logging appender the application's own {@code log.error(msg, e)} goes through — and both records
 * carry the same {@code (trace_id, span_id, exception.type)}. Because they pass through the same
 * BatchLogRecordProcessor they almost always travel in the same export request, so a per-request
 * table is enough; there is intentionally no cache across requests (memory must not scale with
 * volume), and a duplicate that straddles a batch boundary is simply stored twice.
 *
 * <p>Winner rule: the record that carries a stack trace, then the longer stack trace, then the
 * first seen. Instrumentation records usually carry the complete stack; appender records may carry
 * only type and message.
 *
 * <p>Not thread-safe: one instance per export request.
 */
public class ExceptionLogDeduplicator {

    /**
     * {@code untypedOrdinal} is 0 for every record with a resolved type; a record without one gets its
     * own ordinal so that it never collides with another record (see {@link #offer}).
     */
    private record Key(ByteString traceId, ByteString spanId, String exceptionType, int untypedOrdinal) {
    }

    private final Map<Key, ExceptionLogCandidate> winners = new LinkedHashMap<>();
    private int untypedRecords = 0;

    /**
     * @return {@code true} for the first record of a key, {@code false} for every later record with
     * the same key (the caller counts it as a duplicate). A later record with a longer stack trace
     * still takes over as the key's winner; either way exactly one record per key survives and one
     * duplicate per collision is counted.
     *
     * <p>A record whose type cannot be resolved (see {@link #exceptionType}) is never a duplicate: the
     * mapper rejects each such record as {@code no_exception_type}, and folding them into one key here
     * would report one rejection for several dropped records.
     */
    public boolean offer(ExceptionLogCandidate candidate) {
        final Key key = keyOf(candidate.record());
        final ExceptionLogCandidate current = winners.get(key);
        if (current == null) {
            winners.put(key, candidate);
            return true;
        }
        if (stackTraceLength(candidate.record()) > stackTraceLength(current.record())) {
            winners.put(key, candidate);
        }
        return false;
    }

    /** The surviving candidate per key, in first-seen key order. */
    public Collection<ExceptionLogCandidate> winners() {
        return winners.values();
    }

    private Key keyOf(LogRecord record) {
        final String exceptionType = exceptionType(record);
        if (exceptionType == null) {
            return new Key(record.getTraceId(), record.getSpanId(), null, ++untypedRecords);
        }
        return new Key(record.getTraceId(), record.getSpanId(), exceptionType, 0);
    }

    /**
     * The type the mapper will store the record under: a non-empty {@code exception.type}, else a
     * non-empty {@code error.type}, else {@code null}. Same semantics as
     * {@link ExceptionAttributeUtils#resolveExceptionType} (an empty or non-string {@code exception.type}
     * counts as absent) so that two records the mapper would store under the same type share one key.
     */
    static String exceptionType(LogRecord record) {
        String errorType = null;
        for (KeyValue attribute : record.getAttributesList()) {
            final String key = attribute.getKey();
            if (OtlpTraceConstants.ATTRIBUTE_KEY_EXCEPTION_TYPE.equals(key)) {
                final String exceptionType = attribute.getValue().getStringValue();
                if (StringUtils.hasLength(exceptionType)) {
                    return exceptionType;
                }
            } else if (OtlpTraceConstants.ATTRIBUTE_KEY_ERROR_TYPE.equals(key)) {
                final String candidate = attribute.getValue().getStringValue();
                if (StringUtils.hasLength(candidate)) {
                    errorType = candidate;
                }
            }
        }
        return errorType;
    }

    static int stackTraceLength(LogRecord record) {
        for (KeyValue attribute : record.getAttributesList()) {
            if (OtlpTraceConstants.ATTRIBUTE_KEY_EXCEPTION_STACKTRACE.equals(attribute.getKey())) {
                return attribute.getValue().getStringValue().length();
            }
        }
        return 0;
    }
}
