package com.nhn.hippo.web.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nhn.hippo.web.dao.AgentInfoDao;
import com.profiler.common.bo.AgentInfoBo;

/**
 * 
 * @author netspider
 */
@Service
public class MonitorServiceImpl implements MonitorService {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private AgentInfoDao agentInfoDao;

	public AgentInfoBo getAgentInfo(String agentId) {
		AgentInfoBo agentInfo = agentInfoDao.findAgentInfoBeforeStartTime(agentId, System.currentTimeMillis());
		logger.debug("Found agentInfo agentId={}, agentInfo={}", agentId, agentInfo);
		return agentInfo;
	}
}
