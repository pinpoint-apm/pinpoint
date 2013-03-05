package com.profiler.server.dao;

/**
 * 
 * @author netspider
 * 
 */
public interface TerminalStatisticsDao {
	void update(String sourceApplicationName, String destApplicationName, short destServiceType, String destHost, int elapsed, boolean isError);
}
