package com.profiler.server.dao;

import com.nhn.pinpoint.common.dto2.thrift.AgentInfo;

/**
 *
 */
public interface AgentInfoDao {
    void insert(AgentInfo agentInfo);
}
