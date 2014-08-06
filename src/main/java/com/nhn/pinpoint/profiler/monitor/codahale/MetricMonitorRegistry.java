package com.nhn.pinpoint.profiler.monitor.codahale;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.JvmAttributeGaugeSet;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.jvm.GarbageCollectorMetricSet;
import com.codahale.metrics.jvm.MemoryUsageGaugeSet;
import com.codahale.metrics.jvm.ThreadStatesGaugeSet;
import com.nhn.pinpoint.profiler.monitor.CounterMonitor;
import com.nhn.pinpoint.profiler.monitor.EventRateMonitor;
import com.nhn.pinpoint.profiler.monitor.HistogramMonitor;
import com.nhn.pinpoint.profiler.monitor.MonitorName;
import com.nhn.pinpoint.profiler.monitor.MonitorRegistry;
import com.nhn.pinpoint.profiler.monitor.codahale.cpu.CpuLoadMetricSetSelector;
import com.nhn.pinpoint.profiler.monitor.codahale.cpu.metric.CpuLoadMetricSet;

/**
 * 모니터링을 위해 <a href="http://metrics.codahale.com/">Codahale</a>
 * {@link MetricRegistry}를 활용한다. 인터페이스는 netty에서 정의한 MonitorRegistry를 참고하였음.
 *
 * @author emeroad
 * @author harebox
 */
public class MetricMonitorRegistry implements MonitorRegistry {

	private final MetricRegistry delegate;

	public MetricMonitorRegistry() {
		this(new MetricRegistry());
	}

	public MetricMonitorRegistry(MetricRegistry registry) {
		if (registry == null) {
			throw new NullPointerException("registry is null");
		}
		this.delegate = registry;
	}

	public HistogramMonitor newHistogramMonitor(MonitorName monitorName) {
		final Histogram histogram = this.delegate.histogram(monitorName.getName());
		return new MetricHistogramMonitor(histogram);
	}

	public EventRateMonitor newEventRateMonitor(MonitorName monitorName) {
        if (monitorName == null) {
            throw new NullPointerException("monitorName must not be null");
        }
        final Meter meter = this.delegate.meter(monitorName.getName());
		return new MetricEventRateMonitor(meter);
	}

	public CounterMonitor newCounterMonitor(MonitorName monitorName) {
        if (monitorName == null) {
            throw new NullPointerException("monitorName must not be null");
        }
        final Counter counter = this.delegate.counter(monitorName.getName());
		return new MetricCounterMonitor(counter);
	}

	public MemoryUsageGaugeSet registerJvmMemoryMonitor(MonitorName monitorName) {
        if (monitorName == null) {
            throw new NullPointerException("monitorName must not be null");
        }
        return this.delegate.register(monitorName.getName(), new MemoryUsageGaugeSet());
	}
	
	public JvmAttributeGaugeSet registerJvmAttributeMonitor(MonitorName monitorName) {
        if (monitorName == null) {
            throw new NullPointerException("monitorName must not be null");
        }
        return this.delegate.register(monitorName.getName(), new JvmAttributeGaugeSet());
	}
	
	public GarbageCollectorMetricSet registerJvmGcMonitor(MonitorName monitorName) {
        if (monitorName == null) {
            throw new NullPointerException("monitorName must not be null");
        }
        return this.delegate.register(monitorName.getName(), new GarbageCollectorMetricSet());
	}
	
	public CpuLoadMetricSet registerCpuLoadMonitor(MonitorName monitorName) {
		if (monitorName == null) {
			throw new NullPointerException("monitorName must not be null");
		}
		return this.delegate.register(monitorName.getName(), CpuLoadMetricSetSelector.getCpuLoadMetricSet());
	}
	
	public ThreadStatesGaugeSet registerJvmThreadStatesMonitor(MonitorName monitorName) {
        if (monitorName == null) {
            throw new NullPointerException("monitorName must not be null");
        }
        return this.delegate.register(monitorName.getName(), new ThreadStatesGaugeSet());
	}
	
	public MetricRegistry getRegistry() {
		return this.delegate;
	}
	
	public String toString() {
		return "MetricMonitorRegistry(delegate=" + this.delegate + ")";
	}

}
