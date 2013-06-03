package com.nhn.pinpoint.server.dao;

/**
 * 
 * @author netspider
 * 
 */
public interface HostApplicationMapDao {
	void insert(String host, String applicationName, short serviceType);
}
