package com.nhn.pinpoint.web.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nhn.pinpoint.common.dto2.thrift.AgentStat;
import com.nhn.pinpoint.web.dao.AgentStatDao;

/**
 * @author harebox
 */
@Service
public class AgentStatServiceImpl implements AgentStatService {
	
	@Autowired
	private AgentStatDao agentStatDao;

	public List<AgentStat> selectAgentStatList(String agentId, long start, long end) {
		return agentStatDao.scanAgentStatList(agentId, start, end);
	}

}
