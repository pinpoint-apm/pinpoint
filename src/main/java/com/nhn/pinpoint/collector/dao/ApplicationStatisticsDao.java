package com.nhn.pinpoint.collector.dao;

/**
 * 
 * @author netspider
 * 
 */
public interface ApplicationStatisticsDao {
	void update(String applicationName, short serviceType, String agentId, int elapsed, boolean isError);
}
