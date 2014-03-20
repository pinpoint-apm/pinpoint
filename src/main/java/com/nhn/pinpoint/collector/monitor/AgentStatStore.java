package com.nhn.pinpoint.collector.monitor;

import com.nhn.pinpoint.thrift.dto.TAgentStat;

/**
 * @author harebox
 */
public interface AgentStatStore {

	void store(TAgentStat agentStat);
	
	TAgentStat get(String agentId);

}
