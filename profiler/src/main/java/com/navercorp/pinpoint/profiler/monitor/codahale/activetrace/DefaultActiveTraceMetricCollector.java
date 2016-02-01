/*
 * Copyright 2016 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.monitor.codahale.activetrace;

import static com.navercorp.pinpoint.profiler.monitor.codahale.MetricMonitorValues.*;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.Metric;
import com.navercorp.pinpoint.profiler.monitor.codahale.MetricMonitorValues;
import com.navercorp.pinpoint.profiler.monitor.codahale.activetrace.metric.ActiveTraceMetricSet;
import com.navercorp.pinpoint.thrift.dto.TActiveTrace;
import com.navercorp.pinpoint.thrift.dto.TActiveTraceHistogram;

import java.util.Map;

/**
 * @author HyunGil Jeong
 */
public class DefaultActiveTraceMetricCollector implements ActiveTraceMetricCollector {

    private static final Gauge<TActiveTraceHistogram> UNSUPPORTED_GAUGE = new EmptyGauge<TActiveTraceHistogram>(null);

    private final Gauge<TActiveTraceHistogram> activeTraceHistogramGauge;

    public DefaultActiveTraceMetricCollector(ActiveTraceMetricSet activeTraceMetricSet) {
        if (activeTraceMetricSet == null) {
            throw new NullPointerException("activeTraceMetricSet must not be null");
        }
        Map<String, Metric> metrics = activeTraceMetricSet.getMetrics();
        this.activeTraceHistogramGauge = ((Gauge<TActiveTraceHistogram>) MetricMonitorValues.getMetric(metrics, ACTIVE_TRACE_COUNT, UNSUPPORTED_GAUGE));
    }

    @Override
    public TActiveTrace collect() {
        TActiveTrace activeTrace = new TActiveTrace();
        activeTrace.setHistogram(this.activeTraceHistogramGauge.getValue());
        return activeTrace;
    }

}
