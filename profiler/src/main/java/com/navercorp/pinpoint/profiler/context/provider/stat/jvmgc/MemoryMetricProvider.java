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

package com.navercorp.pinpoint.profiler.context.provider.stat.jvmgc;

import com.codahale.metrics.Metric;
import com.codahale.metrics.jvm.MemoryUsageGaugeSet;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.navercorp.pinpoint.profiler.monitor.codahale.MetricMonitorValues;
import com.navercorp.pinpoint.profiler.monitor.metric.memory.CmsGcMemoryMetric;
import com.navercorp.pinpoint.profiler.monitor.metric.memory.G1GcMemoryMetric;
import com.navercorp.pinpoint.profiler.monitor.metric.memory.MemoryMetric;
import com.navercorp.pinpoint.profiler.monitor.metric.memory.ParallelGcMemoryMetric;
import com.navercorp.pinpoint.profiler.monitor.metric.memory.SerialGcMemoryMetric;
import com.navercorp.pinpoint.profiler.monitor.metric.memory.UnknownMemoryMetric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;

/**
 * @author dawidmalina
 * @author HyunGil Jeong
 */
public class MemoryMetricProvider implements Provider<MemoryMetric> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final MemoryUsageGaugeSet memoryUsageGaugeSet = new MemoryUsageGaugeSet();

    @Inject
    public MemoryMetricProvider() {
    }

    @Override
    public MemoryMetric get() {
        Map<String, Metric> memoryUsageMetrics = memoryUsageGaugeSet.getMetrics();
        Set<String> metricNames = memoryUsageMetrics.keySet();

        MemoryMetric memoryMetric;
        if (metricNames.contains(MetricMonitorValues.METRIC_MEMORY_POOLS_SERIAL_OLDGEN_USAGE)) {
            memoryMetric = new SerialGcMemoryMetric(memoryUsageMetrics);
        } else if (metricNames.contains(MetricMonitorValues.METRIC_MEMORY_POOLS_PS_OLDGEN_USAGE)) {
            memoryMetric = new ParallelGcMemoryMetric(memoryUsageMetrics);
        } else if (metricNames.contains(MetricMonitorValues.METRIC_MEMORY_POOLS_CMS_OLDGEN_USAGE)) {
            memoryMetric = new CmsGcMemoryMetric(memoryUsageMetrics);
        } else if (metricNames.contains(MetricMonitorValues.METRIC_MEMORY_POOLS_G1_OLDGEN_USAGE)) {
            memoryMetric = new G1GcMemoryMetric(memoryUsageMetrics);
        } else {
            memoryMetric = new UnknownMemoryMetric(memoryUsageMetrics);
        }
        logger.info("loaded : {}", memoryMetric);
        return memoryMetric;
    }
}
