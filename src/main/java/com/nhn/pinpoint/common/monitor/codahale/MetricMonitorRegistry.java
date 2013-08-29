package com.nhn.pinpoint.common.monitor.codahale;

import java.util.concurrent.TimeUnit;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.JvmAttributeGaugeSet;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.SharedMetricRegistries;
import com.codahale.metrics.json.MetricsModule;
import com.codahale.metrics.jvm.GarbageCollectorMetricSet;
import com.codahale.metrics.jvm.MemoryUsageGaugeSet;
import com.codahale.metrics.jvm.ThreadStatesGaugeSet;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhn.pinpoint.common.monitor.CounterMonitor;
import com.nhn.pinpoint.common.monitor.EventRateMonitor;
import com.nhn.pinpoint.common.monitor.HistogramMonitor;
import com.nhn.pinpoint.common.monitor.MonitorName;
import com.nhn.pinpoint.common.monitor.MonitorRegistry;
import com.nhn.pinpoint.common.monitor.codahale.MetricCounterMonitor;
import com.nhn.pinpoint.common.monitor.codahale.MetricEventRateMonitor;
import com.nhn.pinpoint.common.monitor.codahale.MetricHistogramMonitor;
import com.nhn.pinpoint.common.monitor.codahale.MetricMonitorRegistry;
//import com.nhn.pinpoint.profiler.logging.Logger;
//import com.nhn.pinpoint.profiler.logging.LoggerFactory;

/**
 * 모니터링을 위해 <a href="http://metrics.codahale.com/">Codahale</a>
 * {@link MetricRegistry}를 활용한다. 인터페이스는 netty에서 정의한 MonitorRegistry를 참고하였음.
 * 
 * @author harebox
 */
public class MetricMonitorRegistry implements MonitorRegistry {

//	public static final Logger logger = LoggerFactory.getLogger(MetricMonitorRegistry.class);
	public static final MetricRegistry DEFAULT_REGISTRY;

	final MetricRegistry delegate;
	final ObjectMapper jsonMapper;

	static {
		DEFAULT_REGISTRY = SharedMetricRegistries
				.getOrCreate(MetricMonitorRegistry.class.getName());
	}

	public MetricMonitorRegistry() {
		this(DEFAULT_REGISTRY);
	}

	public MetricMonitorRegistry(MetricRegistry registry) {
		if (registry == null) {
			throw new NullPointerException("registry is null");
		}
		this.delegate = registry;
		this.jsonMapper = new ObjectMapper().registerModule(new MetricsModule(
				TimeUnit.MILLISECONDS, TimeUnit.MILLISECONDS, false));
	}

	public HistogramMonitor newHistogramMonitor(MonitorName monitorName) {
		final Histogram histogram = this.delegate.histogram(monitorName.getName());
		return new MetricHistogramMonitor(histogram);
	}

	public EventRateMonitor newEventRateMonitor(MonitorName monitorName) {
		final Meter meter = this.delegate.meter(monitorName.getName());
		return new MetricEventRateMonitor(meter);
	}

	public CounterMonitor newCounterMonitor(MonitorName monitorName) {
		final Counter counter = this.delegate.counter(monitorName.getName());
		return new MetricCounterMonitor(counter);
	}

	public void registerJvmMemoryMonitor(MonitorName monitorName) {
		this.delegate.register(monitorName.getName(), new MemoryUsageGaugeSet());
	}
	
	public void registerJvmAttributeMonitor(MonitorName monitorName) {
		this.delegate.register(monitorName.getName(), new JvmAttributeGaugeSet());
	}
	
	public void registerJvmGcMonitor(MonitorName monitorName) {
		this.delegate.register(monitorName.getName(), new GarbageCollectorMetricSet());
	}
	
	public void registerJvmThreadStatesMonitor(MonitorName monitorName) {
		this.delegate.register(monitorName.getName(), new ThreadStatesGaugeSet());
	}

	public byte[] getMonitorsAsJsonBytes() {
		try {
			return jsonMapper.writeValueAsBytes(this.delegate.getMetrics());
		} catch (JsonProcessingException e) {
//			logger.error("cannot serialize the metrics : {}", e.getMessage());
			return null;
		}
	}

	public String getMonitorsAsJson() {
		try {
			return jsonMapper.writeValueAsString(this.delegate.getMetrics());
		} catch (JsonProcessingException e) {
//			logger.error("cannot serialize the metrics : {}", e.getMessage());
			return null;
		}
	}
	
	public MetricRegistry getRegistry() {
		return this.delegate;
	}
	
	public String toString() {
		return "MetricMonitorRegistry(delegate=" + this.delegate + ")";
	}

}
