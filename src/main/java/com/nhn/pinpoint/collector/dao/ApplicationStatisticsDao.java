package com.nhn.pinpoint.collector.dao;

/**
 * 
 * @author netspider
 * 
 */
public interface ApplicationStatisticsDao extends CachedStatisticsDao {
	void update(String applicationName, short serviceType, String agentId, int elapsed, boolean isError);
}
