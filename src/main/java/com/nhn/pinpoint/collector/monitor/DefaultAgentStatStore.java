package com.nhn.pinpoint.collector.monitor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nhn.pinpoint.common.dto2.thrift.AgentStat;

/**
 * 
 * @author harebox
 */
public class DefaultAgentStatStore implements AgentStatStore {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	// in-memory storage
	private Map<String, AgentStat> map = new ConcurrentHashMap<String, AgentStat>();
	
	private ObjectMapper jsonMapper = new ObjectMapper();

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
				result = jsonMapper.writeValueAsString(typeObject);
			}
		} catch (Exception e) {
			logger.error("failed to serialze the object to JSON : {}", e.getMessage());
		}
		return result;
	}
	
	public String toJson() {
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		sb.append("\"agentId\"").append(" : ").append("\"").append(map.keySet().toString()).append("\"");
		sb.append("}");
		return sb.toString();
	}
	
}
