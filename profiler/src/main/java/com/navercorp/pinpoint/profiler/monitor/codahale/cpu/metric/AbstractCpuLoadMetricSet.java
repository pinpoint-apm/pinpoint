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

import java.lang.management.ManagementFactory;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.Metric;
import com.navercorp.pinpoint.profiler.monitor.codahale.MetricMonitorValues;

/**
 * @author hyungil.jeong
 */
public abstract class AbstractCpuLoadMetricSet implements CpuLoadMetricSet {

    private final com.sun.management.OperatingSystemMXBean mxBean;

    protected AbstractCpuLoadMetricSet() {
        this.mxBean = (com.sun.management.OperatingSystemMXBean)ManagementFactory.getOperatingSystemMXBean();
    }

    protected abstract Gauge<Double> getJvmCpuLoadGauge(final com.sun.management.OperatingSystemMXBean operatingSystemMXBean);

    protected abstract Gauge<Double> getSystemCpuLoadGauge(final com.sun.management.OperatingSystemMXBean operatingSystemMXBean);

    @Override
    public Map<String, Metric> getMetrics() {
        final Map<String, Metric> gauges = new HashMap<String, Metric>();
        gauges.put(MetricMonitorValues.CPU_LOAD_JVM, getJvmCpuLoadGauge(this.mxBean));
        gauges.put(MetricMonitorValues.CPU_LOAD_SYSTEM, getSystemCpuLoadGauge(this.mxBean));
        return Collections.unmodifiableMap(gauges);
    }
}
