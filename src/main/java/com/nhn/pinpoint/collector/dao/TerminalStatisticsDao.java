package com.nhn.pinpoint.collector.dao;

/**
 * 
 * @author netspider
 * 
 */
@Deprecated
public interface TerminalStatisticsDao {
	void update(String sourceApplicationName, String destApplicationName, short destServiceType, String destHost, int elapsed, boolean isError);
}
