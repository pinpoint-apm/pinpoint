/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.monitor.codahale.cpu.metric;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.Metric;
import com.navercorp.pinpoint.profiler.monitor.codahale.MetricMonitorValues;

/**
 * @author hyungil.jeong
 */
public final class EmptyCpuLoadMetricSet implements CpuLoadMetricSet {

    private static final Double UNSUPPORTED_CPU_LOAD_METRIC = -1.0d;
    private static final Gauge<Double> UNSUPPORTED_CPU_LOAD_METRIC_GAUGE = new Gauge<Double>() {
        @Override
        public Double getValue() {
            return UNSUPPORTED_CPU_LOAD_METRIC;
        }
    };

    @Override
    public Map<String, Metric> getMetrics() {
        final Map<String, Metric> gauges = new HashMap<String, Metric>();
        gauges.put(MetricMonitorValues.CPU_LOAD_JVM, UNSUPPORTED_CPU_LOAD_METRIC_GAUGE);
        gauges.put(MetricMonitorValues.CPU_LOAD_SYSTEM, UNSUPPORTED_CPU_LOAD_METRIC_GAUGE);
        return Collections.unmodifiableMap(gauges);
    }

    @Override
    public String toString() {
        return "Disabled CpuLoadMetricSet";
    }

}
