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
import io.opentelemetry.proto.common.v1.KeyValue;
import io.opentelemetry.proto.metrics.v1.AggregationTemporality;
import io.opentelemetry.proto.metrics.v1.ExponentialHistogram;
import io.opentelemetry.proto.metrics.v1.ExponentialHistogramDataPoint;
import io.opentelemetry.proto.metrics.v1.Gauge;
import io.opentelemetry.proto.metrics.v1.Histogram;
import io.opentelemetry.proto.metrics.v1.HistogramDataPoint;
import io.opentelemetry.proto.metrics.v1.Metric;
import io.opentelemetry.proto.metrics.v1.NumberDataPoint;
import io.opentelemetry.proto.metrics.v1.Sum;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Builds OTLP metrics shaped like the OTel Java agent's runtime-telemetry output: gauges for
 * utilizations, UpDownCounter sums for memory/threads/classes/buffers, cumulative histograms for
 * GC and HTTP durations.
 */
final class MetricFixtures {

    static final long START_NS = TimeUnit.MILLISECONDS.toNanos(1_700_000_000_000L);
    static final long T1_NS = START_NS + TimeUnit.SECONDS.toNanos(60);
    static final long T2_NS = START_NS + TimeUnit.SECONDS.toNanos(120);

    private MetricFixtures() {
    }

    static List<KeyValue> attrs(String... keyValues) {
        final List<KeyValue> list = new ArrayList<>();
        for (int i = 0; i < keyValues.length; i += 2) {
            list.add(KeyValue.newBuilder().setKey(keyValues[i])
                    .setValue(AnyValue.newBuilder().setStringValue(keyValues[i + 1]).build()).build());
        }
        return list;
    }

    static NumberDataPoint intPoint(long value, long timeNs, String... attrs) {
        return NumberDataPoint.newBuilder().setAsInt(value).setTimeUnixNano(timeNs).setStartTimeUnixNano(START_NS)
                .addAllAttributes(attrs(attrs)).build();
    }

    static NumberDataPoint doublePoint(double value, long timeNs, String... attrs) {
        return NumberDataPoint.newBuilder().setAsDouble(value).setTimeUnixNano(timeNs).setStartTimeUnixNano(START_NS)
                .addAllAttributes(attrs(attrs)).build();
    }

    static Metric gauge(String name, String unit, NumberDataPoint... points) {
        return Metric.newBuilder().setName(name).setUnit(unit)
                .setGauge(Gauge.newBuilder().addAllDataPoints(List.of(points))).build();
    }

    /** UpDownCounter: cumulative, non-monotonic (what the SDK sends for memory/threads/classes). */
    static Metric upDownCounter(String name, String unit, NumberDataPoint... points) {
        return Metric.newBuilder().setName(name).setUnit(unit)
                .setSum(Sum.newBuilder().setIsMonotonic(false)
                        .setAggregationTemporality(AggregationTemporality.AGGREGATION_TEMPORALITY_CUMULATIVE)
                        .addAllDataPoints(List.of(points))).build();
    }

    static Metric counter(String name, String unit, AggregationTemporality temporality, NumberDataPoint... points) {
        return Metric.newBuilder().setName(name).setUnit(unit)
                .setSum(Sum.newBuilder().setIsMonotonic(true).setAggregationTemporality(temporality)
                        .addAllDataPoints(List.of(points))).build();
    }

    static HistogramDataPoint histogramPoint(long count, double sum, long timeNs, List<Double> bounds, List<Long> counts, String... attrs) {
        return histogramPoint(count, sum, timeNs, START_NS, bounds, counts, attrs);
    }

    static HistogramDataPoint histogramPoint(long count, double sum, long timeNs, long startNs,
                                             List<Double> bounds, List<Long> counts, String... attrs) {
        return HistogramDataPoint.newBuilder().setCount(count).setSum(sum)
                .setTimeUnixNano(timeNs).setStartTimeUnixNano(startNs)
                .addAllExplicitBounds(bounds).addAllBucketCounts(counts)
                .addAllAttributes(attrs(attrs)).build();
    }

    static Metric histogram(String name, String unit, AggregationTemporality temporality, HistogramDataPoint... points) {
        return Metric.newBuilder().setName(name).setUnit(unit)
                .setHistogram(Histogram.newBuilder().setAggregationTemporality(temporality)
                        .addAllDataPoints(List.of(points))).build();
    }

    static Metric exponentialHistogram(String name) {
        return Metric.newBuilder().setName(name).setUnit("s")
                .setExponentialHistogram(ExponentialHistogram.newBuilder()
                        .setAggregationTemporality(AggregationTemporality.AGGREGATION_TEMPORALITY_CUMULATIVE)
                        .addDataPoints(ExponentialHistogramDataPoint.newBuilder().setCount(1).setTimeUnixNano(T1_NS)))
                .build();
    }
}
