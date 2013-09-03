package com.nhn.pinpoint.profiler.monitor;

public interface EventRateMonitor {

	void event();

	void events(final long count);
	
	long getCount();
	
	double getRate();
	
}
