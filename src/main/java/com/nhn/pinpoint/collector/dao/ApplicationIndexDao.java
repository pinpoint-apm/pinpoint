package com.nhn.pinpoint.collector.dao;

import com.nhn.pinpoint.thrift.dto.AgentInfo;

public interface ApplicationIndexDao {
	public void insert(final AgentInfo agentInfo);
}
