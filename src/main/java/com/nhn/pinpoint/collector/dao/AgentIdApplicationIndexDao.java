package com.nhn.pinpoint.collector.dao;

/**
 * @author emeroad
 */
@Deprecated
public interface AgentIdApplicationIndexDao {
	void insert(String agentId, String applicationName);

	String selectApplicationName(String agentId);
}
