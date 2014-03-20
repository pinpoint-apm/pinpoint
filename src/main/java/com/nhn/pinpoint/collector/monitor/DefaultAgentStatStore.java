package com.nhn.pinpoint.collector.monitor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.nhn.pinpoint.thrift.dto.TAgentStat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 테스트를 위해 메모리에 (AgentId -> AgentStat) 맵을 캐싱해둔다. 
 * @author harebox
 */
public class DefaultAgentStatStore implements AgentStatStore {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	// in-memory storage
	private Map<String, TAgentStat> map = new ConcurrentHashMap<String, TAgentStat>();
	
	public void store(TAgentStat agentStat) {
		String agentId = AgentStatSupport.getAgentId(agentStat);
		
		if (agentId != null) {
			map.put(agentId, agentStat);
			logger.debug("{} -> {}", agentId, agentStat);
		} else {
			logger.debug("invalid AgentStat : {}", agentStat);
		}
	}

	public TAgentStat get(String agentId) {
		return map.get(agentId);
	}


	
}
