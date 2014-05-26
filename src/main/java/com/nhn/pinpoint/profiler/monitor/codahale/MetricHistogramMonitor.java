package com.nhn.pinpoint.profiler.monitor.codahale;

import com.codahale.metrics.Histogram;
import com.nhn.pinpoint.profiler.monitor.HistogramMonitor;

public class MetricHistogramMonitor implements HistogramMonitor {

	private final Histogram delegate;
	
	public MetricHistogramMonitor(Histogram delegate) {
		if (delegate == null) {
			throw new NullPointerException("Histogram delegate is null");
		}
		this.delegate = delegate;
	}
	
	public void reset() {
		throw new RuntimeException("Histogram reset is not supported in Codahale Metrics 3.x.");
	}

	public void update(long value) {
		this.delegate.update(value);
	}
	
	public long getCount() {
		return this.delegate.getCount();
	}
	
	public Histogram getDelegate() {
		return this.delegate;
	}
	
	public String toString() {
		return "MetricValueDistributionMonitor(delegate=" + this.delegate + ")";
	}

}
