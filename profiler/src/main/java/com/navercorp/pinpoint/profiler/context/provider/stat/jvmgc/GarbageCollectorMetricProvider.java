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
import com.codahale.metrics.jvm.GarbageCollectorMetricSet;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.navercorp.pinpoint.profiler.monitor.codahale.MetricMonitorValues;
import com.navercorp.pinpoint.profiler.monitor.metric.gc.CmsGcGarbageCollectorMetric;
import com.navercorp.pinpoint.profiler.monitor.metric.gc.G1GcGarbageCollectorMetric;
import com.navercorp.pinpoint.profiler.monitor.metric.gc.GarbageCollectorMetric;
import com.navercorp.pinpoint.profiler.monitor.metric.gc.ParallelGcGarbageCollectorMetric;
import com.navercorp.pinpoint.profiler.monitor.metric.gc.SerialGcGarbageCollectorMetric;
import com.navercorp.pinpoint.profiler.monitor.metric.gc.UnknownGarbageCollectorMetric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;

/**
 * @author dawidmalina
 * @author HyunGil Jeong
 */
public class GarbageCollectorMetricProvider implements Provider<GarbageCollectorMetric> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final GarbageCollectorMetricSet garbageCollectorMetricSet = new GarbageCollectorMetricSet();

    @Inject
    public GarbageCollectorMetricProvider() {
    }

    @Override
    public GarbageCollectorMetric get() {
        Map<String, Metric> garbageCollectorMetrics = garbageCollectorMetricSet.getMetrics();
        Set<String> metricNames = garbageCollectorMetrics.keySet();

        GarbageCollectorMetric garbageCollectorMetric;
        if (metricNames.contains(MetricMonitorValues.METRIC_GC_SERIAL_OLDGEN_COUNT)) {
            garbageCollectorMetric = new SerialGcGarbageCollectorMetric(garbageCollectorMetrics);
        } else if (metricNames.contains(MetricMonitorValues.METRIC_GC_PS_OLDGEN_COUNT)) {
            garbageCollectorMetric = new ParallelGcGarbageCollectorMetric(garbageCollectorMetrics);
        } else if (metricNames.contains(MetricMonitorValues.METRIC_GC_CMS_OLDGEN_COUNT)) {
            garbageCollectorMetric = new CmsGcGarbageCollectorMetric(garbageCollectorMetrics);
        } else if (metricNames.contains(MetricMonitorValues.METRIC_GC_G1_OLDGEN_COUNT)) {
            garbageCollectorMetric = new G1GcGarbageCollectorMetric(garbageCollectorMetrics);
        } else {
            garbageCollectorMetric = new UnknownGarbageCollectorMetric();
        }
        logger.info("loaded : {}", garbageCollectorMetric);
        return garbageCollectorMetric;
    }
}
