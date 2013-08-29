package com.nhn.pinpoint.common.monitor;

import com.nhn.pinpoint.common.monitor.CounterMonitor;
import com.nhn.pinpoint.common.monitor.EventRateMonitor;
import com.nhn.pinpoint.common.monitor.HistogramMonitor;
import com.nhn.pinpoint.common.monitor.MonitorName;

public interface MonitorRegistry {

	HistogramMonitor newHistogramMonitor(final MonitorName monitorName);

	EventRateMonitor newEventRateMonitor(final MonitorName monitorName);

	CounterMonitor newCounterMonitor(final MonitorName monitorName);

	byte[] getMonitorsAsJsonBytes();

	String getMonitorsAsJson();

}
