package com.nhn.pinpoint.collector.dao;

import com.nhn.pinpoint.thrift.dto.TAgentInfo;

/**
 *
 */
public interface AgentInfoDao {
    void insert(TAgentInfo agentInfo);
}
