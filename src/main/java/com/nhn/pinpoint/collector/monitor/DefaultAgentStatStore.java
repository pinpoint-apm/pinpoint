package com.nhn.pinpoint.collector.monitor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.nhn.pinpoint.thrift.dto.AgentStat;

/**
 * 테스트를 위해 메모리에 (AgentId -> AgentStat) 맵을 캐싱해둔다. 
 * @author harebox
 */
public class DefaultAgentStatStore implements AgentStatStore {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	@Qualifier("jsonObjectMapper")
	private ObjectMapper jsonObjectMapper;
	
	// in-memory storage
	private Map<String, AgentStat> map = new ConcurrentHashMap<String, AgentStat>();
	
	public void store(AgentStat agentStat) {
		String agentId = AgentStatSupport.getAgentId(agentStat);
		
		if (agentId != null) {
			map.put(agentId, agentStat);
			logger.info("{} -> {}", agentId, agentStat);
		} else {
			logger.info("invalid AgentStat : {}", agentStat);
		}
	}

	public AgentStat get(String agentId) {
		return map.get(agentId);
	}
	
	public String getInJson(String agentId) {
		String result = null;
		try {
			AgentStat agentStat = map.get(agentId);
			if (agentStat != null) {
				Object typeObject = agentStat.getFieldValue(agentStat.getSetField());
				result = jsonObjectMapper.writeValueAsString(typeObject);
			}
		} catch (Exception e) {
			logger.error("failed to serialze the object to JSON : {}", e.getMessage());
		}
		return result;
	}
	
	public String getInJson() {
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		sb.append("\"agentId\"").append(" : ").append("\"").append(map.keySet().toString()).append("\"");
		sb.append("}");
		return sb.toString();
	}
	
}
