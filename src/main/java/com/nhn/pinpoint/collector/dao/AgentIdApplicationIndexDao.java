package com.nhn.pinpoint.collector.dao;

public interface AgentIdApplicationIndexDao {
	void insert(String agentId, String applicationName);

	String selectApplicationName(String agentId);
}
