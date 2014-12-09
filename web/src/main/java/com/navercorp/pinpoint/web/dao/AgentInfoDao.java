package com.navercorp.pinpoint.web.dao;

import com.navercorp.pinpoint.common.bo.AgentInfoBo;
import com.navercorp.pinpoint.web.vo.Range;

import java.util.List;

/**
 * @author emeroad
 */
public interface AgentInfoDao {
    @Deprecated
    AgentInfoBo findAgentInfoBeforeStartTime(String agentId, long currentTime);

    List<AgentInfoBo> getAgentInfo(String agentId, Range range);
}
