/*
 * Copyright 2026 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.otlp.trace.collector;

/**
 * Why a span was rejected during mapping (a client-side data fault reported back through the OTLP
 * {@code ExportTracePartialSuccess} and counted per reason in the
 * {@code collector.otlptrace.span.rejected} metric). The {@link #tagValue()} is the stable
 * identifier used both as the metric {@code reason} tag and in the README, so a rename here is a
 * dashboard change.
 */
public enum OtlpTraceRejectReason {
    /** W3C trace-flags present with the sampled bit clear: the span is not part of a sampled trace. */
    UNSAMPLED("unsampled", "unsampled span"),
    /** Malformed trace/span/parent id (wrong length or all-zero). */
    INVALID_ID("invalid_id", "invalid id"),
    /** The ResourceSpans carries no usable application/agent identifier (validation failure). */
    INVALID_RESOURCE("invalid_resource", "invalid resource"),
    /** A non-root span whose parent is not in the request, so it could not be linked into a trace. */
    ORPHAN("orphan", "orphan span"),
    /** The mapper threw while converting a root span or a span chunk (collector-side fault candidate). */
    MAPPING_ERROR("mapping_error", "mapping error");

    private final String tagValue;
    private final String message;

    OtlpTraceRejectReason(String tagValue, String message) {
        this.tagValue = tagValue;
        this.message = message;
    }

    /** Metric tag value (snake_case), also the identifier documented in the README. */
    public String tagValue() {
        return tagValue;
    }

    /** Human-readable label used in the partial-success error message, e.g. {@code invalid id (3)}. */
    public String message() {
        return message;
    }
}
