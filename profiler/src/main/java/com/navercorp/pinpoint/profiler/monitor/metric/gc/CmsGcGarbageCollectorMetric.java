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

package com.navercorp.pinpoint.profiler.monitor.metric.gc;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.Metric;
import com.navercorp.pinpoint.profiler.monitor.codahale.MetricMonitorValues;
import com.navercorp.pinpoint.thrift.dto.TJvmGcType;

import java.util.Map;

/**
 * HotSpot's Concurrent-Mark-Sweep gc garbage collector metrics
 *
 * @author dawidmalina
 * @author HyunGil Jeong
 */
public class CmsGcGarbageCollectorMetric implements GarbageCollectorMetric {

    private static final TJvmGcType GC_TYPE = TJvmGcType.CMS;

    private final Gauge<Long> gcOldCountGauge;
    private final Gauge<Long> gcOldTimeGauge;

    private final Gauge<Long> gcNewCountGauge;
    private final Gauge<Long> gcNewTimeGauge;

    @SuppressWarnings("unchecked")
    public CmsGcGarbageCollectorMetric(Map<String, Metric> garbageCollectorMetrics) {
        if (garbageCollectorMetrics == null) {
            throw new NullPointerException("garbageCollectorMetrics must not be null");
        }
        gcOldCountGauge = (Gauge<Long>) MetricMonitorValues.getMetric(garbageCollectorMetrics, MetricMonitorValues.METRIC_GC_CMS_OLDGEN_COUNT, EMPTY_LONG_GAUGE);
        gcOldTimeGauge = (Gauge<Long>) MetricMonitorValues.getMetric(garbageCollectorMetrics, MetricMonitorValues.METRIC_GC_CMS_OLDGEN_TIME, EMPTY_LONG_GAUGE);
        gcNewCountGauge = (Gauge<Long>) MetricMonitorValues.getMetric(garbageCollectorMetrics, MetricMonitorValues.METRIC_GC_CMS_NEWGEN_COUNT, EMPTY_LONG_GAUGE);
        gcNewTimeGauge = (Gauge<Long>) MetricMonitorValues.getMetric(garbageCollectorMetrics, MetricMonitorValues.METRIC_GC_CMS_NEWGEN_TIME, EMPTY_LONG_GAUGE);
    }

    @Override
    public TJvmGcType gcType() {
        return GC_TYPE;
    }

    @Override
    public Long gcOldCount() {
        return gcOldCountGauge.getValue();
    }

    @Override
    public Long gcOldTime() {
        return gcOldTimeGauge.getValue();
    }

    @Override
    public Long gcNewCount() {
        return gcNewCountGauge.getValue();
    }

    @Override
    public Long gcNewTime() {
        return gcNewTimeGauge.getValue();
    }

    @Override
    public String toString() {
        return "HotSpot's Concurrent-Mark-Sweep gc garbage metrics";
    }
}
