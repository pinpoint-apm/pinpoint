package com.nhn.pinpoint.profiler.monitor.codahale;

import com.codahale.metrics.Counter;
import com.nhn.pinpoint.profiler.monitor.CounterMonitor;

public class MetricCounterMonitor implements CounterMonitor {

	final Counter delegate;
	
	public MetricCounterMonitor(Counter delegate) {
		if (delegate == null) {
			throw new NullPointerException("Counter delegate is null");
		}
		this.delegate = delegate;
	}
	
	public void incr() {
		this.delegate.inc();
	}

	public void incr(long delta) {
		this.delegate.inc(delta);
	}

	public void decr() {
		this.delegate.dec();
	}

	public void decr(long delta) {
		this.delegate.dec(delta);
	}

	public void reset() {
		throw new RuntimeException("Counter reset is not supported in Codahale Metrics 3.x.");
	}

	public long getCount() {
		return this.delegate.getCount();
	}
	
	public Counter getDelegate() {
		return this.delegate;
	}
	
	public String toString() {
		return "MetricCounterMonitor(delegate=" + this.delegate + ")";
	}

}
