package com.nhn.pinpoint.collector.monitor;

import com.nhn.pinpoint.common.dto2.thrift.AgentStat;

/**
 * @author harebox
 */
public interface AgentStatStore {

	void store(AgentStat agentStat);
	
	AgentStat get(String agentId);

	String getInJson();
	
	String getInJson(String agentId);
	
}
