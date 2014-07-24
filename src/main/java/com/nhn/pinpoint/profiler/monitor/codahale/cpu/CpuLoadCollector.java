package com.nhn.pinpoint.profiler.monitor.codahale.cpu;

import static com.nhn.pinpoint.profiler.monitor.codahale.MetricMonitorValues.*;

import java.util.SortedMap;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import com.nhn.pinpoint.profiler.monitor.codahale.MetricMonitorRegistry;
import com.nhn.pinpoint.profiler.monitor.codahale.MetricMonitorValues;
import com.nhn.pinpoint.thrift.dto.TCpuLoad;

/**
 * @author hyungil.jeong
 */
public class CpuLoadCollector {
	
	private static final Double NOT_COLLECTED = -1D;

	private final Gauge<Double> jvmCpuLoadGauge;
	private final Gauge<Double> systemCpuLoadGauge;

	public CpuLoadCollector(MetricMonitorRegistry registry) {
		if (registry == null) {
			throw new NullPointerException("registry must not be null");
		}
		final MetricRegistry metricRegistry = registry.getRegistry();
		// metricRegistry.getGauges의 리턴 타입이 rawType이기 때문에 
		@SuppressWarnings("unchecked")
		final SortedMap<String, Gauge<?>> gauges = (SortedMap<String, Gauge<?>>)(SortedMap<?, ?>)metricRegistry.getGauges();
		
		this.jvmCpuLoadGauge = MetricMonitorValues.getGauge(gauges, CPU_LOAD_JVM, DOUBLE_ZERO);
		this.systemCpuLoadGauge = MetricMonitorValues.getGauge(gauges, CPU_LOAD_SYSTEM, DOUBLE_ZERO);
	}
	
	public TCpuLoad collectCpuLoad() {
		Double jvmCpuLoad = this.jvmCpuLoadGauge.getValue();
		Double systemCpuLoad = this.systemCpuLoadGauge.getValue();
		if (jvmCpuLoad == NOT_COLLECTED && systemCpuLoad == NOT_COLLECTED) {
			return null;
		}
		TCpuLoad cpuLoad = new TCpuLoad();
		cpuLoad.setJvmCpuLoad(jvmCpuLoad);
		cpuLoad.setJvmCpuLoadIsSet(jvmCpuLoad != NOT_COLLECTED);
		cpuLoad.setSystemCpuLoad(systemCpuLoad);
		cpuLoad.setSystemCpuLoadIsSet(systemCpuLoad != NOT_COLLECTED);
		return cpuLoad;
	}

	public Double collectJvmCpuLoad() {
		return this.jvmCpuLoadGauge.getValue();
	}

	public Double collectSystemCpuLoad() {
		return this.systemCpuLoadGauge.getValue();
	}
}
