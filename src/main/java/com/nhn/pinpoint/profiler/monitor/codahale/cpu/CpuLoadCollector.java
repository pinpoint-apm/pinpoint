package com.nhn.pinpoint.profiler.monitor.codahale.cpu;

import static com.nhn.pinpoint.profiler.monitor.codahale.MetricMonitorValues.*;

import java.util.Map;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.Metric;
import com.nhn.pinpoint.profiler.monitor.codahale.MetricMonitorValues;
import com.nhn.pinpoint.profiler.monitor.codahale.cpu.metric.CpuLoadMetricSet;
import com.nhn.pinpoint.thrift.dto.TCpuLoad;

/**
 * @author hyungil.jeong
 */
public class CpuLoadCollector {

    private final Gauge<Double> jvmCpuLoadGauge;
    private final Gauge<Double> systemCpuLoadGauge;

    @SuppressWarnings("unchecked")
    public CpuLoadCollector(CpuLoadMetricSet cpuLoadMetricSet) {
        if (cpuLoadMetricSet == null) {
            throw new NullPointerException("cpuLoadMetricSet must not be null");
        }
        Map<String, Metric> metrics = cpuLoadMetricSet.getMetrics();
        this.jvmCpuLoadGauge = (Gauge<Double>)MetricMonitorValues.getMetric(metrics, CPU_LOAD_JVM, DOUBLE_ZERO);
        this.systemCpuLoadGauge = (Gauge<Double>)MetricMonitorValues.getMetric(metrics, CPU_LOAD_SYSTEM, DOUBLE_ZERO);
    }

    public TCpuLoad collectCpuLoad() {
        Double jvmCpuLoad = this.jvmCpuLoadGauge.getValue();
        Double systemCpuLoad = this.systemCpuLoadGauge.getValue();
        if (notCollected(jvmCpuLoad) && notCollected(systemCpuLoad)) {
            return null;
        }
        TCpuLoad cpuLoad = new TCpuLoad();
        if (!notCollected(jvmCpuLoad)) {
            cpuLoad.setJvmCpuLoad(jvmCpuLoad);
        }
        if (!notCollected(systemCpuLoad)) {
            cpuLoad.setSystemCpuLoad(systemCpuLoad);
        }
        return cpuLoad;
    }

    private boolean notCollected(double cpuLoad) {
        return cpuLoad < 0;
    }
}
