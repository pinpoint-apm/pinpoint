package com.nhn.pinpoint.common.monitor;

public interface EventRateMonitor {

	void event();

	void events(final long count);
	
	long getCount();
	
	double getRate();
	
}
