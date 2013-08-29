package com.nhn.pinpoint.collector.monitor;

import com.nhn.pinpoint.common.dto2.thrift.AgentStat;

/**
 * 
 * @author harebox
 *
 */
public interface AgentStatStore {

	void store(AgentStat agentStat);
	
	AgentStat getByAgentId(String agentId);

	AgentStat getByIpPort(String ipport);
	
	String getStatByAgentId(String agentId);
	
	String getStatByIpPort(String ipport);

	String toJson();
	
}
