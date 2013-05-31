package com.nhn.pinpoint.web.dao;

import com.nhn.pinpoint.common.bo.AgentInfoBo;

/**
 *
 */
public interface AgentInfoDao {

    AgentInfoBo findAgentInfoBeforeStartTime(String agentId, long currentTime);
}
