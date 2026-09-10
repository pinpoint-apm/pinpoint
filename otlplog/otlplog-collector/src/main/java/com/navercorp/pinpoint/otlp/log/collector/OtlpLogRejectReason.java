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

package com.navercorp.pinpoint.otlp.log.collector;

/**
 * Why a LogRecord was not stored. Every reason is counted in
 * {@code collector.otlplog.record.dropped{reason}}; only the reasons flagged
 * {@link #isClientVisible() client-visible} are also reported back in the OTLP
 * {@code ExportLogsPartialSuccess}. The rest are the receiver's own selection (non-exception
 * logs, blacklist, in-request duplicates, unsampled traces) — accepted and discarded on purpose,
 * so they must not make the exporter log a "records rejected" warning.
 *
 * <p>{@link #tagValue()} is the stable identifier used as the metric tag and in the README; renaming
 * one is a dashboard change.
 */
public enum OtlpLogRejectReason {
    /** No {@code exception.*} attribute: an ordinary log line, out of scope by design. */
    NO_EXCEPTION("no_exception", "no exception attributes", false),
    /** Instrumentation scope or application matched the configured blacklist prefixes. */
    BLACKLISTED("blacklisted", "blacklisted", false),
    /** The ResourceLogs carries no usable application/agent identifier (validation failure). */
    INVALID_RESOURCE("invalid_resource", "invalid resource", true),
    /** Missing or malformed {@code trace_id}/{@code span_id}: logged outside any span, nothing to correlate. */
    NO_TRACE_CONTEXT("no_trace_context", "no trace context", true),
    /** Trace flags say the trace was not sampled and storing such records is disabled. */
    UNSAMPLED_CONTEXT("unsampled_context", "unsampled trace context", false),
    /** Another record in the same request carried the same (traceId, spanId, exception.type) with a better stack trace. */
    DUPLICATE("duplicate", "duplicate", false),
    /** {@code exception.*} attributes present but no resolvable exception type ({@code exception.type} / {@code error.type}). */
    NO_EXCEPTION_TYPE("no_exception_type", "no exception type", true),
    /** The mapper threw while converting the record (collector-side fault candidate). */
    MAPPING_ERROR("mapping_error", "mapping error", true),
    /** The exceptiontrace storage module is not enabled in this process, so there is nowhere to store. */
    STORAGE_UNAVAILABLE("storage_unavailable", "storage unavailable", false);

    private final String tagValue;
    private final String message;
    private final boolean clientVisible;

    OtlpLogRejectReason(String tagValue, String message, boolean clientVisible) {
        this.tagValue = tagValue;
        this.message = message;
        this.clientVisible = clientVisible;
    }

    /** Metric tag value (snake_case), also the identifier documented in the README. */
    public String tagValue() {
        return tagValue;
    }

    /** Human-readable label used in the partial-success error message, e.g. {@code no trace context (3)}. */
    public String message() {
        return message;
    }

    /** Whether the drop is reported to the exporter as a rejected record (OTLP partial success). */
    public boolean isClientVisible() {
        return clientVisible;
    }
}
