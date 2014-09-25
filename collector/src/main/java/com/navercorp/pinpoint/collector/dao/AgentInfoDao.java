package com.nhn.pinpoint.collector.dao;

import com.nhn.pinpoint.thrift.dto.TAgentInfo;

/**
 * @author emeroad
 */
public interface AgentInfoDao {
    void insert(TAgentInfo agentInfo);
}
