package com.nhn.pinpoint.collector.dao;

/**
 * 
 * @author netspider
 * @author emeroad
 */
public interface ApplicationMapStatisticsCalleeDao extends CachedStatisticsDao {
	void update(String calleeApplicationName, short calleeServiceType, String calleeHost, String callerApplicationName, short callerServiceType, int elapsed, boolean isError);
}
