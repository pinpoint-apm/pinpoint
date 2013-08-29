package com.nhn.pinpoint.common.monitor;

public interface HistogramMonitor {

	void reset();

	void update(final long value);
	
	long getCount();

}
