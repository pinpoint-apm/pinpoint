package com.profiler.server.dao;

import com.profiler.common.dto2.thrift.AgentInfo;

/**
 *
 */
public interface AgentInfoDao {
    void insert(AgentInfo agentInfo);
}
