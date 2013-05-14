package com.nhn.pinpoint.web.service;

import com.profiler.common.bo.AgentInfoBo;

/**
 * @author netspider
 */
public interface MonitorService {
	public AgentInfoBo getAgentInfo(String agentId);
}
