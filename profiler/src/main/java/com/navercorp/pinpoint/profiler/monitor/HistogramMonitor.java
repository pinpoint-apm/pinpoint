package com.nhn.pinpoint.profiler.monitor;

public interface HistogramMonitor {

	void reset();

	void update(final long value);
	
	long getCount();

}
