package com.nhn.pinpoint.web.service;

import java.util.List;

import com.nhn.pinpoint.thrift.dto.TAgentStat;

public interface AgentStatService {

	/**
	 * 주어진 시간 범위에 따라 특정 agentId에 해당하는 시스템 통계 정보를 조회한다.
	 * @param agentId
	 * @param from
	 * @param to
	 * @return
	 */
	List<TAgentStat> selectAgentStatList(String agentId, long start, long end);
	
}
