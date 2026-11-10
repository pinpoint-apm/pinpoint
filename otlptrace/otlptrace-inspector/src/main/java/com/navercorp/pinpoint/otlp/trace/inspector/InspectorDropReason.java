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

package com.navercorp.pinpoint.otlp.trace.inspector;

/** Why a data point produced no Inspector value. Reported per point for the collector's counters. */
public enum InspectorDropReason {
    /** The metric name has no role in the dialect (application or library metric). Expected and frequent. */
    UNKNOWN_METRIC("unknown_metric"),
    /** ExponentialHistogram / Summary, or a histogram where a number was expected. */
    UNSUPPORTED_TYPE("unsupported_type"),
    /** Cumulative histogram whose previous point is not cached yet (first export, restart, eviction). */
    NO_PREVIOUS_POINT("no_previous_point"),
    /**
     * A series of a known metric whose attribute value has no Inspector chart, e.g. the JDK 14+ buffer
     * pool {@code mapped - 'non-volatile memory'} next to {@code direct} and {@code mapped}, or a
     * garbage collector the agent's {@code jvmGc} chart does not count (young, G1 concurrent, ZGC
     * cycles). Expected; the metric's other series are mapped.
     */
    IGNORED_SERIES("ignored_series"),
    /**
     * Another source of the same agent reports this Inspector value: the Micrometer bridge or the JMX
     * Insight JVM rules next to the runtime-telemetry metrics, or two dialects in one Resource. Only
     * one source per value is used, so the JVM is not counted twice. Expected while such a bridge is
     * on.
     */
    DUPLICATE_SOURCE("duplicate_source"),
    /**
     * The point carries no value: the {@code FLAG_NO_RECORDED_VALUE} data point flag (an upstream
     * staleness marker) or a number point without a value field. Not a zero; left out of the chart
     * and of the series state.
     */
    NO_RECORDED_VALUE("no_recorded_value"),
    /**
     * A required attribute is missing or a value is not usable (non-finite, negative count, unknown
     * duration unit, histogram without {@code sum}).
     */
    INVALID_POINT("invalid_point");

    private final String tag;

    InspectorDropReason(String tag) {
        this.tag = tag;
    }

    /** Metric tag value. */
    public String tag() {
        return tag;
    }
}
