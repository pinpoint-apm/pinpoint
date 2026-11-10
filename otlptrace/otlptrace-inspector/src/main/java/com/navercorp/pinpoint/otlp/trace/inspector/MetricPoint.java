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

import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

/**
 * One OTLP data point, flattened: the metric name and unit, the point attributes as strings, the
 * timestamps in millis, and either a number ({@code value}) or a histogram.
 *
 * @param scope       the instrumentation scope key ({@code name[@version]}), {@link #NO_SCOPE} when
 *                    unknown; part of the series identity
 * @param startTimeMs {@code start_time_unix_nano} in millis, 0 when unset. For cumulative points it
 *                    is the series start; a change means the process restarted.
 */
public record MetricPoint(String name, String unit, String scope, Map<String, String> attributes,
                          long timeMs, long startTimeMs,
                          PointKind kind, Temporality temporality, boolean monotonic,
                          double value, @Nullable HistogramValue histogram) {

    /** Scope key of a point whose instrumentation scope is not known (or is the empty scope). */
    public static final String NO_SCOPE = "";

    public MetricPoint {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(unit, "unit");
        Objects.requireNonNull(scope, "scope");
        Objects.requireNonNull(attributes, "attributes");
        Objects.requireNonNull(kind, "kind");
        Objects.requireNonNull(temporality, "temporality");
        if (kind == PointKind.HISTOGRAM && histogram == null) {
            throw new IllegalArgumentException("histogram point without histogram value: " + name);
        }
    }

    /** A point without a known instrumentation scope. */
    public MetricPoint(String name, String unit, Map<String, String> attributes,
                       long timeMs, long startTimeMs,
                       PointKind kind, Temporality temporality, boolean monotonic,
                       double value, @Nullable HistogramValue histogram) {
        this(name, unit, NO_SCOPE, attributes, timeMs, startTimeMs, kind, temporality, monotonic, value, histogram);
    }

    public @Nullable String attribute(String key) {
        return attributes.get(key);
    }

    /**
     * A cumulative Sum that is not monotonic (an UpDownCounter such as {@code jvm.memory.used}) is a
     * current value, not a running total: SDKs export UpDownCounters cumulatively even under a DELTA
     * preference. Only monotonic Sums and histograms carry temporality that matters here.
     */
    public boolean isRunningTotal() {
        return kind == PointKind.SUM && monotonic;
    }

    /**
     * Stable identity of the series this point belongs to: the instrumentation scope, the metric name
     * and the sorted attributes. Without a scope it is the {@link #metricKey()}.
     */
    public String seriesKey() {
        final String metricKey = metricKey();
        if (scope.isEmpty()) {
            return metricKey;
        }
        return scope + '|' + metricKey;
    }

    /** The series identity without the scope: metric name plus sorted attributes. */
    public String metricKey() {
        if (attributes.isEmpty()) {
            return name;
        }
        final StringBuilder sb = new StringBuilder(name);
        final Map<String, String> sorted = attributes instanceof TreeMap ? attributes : new TreeMap<>(attributes);
        for (Map.Entry<String, String> entry : sorted.entrySet()) {
            sb.append('{').append(entry.getKey()).append('=').append(entry.getValue()).append('}');
        }
        return sb.toString();
    }
}
