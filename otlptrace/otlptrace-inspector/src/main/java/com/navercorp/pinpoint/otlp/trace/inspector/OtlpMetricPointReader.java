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

import io.opentelemetry.proto.common.v1.AnyValue;
import io.opentelemetry.proto.common.v1.InstrumentationScope;
import io.opentelemetry.proto.common.v1.KeyValue;
import io.opentelemetry.proto.metrics.v1.DataPointFlags;
import io.opentelemetry.proto.metrics.v1.HistogramDataPoint;
import io.opentelemetry.proto.metrics.v1.Metric;
import io.opentelemetry.proto.metrics.v1.NumberDataPoint;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.function.IntConsumer;

/**
 * Flattens an OTLP {@link Metric} into {@link MetricPoint}s. Gauge, Sum and Histogram are read;
 * ExponentialHistogram and Summary return {@code null} so the caller can count them as unsupported.
 */
public final class OtlpMetricPointReader {

    private OtlpMetricPointReader() {
    }

    /** Number of data points of the metric, whatever its type, for the received counter. */
    public static int countPoints(Metric metric) {
        return switch (metric.getDataCase()) {
            case GAUGE -> metric.getGauge().getDataPointsCount();
            case SUM -> metric.getSum().getDataPointsCount();
            case HISTOGRAM -> metric.getHistogram().getDataPointsCount();
            case EXPONENTIAL_HISTOGRAM -> metric.getExponentialHistogram().getDataPointsCount();
            case SUMMARY -> metric.getSummary().getDataPointsCount();
            default -> 0;
        };
    }

    /** @return the points, or {@code null} when the metric's data type is not supported */
    public static @Nullable List<MetricPoint> read(Metric metric) {
        return read(metric, MetricPoint.NO_SCOPE, skipped -> { });
    }

    /**
     * Reads the points of a metric of one instrumentation scope.
     *
     * @param scope          the scope key ({@link #scopeKey}); part of each point's series identity
     * @param noRecordedValue receives the number of points that carry no value: the
     *                       {@code FLAG_NO_RECORDED_VALUE} data point flag (an upstream staleness
     *                       marker, e.g. the Prometheus receiver's) or a number point without a value
     *                       field. They are left out, so they neither reach a chart nor move a
     *                       series' state.
     * @return the points, or {@code null} when the metric's data type is not supported
     */
    public static @Nullable List<MetricPoint> read(Metric metric, String scope, IntConsumer noRecordedValue) {
        final String name = metric.getName();
        final String unit = metric.getUnit();
        int skipped = 0;
        final List<MetricPoint> result;
        switch (metric.getDataCase()) {
            case GAUGE: {
                final List<NumberDataPoint> points = metric.getGauge().getDataPointsList();
                result = new ArrayList<>(points.size());
                for (NumberDataPoint point : points) {
                    if (hasNoValue(point)) {
                        skipped++;
                        continue;
                    }
                    result.add(number(name, unit, scope, point, PointKind.GAUGE, Temporality.UNSPECIFIED, false));
                }
                break;
            }
            case SUM: {
                final Temporality temporality = Temporality.of(metric.getSum().getAggregationTemporality());
                final boolean monotonic = metric.getSum().getIsMonotonic();
                final List<NumberDataPoint> points = metric.getSum().getDataPointsList();
                result = new ArrayList<>(points.size());
                for (NumberDataPoint point : points) {
                    if (hasNoValue(point)) {
                        skipped++;
                        continue;
                    }
                    result.add(number(name, unit, scope, point, PointKind.SUM, temporality, monotonic));
                }
                break;
            }
            case HISTOGRAM: {
                final Temporality temporality = Temporality.of(metric.getHistogram().getAggregationTemporality());
                final List<HistogramDataPoint> points = metric.getHistogram().getDataPointsList();
                result = new ArrayList<>(points.size());
                for (HistogramDataPoint point : points) {
                    if (isNoRecordedValue(point.getFlags())) {
                        skipped++;
                        continue;
                    }
                    result.add(histogram(name, unit, scope, point, temporality));
                }
                break;
            }
            default:
                return null;
        }
        if (skipped > 0) {
            noRecordedValue.accept(skipped);
        }
        return result;
    }

    /**
     * Series identity of an instrumentation scope: its name, plus {@code @version} when set. Two
     * scopes may report the same metric name and attributes; per the OTLP data model they are
     * different streams.
     */
    public static String scopeKey(InstrumentationScope scope) {
        final String name = scope.getName();
        final String version = scope.getVersion();
        if (version.isEmpty()) {
            return name;
        }
        return name + "@" + version;
    }

    private static boolean hasNoValue(NumberDataPoint point) {
        return isNoRecordedValue(point.getFlags()) || point.getValueCase() == NumberDataPoint.ValueCase.VALUE_NOT_SET;
    }

    private static boolean isNoRecordedValue(int flags) {
        return (flags & DataPointFlags.DATA_POINT_FLAGS_NO_RECORDED_VALUE_MASK_VALUE) != 0;
    }

    private static MetricPoint number(String name, String unit, String scope, NumberDataPoint point,
                                      PointKind kind, Temporality temporality, boolean monotonic) {
        final double value;
        if (point.getValueCase() == NumberDataPoint.ValueCase.AS_INT) {
            value = point.getAsInt();
        } else {
            value = point.getAsDouble();
        }
        return new MetricPoint(name, unit, scope, attributes(point.getAttributesList()),
                toMillis(point.getTimeUnixNano()), toMillis(point.getStartTimeUnixNano()),
                kind, temporality, monotonic, value, null);
    }

    private static MetricPoint histogram(String name, String unit, String scope, HistogramDataPoint point, Temporality temporality) {
        // sum is optional in OTLP: without it neither the mean nor the total time is known, so it is
        // carried as NaN and the mapper drops the point (invalid_point) before it touches any state
        final HistogramValue value = new HistogramValue(point.getCount(), point.hasSum() ? point.getSum() : Double.NaN,
                point.hasMax(), point.getMax(),
                List.copyOf(point.getExplicitBoundsList()), List.copyOf(point.getBucketCountsList()));
        return new MetricPoint(name, unit, scope, attributes(point.getAttributesList()),
                toMillis(point.getTimeUnixNano()), toMillis(point.getStartTimeUnixNano()),
                PointKind.HISTOGRAM, temporality, true, 0, value);
    }

    static Map<String, String> attributes(List<KeyValue> keyValues) {
        if (keyValues.isEmpty()) {
            return Map.of();
        }
        final Map<String, String> map = new TreeMap<>();
        for (KeyValue keyValue : keyValues) {
            final String value = toString(keyValue.getValue());
            if (value != null) {
                map.put(keyValue.getKey(), value);
            }
        }
        return map;
    }

    private static @Nullable String toString(AnyValue value) {
        return switch (value.getValueCase()) {
            case STRING_VALUE -> value.getStringValue();
            case INT_VALUE -> Long.toString(value.getIntValue());
            case DOUBLE_VALUE -> Double.toString(value.getDoubleValue());
            case BOOL_VALUE -> Boolean.toString(value.getBoolValue());
            default -> null;
        };
    }

    private static long toMillis(long unixNano) {
        return TimeUnit.NANOSECONDS.toMillis(unixNano);
    }
}
