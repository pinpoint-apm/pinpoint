package com.navercorp.pinpoint.collector.dao;

import com.navercorp.pinpoint.thrift.dto.TAgentInfo;

/**
 * @author emeroad
 */
public interface AgentInfoDao {
    void insert(TAgentInfo agentInfo);
}
