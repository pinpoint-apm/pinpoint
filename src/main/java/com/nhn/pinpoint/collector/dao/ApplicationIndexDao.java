package com.nhn.pinpoint.collector.dao;

import com.nhn.pinpoint.common.dto2.thrift.AgentInfo;

public interface ApplicationIndexDao {
	public void insert(final AgentInfo agentInfo);
}
