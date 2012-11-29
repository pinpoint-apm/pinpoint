package com.profiler.server.dao;

/**
 * 
 * @author netspider
 * 
 */
public interface TerminalStatistics {
	void update(String sourceApplicationName, String destApplicationName, short destServiceType);
}
