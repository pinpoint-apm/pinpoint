package com.nhn.pinpoint.profiler.monitor;

public interface CounterMonitor {

	void incr();

	void incr(final long delta);

	void decr();

	void decr(final long delta);
	
	void reset();
	
	long getCount();

}
