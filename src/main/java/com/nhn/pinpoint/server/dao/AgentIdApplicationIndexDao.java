package com.nhn.pinpoint.server.dao;

public interface AgentIdApplicationIndexDao {
	void insert(String agentId, String applicationName);

	String selectApplicationName(String agentId);
}
