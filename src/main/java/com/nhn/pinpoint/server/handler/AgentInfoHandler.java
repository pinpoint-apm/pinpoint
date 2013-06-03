package com.nhn.pinpoint.server.handler;

import java.net.DatagramPacket;

import org.apache.thrift.TBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.nhn.pinpoint.common.dto2.thrift.AgentInfo;
import com.nhn.pinpoint.server.dao.AgentIdApplicationIndexDao;
import com.nhn.pinpoint.server.dao.AgentInfoDao;
import com.nhn.pinpoint.server.dao.ApplicationIndexDao;

public class AgentInfoHandler implements Handler {

	private final Logger logger = LoggerFactory.getLogger(AgentInfoHandler.class.getName());

	@Autowired
	private AgentInfoDao agentInfoDao;

	@Autowired
	private ApplicationIndexDao applicationIndexDao;

	@Autowired
	private AgentIdApplicationIndexDao agentIdApplicationIndexDao;

	public void handler(TBase<?, ?> tbase, DatagramPacket datagramPacket) {
		if (!(tbase instanceof AgentInfo)) {
			logger.warn("invalid tbase:{}", tbase);
			return;
		}

		try {
			AgentInfo agentInfo = (AgentInfo) tbase;

			logger.debug("Received AgentInfo={}", agentInfo);

			// agent info
			agentInfoDao.insert(agentInfo);
			
			// applicationname으로 agentid를 조회하기위한 용도.
			applicationIndexDao.insert(agentInfo);
			
			// agentid로 applicationname을 조회하기 위한 용도
			agentIdApplicationIndexDao.insert(agentInfo.getAgentId(), agentInfo.getApplicationName());
		} catch (Exception e) {
			logger.warn("Span handle error " + e.getMessage(), e);
		}
	}
}
