package com.nhn.pinpoint.collector.dao;

import com.nhn.pinpoint.thrift.dto.AgentInfo;

/**
 *
 */
public interface AgentInfoDao {
    void insert(AgentInfo agentInfo);
}
