package com.nhn.hippo.web.dao;

/**
 *
 */
public interface AgentInfoDao {

    long selectAgentInfoBeforeStartTime(String agentInfo, long currentTime);
}
