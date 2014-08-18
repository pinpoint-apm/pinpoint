package com.nhn.pinpoint.collector.dao;

import com.nhn.pinpoint.thrift.dto.TAgentStat;

/**
 * @author emeroad
 * @author hyungil.jeong
 */
public interface AgentStatDao {
    void insert(TAgentStat agentStat);
}
