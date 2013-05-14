package com.nhn.pinpoint.web.dao;

import com.profiler.common.bo.AgentInfoBo;

/**
 *
 */
public interface AgentInfoDao {

    AgentInfoBo findAgentInfoBeforeStartTime(String agentId, long currentTime);
}
