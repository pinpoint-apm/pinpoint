package com.profiler.server.dao;

import com.profiler.common.dto.thrift.AgentInfo;

public interface ApplicationIndexDao {
	public void insert(final AgentInfo agentInfo);
}
