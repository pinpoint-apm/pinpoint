package com.nhn.pinpoint.web.dao;

import java.util.List;

import com.nhn.pinpoint.thrift.dto.TAgentStat;

public interface AgentStatDao {
	
	List<TAgentStat> scanAgentStatList(String agentId, long start, long end);
//	List<AgentStat> scanAgentStatList(String agentId, long start, long end, final int limit);
	
}
