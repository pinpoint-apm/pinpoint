package com.nhn.pinpoint.collector.handler;

import com.nhn.pinpoint.thrift.dto.TAgentInfo;
import org.apache.thrift.TBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.nhn.pinpoint.collector.dao.AgentIdApplicationIndexDao;
import com.nhn.pinpoint.collector.dao.AgentInfoDao;
import com.nhn.pinpoint.collector.dao.ApplicationIndexDao;
import org.springframework.stereotype.Service;

/**
 * @author emeroad
 */
@Service("agentInfoHandler")
public class AgentInfoHandler implements SimpleHandler {

	private final Logger logger = LoggerFactory.getLogger(AgentInfoHandler.class.getName());

	@Autowired
	private AgentInfoDao agentInfoDao;

	@Autowired
	private ApplicationIndexDao applicationIndexDao;

//	@Autowired
//	private AgentIdApplicationIndexDao agentIdApplicationIndexDao;

	public void handler(TBase<?, ?> tbase) {
		if (!(tbase instanceof TAgentInfo)) {
			logger.warn("invalid tbase:{}", tbase);
			return;
		}


		try {
			TAgentInfo agentInfo = (TAgentInfo) tbase;

			logger.debug("Received AgentInfo={}", agentInfo);

			// agent info
			agentInfoDao.insert(agentInfo);

			// applicationname으로 agentid를 조회하기위한 용도.
			applicationIndexDao.insert(agentInfo);

			// agentid로 applicationname을 조회하기 위한 용도
//			agentIdApplicationIndexDao.insert(agentInfo.getAgentId(), agentInfo.getApplicationName());
		} catch (Exception e) {
			logger.warn("AgentInfo handle error. Caused:{}", e.getMessage(), e);
		}
	}
}
