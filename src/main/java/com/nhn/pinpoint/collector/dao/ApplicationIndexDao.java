package com.nhn.pinpoint.collector.dao;

import com.nhn.pinpoint.thrift.dto.TAgentInfo;

public interface ApplicationIndexDao {
	public void insert(final TAgentInfo agentInfo);
}
