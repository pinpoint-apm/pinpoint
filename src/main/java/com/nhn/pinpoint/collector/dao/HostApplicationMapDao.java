package com.nhn.pinpoint.collector.dao;

/**
 * 
 * @author netspider
 * 
 */
public interface HostApplicationMapDao {
	void insert(String host, String applicationName, short serviceType);
}
