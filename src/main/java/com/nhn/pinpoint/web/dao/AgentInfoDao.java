package com.nhn.pinpoint.web.dao;

import com.nhn.pinpoint.common.bo.AgentInfoBo;

import java.util.List;

/**
 * @author emeroad
 */
public interface AgentInfoDao {
    @Deprecated
    AgentInfoBo findAgentInfoBeforeStartTime(String agentId, long currentTime);

    List<AgentInfoBo> getAgentInfo(String agentId, long from, long to);
}
