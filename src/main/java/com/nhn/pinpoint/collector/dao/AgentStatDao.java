package com.nhn.pinpoint.collector.dao;

import com.nhn.pinpoint.common.dto2.thrift.AgentStat;

public interface AgentStatDao {
	void insert(AgentStat agentStat, final byte[] value);
}
