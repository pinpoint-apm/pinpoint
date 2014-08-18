package com.nhn.pinpoint.profiler.monitor.codahale.cpu.metric;

import java.lang.management.ManagementFactory;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.Metric;
import com.nhn.pinpoint.profiler.monitor.codahale.MetricMonitorValues;

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
