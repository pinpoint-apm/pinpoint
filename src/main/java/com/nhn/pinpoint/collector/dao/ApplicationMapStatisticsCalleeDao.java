package com.nhn.pinpoint.collector.dao;

/**
 * 
 * @author netspider
 * @author emeroad
 */
public interface ApplicationMapStatisticsCalleeDao extends CachedStatisticsDao {
	void update(String callerApplicationName, short callerServiceType, String callerHost, String calleeApplicationName, short calleeServiceType, int elapsed, boolean isError);
}
