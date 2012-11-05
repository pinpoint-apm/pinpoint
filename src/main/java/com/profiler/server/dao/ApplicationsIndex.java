package com.profiler.server.dao;

import com.profiler.common.dto.thrift.AgentInfo;

public interface ApplicationsIndex {
	public void insert(final AgentInfo agentInfo);
}
