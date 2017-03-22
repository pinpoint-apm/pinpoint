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
 * @author HyunGil Jeong
 */
public abstract class CommonMemoryMetric implements MemoryMetric {

    private final Gauge<Long> heapMaxGauge;
    private final Gauge<Long> heapUsedGauge;
    private final Gauge<Long> nonHeapMaxGauge;
    private final Gauge<Long> nonHeapUsedGauge;

    @SuppressWarnings("unchecked")
    protected CommonMemoryMetric(Map<String, Metric> memoryUsageMetrics) {
        if (memoryUsageMetrics == null) {
            throw new NullPointerException("memoryUsageMetrics must not be null");
        }
        heapMaxGauge = (Gauge<Long>) MetricMonitorValues.getMetric(memoryUsageMetrics, MetricMonitorValues.METRIC_MEMORY_HEAP_MAX, EMPTY_LONG_GAUGE);
        heapUsedGauge = (Gauge<Long>) MetricMonitorValues.getMetric(memoryUsageMetrics, MetricMonitorValues.METRIC_MEMORY_HEAP_USED, EMPTY_LONG_GAUGE);
        nonHeapMaxGauge = (Gauge<Long>) MetricMonitorValues.getMetric(memoryUsageMetrics, MetricMonitorValues.METRIC_MEMORY_NONHEAP_MAX, EMPTY_LONG_GAUGE);
        nonHeapUsedGauge = (Gauge<Long>) MetricMonitorValues.getMetric(memoryUsageMetrics, MetricMonitorValues.METRIC_MEMORY_NONHEAP_USED, EMPTY_LONG_GAUGE);
    }

    @Override
    public Long heapMax() {
        return heapMaxGauge.getValue();
    }

    @Override
    public Long heapUsed() {
        return heapUsedGauge.getValue();
    }

    @Override
    public Long nonHeapMax() {
        return nonHeapMaxGauge.getValue();
    }

    @Override
    public Long nonHeapUsed() {
        return nonHeapUsedGauge.getValue();
    }
}
