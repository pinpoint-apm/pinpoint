package com.profiler.server.dao;

public interface AgentIdApplicationIndex {
	void insert(String agentId, String applicationName);

	String selectApplicationName(String agentId);
}
