package com.nhn.pinpoint.web.dao;

import com.nhn.pinpoint.common.bo.AgentInfoBo;

import java.util.List;

/**
 *
 */
public interface AgentInfoDao {

    AgentInfoBo findAgentInfoBeforeStartTime(String agentId, long currentTime);

    List<AgentInfoBo> getAgentInfo(String agentId, long startTime);
}
