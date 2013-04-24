package com.profiler.server.dao;

/**
 * caller table로 통합됨
 * 
 * @author netspider
 * 
 */
@Deprecated
public interface ClientStatisticsDao {
	void update(String destApplicationName, short destServiceType, int elapsed, boolean isError);
}
