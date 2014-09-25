package com.nhn.pinpoint.profiler.monitor.codahale.cpu.metric;

import com.codahale.metrics.Gauge;
import com.sun.management.OperatingSystemMXBean;

/**
 * @author hyungil.jeong
 */
public class EnhancedCpuLoadMetricSet extends AbstractCpuLoadMetricSet {

	@Override
	protected Gauge<Double> getJvmCpuLoadGauge(final OperatingSystemMXBean operatingSystemMXBean) {
		return new Gauge<Double>() {
			@Override
			public Double getValue() {
				return operatingSystemMXBean.getProcessCpuLoad();
			}
		};
	}

	@Override
	protected Gauge<Double> getSystemCpuLoadGauge(final OperatingSystemMXBean operatingSystemMXBean) {
		return new Gauge<Double>() {
			@Override
			public Double getValue() {
				return operatingSystemMXBean.getSystemCpuLoad();
			}
		};
	}

	@Override
	public String toString() {
		return "CpuLoadMetricSet for Java 1.7+";
	}
	
}
