package com.profiler.server.dao;

import com.profiler.common.dto2.thrift.AgentInfo;

public interface ApplicationIndexDao {
	public void insert(final AgentInfo agentInfo);
}
