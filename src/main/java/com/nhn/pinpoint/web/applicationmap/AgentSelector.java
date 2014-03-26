package com.nhn.pinpoint.web.applicationmap;

import com.nhn.pinpoint.common.bo.AgentInfoBo;

import java.util.Set;

/**
 * @author emeroad
 */
public interface AgentSelector {
    Set<AgentInfoBo> selectAgent(String applicationId);
}
