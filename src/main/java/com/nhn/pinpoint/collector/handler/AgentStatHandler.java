package com.nhn.pinpoint.collector.handler;

import org.apache.thrift.TBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nhn.pinpoint.collector.StatServer;
import com.nhn.pinpoint.common.dto2.thrift.AgentStat;

@Service("agentStatHandler")
public class AgentStatHandler implements SimpleHandler {

	private final Logger logger = LoggerFactory.getLogger(AgentStatHandler.class.getName());

	@Autowired
	private StatServer statServer;
	
	public void handler(TBase<?, ?> tbase) {
		if (!(tbase instanceof AgentStat)) {
			logger.warn("invalid tbase:{}", tbase);
			return;
		}
		
		try {
			AgentStat agentStat = (AgentStat) tbase;
			if (logger.isDebugEnabled()) {
				logger.debug("Received AgentStat={}", agentStat);
			}

			if (statServer != null) {
				statServer.getStore().store(agentStat);
			}
		} catch (Exception e) {
			logger.warn("AgentStat handle error. Caused:{}", e.getMessage(), e);
		}
	}
}
