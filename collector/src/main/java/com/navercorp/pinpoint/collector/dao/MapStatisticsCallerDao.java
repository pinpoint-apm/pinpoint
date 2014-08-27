package com.nhn.pinpoint.collector.dao;

/**
 * 
 * @author netspider
 * @author emeroad
 */
public interface MapStatisticsCallerDao extends CachedStatisticsDao {
	void update(String callerApplicationName, short callerServiceType, String callerAgentId, String calleeApplicationName, short calleeServiceType, String calleeHost, int elapsed, boolean isError);
}
