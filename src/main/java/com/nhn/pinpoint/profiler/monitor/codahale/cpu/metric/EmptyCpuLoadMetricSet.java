package com.nhn.pinpoint.profiler.monitor.codahale.cpu.metric;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.Metric;
import com.nhn.pinpoint.profiler.monitor.codahale.MetricMonitorValues;

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
