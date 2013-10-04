package com.nhn.pinpoint.collector.dao;

import com.nhn.pinpoint.thrift.dto.TAgentStat;

public interface AgentStatDao {
	void insert(TAgentStat agentStat, final byte[] value);
}
