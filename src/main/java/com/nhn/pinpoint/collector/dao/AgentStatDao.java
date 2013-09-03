package com.nhn.pinpoint.collector.dao;

import com.nhn.pinpoint.thrift.dto.AgentStat;

public interface AgentStatDao {
	void insert(AgentStat agentStat, final byte[] value);
}
