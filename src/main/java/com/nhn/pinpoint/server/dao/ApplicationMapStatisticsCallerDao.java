package com.nhn.pinpoint.server.dao;

/**
 * 
 * @author netspider
 * 
 */
public interface ApplicationMapStatisticsCallerDao {
	void update(String calleeApplicationName, short calleeServiceType, String callerApplicationName, short callerServiceType, String callerHost, int elapsed, boolean isError);
}
