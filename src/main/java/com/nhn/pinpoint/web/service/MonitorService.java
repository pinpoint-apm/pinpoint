package com.nhn.pinpoint.web.service;

import com.nhn.pinpoint.common.bo.AgentInfoBo;

/**
 * @author netspider
 */
public interface MonitorService {
	public AgentInfoBo getAgentInfo(String agentId);
}
