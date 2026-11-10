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

package com.navercorp.pinpoint.otlp.trace.collector.inspector.service;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

/**
 * Transport-agnostic outcome of one OTLP metrics export. Like the logs path there is no retryable
 * server-error class: the stat store is a Kafka producer whose failures are counted
 * ({@code collector.otlptrace.inspector.insert.error}) rather than turned into a whole-batch retry
 * (a retry would double-count the points that were stored), so the response is a success or an
 * OTLP partial success naming the rejected Resources' points.
 */
public final class OtlpMetricExportResult {

    private final int storedStats;
    private final Map<OtlpMetricResourceRejectReason, Long> rejectedPoints;

    OtlpMetricExportResult(int storedStats, Map<OtlpMetricResourceRejectReason, Long> rejectedPoints) {
        this.storedStats = storedStats;
        this.rejectedPoints = Collections.unmodifiableMap(new EnumMap<>(rejectedPoints));
    }

    /** Resources (one JVM each) whose mapped stat was handed to storage. */
    public int getStoredStats() {
        return storedStats;
    }

    /** Data points of rejected Resources, by reason; only reasons that occurred are present. */
    public Map<OtlpMetricResourceRejectReason, Long> getRejectedPoints() {
        return rejectedPoints;
    }

    public long getRejectedPointCount() {
        long total = 0;
        for (long count : rejectedPoints.values()) {
            total += count;
        }
        return total;
    }

    /** {@code "invalid resource (12), service not found (3)"} in enum order. */
    public String getRejectedMessage() {
        final StringBuilder sb = new StringBuilder();
        for (Map.Entry<OtlpMetricResourceRejectReason, Long> entry : rejectedPoints.entrySet()) {
            if (!sb.isEmpty()) {
                sb.append(", ");
            }
            sb.append(entry.getKey().message()).append(" (").append(entry.getValue()).append(')');
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return "OtlpMetricExportResult{storedStats=" + storedStats + ", rejectedPoints=" + rejectedPoints + '}';
    }
}
