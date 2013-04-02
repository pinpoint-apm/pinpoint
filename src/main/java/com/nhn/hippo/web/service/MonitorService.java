package com.nhn.hippo.web.service;

import com.profiler.common.bo.AgentInfoBo;

/**
 * @author netspider
 */
public interface MonitorService {
	public AgentInfoBo getAgentInfo(String agentId);
}
