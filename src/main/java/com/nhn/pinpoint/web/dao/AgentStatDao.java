package com.nhn.pinpoint.web.dao;

import java.util.List;

import com.nhn.pinpoint.thrift.dto.TAgentStat;
import com.nhn.pinpoint.web.vo.Range;

public interface AgentStatDao {
	
	List<TAgentStat> scanAgentStatList(String agentId, Range range);
//	List<AgentStat> scanAgentStatList(String agentId, long start, long end, final int limit);
	
}
