package com.nhn.pinpoint.collector.monitor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.nhn.pinpoint.thrift.dto.TAgentStat;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

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

	@Deprecated
	public String getInJson(String agentId) {
		String result = null;
//		try {
//			TAgentStat agentStat = map.get(agentId);
//			if (agentStat != null) {
//				Object typeObject = agentStat.getFieldValue(agentStat.getSetField());
//				result = jsonObjectMapper.writeValueAsString(typeObject);
//			}
//		} catch (Exception e) {
//			logger.error("failed to serialze the object to JSON : {}", e.getMessage());
//		}
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
