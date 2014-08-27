package com.nhn.pinpoint.collector.dao;

/**
 * 
 * @author netspider
 * @author emeroad
 */
public interface MapStatisticsCalleeDao extends CachedStatisticsDao {
	void update(String calleeApplicationName, short calleeServiceType, String callerApplicationName, short callerServiceType, String callerHost, int elapsed, boolean isError);
}
