package com.nhn.pinpoint.web.service;

import java.util.List;

import com.nhn.pinpoint.thrift.dto.TAgentStat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nhn.pinpoint.web.dao.AgentStatDao;

/**
 * @author harebox
 */
@Service
public class AgentStatServiceImpl implements AgentStatService {
	
	@Autowired
	private AgentStatDao agentStatDao;

	public List<TAgentStat> selectAgentStatList(String agentId, long start, long end) {
        if (agentId == null) {
            throw new NullPointerException("agentId must not be null");
        }
        return agentStatDao.scanAgentStatList(agentId, start, end);
	}

}
