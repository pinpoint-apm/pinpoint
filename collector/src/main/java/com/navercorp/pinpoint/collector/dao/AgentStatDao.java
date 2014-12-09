package com.navercorp.pinpoint.collector.dao;

import com.navercorp.pinpoint.thrift.dto.TAgentStat;

/**
 * @author emeroad
 * @author hyungil.jeong
 */
public interface AgentStatDao {
    void insert(TAgentStat agentStat);
}
