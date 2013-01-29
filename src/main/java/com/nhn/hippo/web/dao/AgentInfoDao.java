package com.nhn.hippo.web.dao;

/**
 *
 */
public interface AgentInfoDao {

    long findAgentInfoBeforeStartTime(String agentInfo, long currentTime);
}
