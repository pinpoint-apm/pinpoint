package com.nhn.pinpoint.web.dao;

import java.util.List;

import com.nhn.pinpoint.common.dto2.thrift.AgentStat;

public interface AgentStatDao {
	
	List<AgentStat> scanAgentStatList(String agentId, long start, long end);
//	List<AgentStat> scanAgentStatList(String agentId, long start, long end, final int limit);
	
}
