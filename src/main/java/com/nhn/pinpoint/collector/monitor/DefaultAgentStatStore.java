package com.nhn.pinpoint.collector.monitor;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nhn.pinpoint.common.dto2.thrift.AgentStat;

/**
 * 
 * @author harebox
 *
 */
public class DefaultAgentStatStore implements AgentStatStore {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	// in-memory storage
	private Map<String, AgentStat> mapAgentId = new HashMap<String, AgentStat>();
	private Map<String, AgentStat> mapIpPort = new HashMap<String, AgentStat>();

	public void store(AgentStat agentStat) {
		mapAgentId.put(agentStat.getAgentId(), agentStat);
		logger.info("{} -> {}", agentStat.getAgentId(), agentStat);
		
		mapIpPort.put(agentStat.getIp() + "." + agentStat.getPorts(), agentStat);
		logger.info("{} -> {}", agentStat.getIp() + "." + agentStat.getPorts(), agentStat);
	}

	public AgentStat getByAgentId(String agentId) {
		return mapAgentId.get(agentId);
	}

	public AgentStat getByIpPort(String ipport) {
		return mapIpPort.get(ipport);
	}
	
	public String getStatByAgentId(String agentId) {
		AgentStat agentStat = mapAgentId.get(agentId);
		return agentStat == null ? null : agentStat.getStatistics();
	}
	
	public String getStatByIpPort(String ipport) {
		AgentStat agentStat = mapIpPort.get(ipport);
		return agentStat == null ? null : agentStat.getStatistics();
	}
	
	public String toJson() {
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		sb.append("\"agentId\"").append(" : ").append("\"").append(mapAgentId.keySet().toString()).append("\"");
		sb.append(",");
		sb.append("\"ipport\"").append(" : ").append("\"").append(mapIpPort.keySet().toString()).append("\"");
		sb.append("}");
		return sb.toString();
	}
	
}
