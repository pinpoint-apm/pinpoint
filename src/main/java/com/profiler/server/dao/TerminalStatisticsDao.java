package com.profiler.server.dao;

/**
 * 
 * @author netspider
 * 
 */
public interface TerminalStatisticsDao {
	void update(String sourceApplicationName, String destApplicationName, short destServiceType);
}
