package com.nhn.pinpoint.profiler.monitor;


public interface MonitorRegistry {

	HistogramMonitor newHistogramMonitor(final MonitorName monitorName);

	EventRateMonitor newEventRateMonitor(final MonitorName monitorName);

	CounterMonitor newCounterMonitor(final MonitorName monitorName);
	
}
