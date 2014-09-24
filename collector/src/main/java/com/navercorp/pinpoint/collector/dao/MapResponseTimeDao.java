package com.nhn.pinpoint.collector.dao;

/**
 * @author emeroad
 */
public interface MapResponseTimeDao extends CachedStatisticsDao {
    void received(String applicationName, short serviceType, String agentId, int elapsed, boolean isError);
}
