package com.nhn.pinpoint.web.vo;

import com.nhn.pinpoint.common.bo.AgentInfoBo;

/**
 * 
 * @author netspider
 * 
 */
public class AgentStatus {

	private final boolean exists;
	private final long checkTime;
	private final AgentInfoBo agentInfo;

	public AgentStatus(AgentInfoBo agentInfoBo) {
		this.exists = agentInfoBo != null;
		this.agentInfo = agentInfoBo;
		this.checkTime = System.currentTimeMillis();
	}

	public boolean isExists() {
		return exists;
	}

	public AgentInfoBo getAgentInfo() {
		return agentInfo;
	}

	public long getCheckTime() {
		return checkTime;
	}
}
