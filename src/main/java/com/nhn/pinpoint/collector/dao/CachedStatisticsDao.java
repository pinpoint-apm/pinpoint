package com.nhn.pinpoint.collector.dao;

/**
 * 
 * @author netspider
 * 
 */
public interface CachedStatisticsDao {
	void flush();

	void flushAll();
}
