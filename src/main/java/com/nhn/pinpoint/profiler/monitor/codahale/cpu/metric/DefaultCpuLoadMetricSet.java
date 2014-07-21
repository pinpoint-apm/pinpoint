package com.nhn.pinpoint.profiler.monitor.codahale.cpu.metric;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;

import com.codahale.metrics.Gauge;
import com.sun.management.OperatingSystemMXBean;

/**
 * @author hyungil.jeong
 */
public final class DefaultCpuLoadMetricSet extends AbstractCpuLoadMetricSet {

	private static final int UNSUPPORTED = -1;
	private static final int UNINITIALIZED = -1;

	private final RuntimeMXBean runtimeMXBean;

	public DefaultCpuLoadMetricSet() {
		this.runtimeMXBean = ManagementFactory.getRuntimeMXBean();
	}

	@Override
	protected Gauge<Double> getJvmCpuLoadGauge(final OperatingSystemMXBean operatingSystemMXBean) {
		return new Gauge<Double>() {

			private long lastCpuTimeNS = UNINITIALIZED;
			private long lastUpTimeMS = UNINITIALIZED;

			@Override
			public Double getValue() {
				
				final long cpuTimeNS = operatingSystemMXBean.getProcessCpuTime();
				if (cpuTimeNS == UNSUPPORTED) {
					return (double)UNSUPPORTED;
				}
				final long upTimeMS = runtimeMXBean.getUptime();
				
				if (this.lastCpuTimeNS == UNINITIALIZED || this.lastUpTimeMS == UNINITIALIZED) {
					this.lastCpuTimeNS = cpuTimeNS;
					this.lastUpTimeMS = upTimeMS;
					return 0.0D;
				}
				
				final long totalCpuTimeNS = cpuTimeNS - lastCpuTimeNS;
				final long diffUpTimeMS = upTimeMS - lastUpTimeMS;
				final int numProcessors = Runtime.getRuntime().availableProcessors();
				final long totalUpTimeNS = (diffUpTimeMS * 1000000) * numProcessors;
				
				final double cpuLoad = totalUpTimeNS > 0 ? 
						Math.min(100F, totalCpuTimeNS / (float)totalUpTimeNS) : UNSUPPORTED;
				
				this.lastCpuTimeNS = cpuTimeNS;
				this.lastUpTimeMS = upTimeMS;
				
				return cpuLoad;
			}
		};
	}

	@Override
	protected Gauge<Double> getSystemCpuLoadGauge(final OperatingSystemMXBean operatingSystemMXBean) {
		return new Gauge<Double>() {
			@Override
			public Double getValue() {
				return operatingSystemMXBean.getSystemLoadAverage();
			}
		};
	}

}
