package com.profiler.server.dao;

import com.profiler.common.dto.thrift.AgentInfo;

public interface ApplicationIndex {
	public void insert(final AgentInfo agentInfo);
}
