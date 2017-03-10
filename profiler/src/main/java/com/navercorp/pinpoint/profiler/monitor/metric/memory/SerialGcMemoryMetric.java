/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.monitor.metric.memory;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.Metric;
import com.navercorp.pinpoint.profiler.monitor.codahale.MetricMonitorValues;

import java.util.Map;

/**
 * HotSpot's Serial gc memory metrics
 *
 * @author dawidmalina
 * @author HyunGil Jeong
 */
public class SerialGcMemoryMetric extends CommonMemoryMetric {

    private final Gauge<Double> newGenUsageGauge;
    private final Gauge<Double> oldGenUsageGauge;
    private final Gauge<Double> codeCacheUsageGauge;
    private final Gauge<Double> survivorUsageGauge;
    private final Gauge<Double> permGenUsageGauge;
    private final Gauge<Double> metaspaceUsageGauge;

    @SuppressWarnings("unchecked")
    public SerialGcMemoryMetric(Map<String, Metric> memoryUsageMetrics) {
        super(memoryUsageMetrics);
        newGenUsageGauge = (Gauge<Double>) MetricMonitorValues.getMetric(memoryUsageMetrics, MetricMonitorValues.METRIC_MEMORY_POOLS_SERIAL_NEWGEN_USAGE, EMPTY_DOUBLE_GAUGE);
        oldGenUsageGauge = (Gauge<Double>) MetricMonitorValues.getMetric(memoryUsageMetrics, MetricMonitorValues.METRIC_MEMORY_POOLS_SERIAL_OLDGEN_USAGE, EMPTY_DOUBLE_GAUGE);
        codeCacheUsageGauge = (Gauge<Double>) MetricMonitorValues.getMetric(memoryUsageMetrics, MetricMonitorValues.METRIC_MEMORY_POOLS_SERIAL_CODE_CACHE_USAGE, EMPTY_DOUBLE_GAUGE);
        survivorUsageGauge = (Gauge<Double>) MetricMonitorValues.getMetric(memoryUsageMetrics, MetricMonitorValues.METRIC_MEMORY_POOLS_SERIAL_SURVIVOR_USAGE, EMPTY_DOUBLE_GAUGE);
        if (memoryUsageMetrics.containsKey(MetricMonitorValues.METRIC_MEMORY_POOLS_SERIAL_PERMGEN_USAGE)) {
            permGenUsageGauge = (Gauge<Double>) MetricMonitorValues.getMetric(memoryUsageMetrics, MetricMonitorValues.METRIC_MEMORY_POOLS_SERIAL_PERMGEN_USAGE, EMPTY_DOUBLE_GAUGE);
            metaspaceUsageGauge = MetricMonitorValues.EXCLUDED_DOUBLE;
        } else {
            permGenUsageGauge = MetricMonitorValues.EXCLUDED_DOUBLE;
            metaspaceUsageGauge = (Gauge<Double>) MetricMonitorValues.getMetric(memoryUsageMetrics, MetricMonitorValues.METRIC_MEMORY_POOLS_SERIAL_METASPACE_USAGE, EMPTY_DOUBLE_GAUGE);
        }
    }

    @Override
    public Double newGenUsage() {
        return newGenUsageGauge.getValue();
    }

    @Override
    public Double oldGenUsage() {
        return oldGenUsageGauge.getValue();
    }

    @Override
    public Double codeCacheUsage() {
        return codeCacheUsageGauge.getValue();
    }

    @Override
    public Double survivorUsage() {
        return survivorUsageGauge.getValue();
    }

    @Override
    public Double permGenUsage() {
        return permGenUsageGauge.getValue();
    }

    @Override
    public Double metaspaceUsage() {
        return metaspaceUsageGauge.getValue();
    }

    @Override
    public String toString() {
        return "HotSpot's Serial gc memory metrics";
    }
}
