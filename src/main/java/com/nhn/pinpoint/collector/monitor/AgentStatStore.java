package com.nhn.pinpoint.collector.monitor;

import com.nhn.pinpoint.thrift.dto.AgentStat;

/**
 * @author harebox
 */
public interface AgentStatStore {

	void store(AgentStat agentStat);
	
	AgentStat get(String agentId);

	String getInJson();
	
	String getInJson(String agentId);
	
}
