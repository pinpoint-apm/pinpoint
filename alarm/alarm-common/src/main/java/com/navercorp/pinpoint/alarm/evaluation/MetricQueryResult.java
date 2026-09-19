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
package com.navercorp.pinpoint.alarm.evaluation;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Metric values for condition evaluation, plus optional human readable detail
 * lines per metric (e.g. the newly seen error groups behind a NEW_GROUP count)
 * and the time range the queries actually covered.
 * <p>
 * Details are plain strings so that this module stays independent of any data
 * source specific model.
 */
public record MetricQueryResult(Map<MetricQueryKey, Double> values,
                                Map<MetricQueryKey, List<String>> details,
                                QueriedRange range) {

    private static final MetricQueryResult EMPTY = new MetricQueryResult(Map.of(), Map.of(), null);

    public MetricQueryResult {
        // LinkedHashMap, not Map.copyOf: an immutable map randomizes its iteration order
        // per JVM, and the message layout reads the metrics in order (${metric} takes the
        // first one), so the same rule would render differently after every restart.
        values = unmodifiableCopy(Objects.requireNonNull(values, "values"));
        details = unmodifiableCopy(Objects.requireNonNull(details, "details"));
    }

    private static <V> Map<MetricQueryKey, V> unmodifiableCopy(Map<MetricQueryKey, V> map) {
        return Collections.unmodifiableMap(new LinkedHashMap<>(map));
    }

    public static MetricQueryResult empty() {
        return EMPTY;
    }

    public static MetricQueryResult of(Map<MetricQueryKey, Double> values) {
        return new MetricQueryResult(values, Map.of(), null);
    }

    public static MetricQueryResult of(Map<MetricQueryKey, Double> values, QueriedRange range) {
        return new MetricQueryResult(values, Map.of(), range);
    }

    public List<String> details(MetricQueryKey key) {
        return details.getOrDefault(key, List.of());
    }

    public boolean isEmpty() {
        return values.isEmpty();
    }

    /**
     * The range the queries behind these values covered, in epoch millis, so that a message
     * links to exactly the data the rule was evaluated on instead of re-deriving the range.
     * Null when no query ran at all, as on a check failure.
     */
    public record QueriedRange(long fromMs, long toMs) {

        /**
         * Widest of the two: a rule with several leaves queries several ranges. {@code left}
         * is the accumulator and starts out null; {@code right} is always a queried range.
         */
        public static QueriedRange union(QueriedRange left, QueriedRange right) {
            if (left == null) {
                return right;
            }
            return new QueriedRange(Math.min(left.fromMs, right.fromMs), Math.max(left.toMs, right.toMs));
        }
    }
}
