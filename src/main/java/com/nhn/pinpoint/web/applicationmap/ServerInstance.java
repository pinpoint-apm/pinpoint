package com.nhn.pinpoint.web.applicationmap;

import org.codehaus.jackson.map.ObjectMapper;

import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.common.bo.AgentInfoBo;
import com.nhn.pinpoint.web.applicationmap.rawdata.ResponseHistogram;
import com.nhn.pinpoint.web.util.JsonSerializable;
import com.nhn.pinpoint.web.util.Mergeable;

/**
 * 
 * @author netspider
 * 
 */
public class ServerInstance implements Comparable<ServerInstance>, Mergeable<ServerInstance>, JsonSerializable {

	private final String name;
	private final ServiceType serviceType;
	
	private final String id;
	private final AgentInfoBo agentInfo;
	private ResponseHistogram histogram;
	
	private ObjectMapper objectMapper = new ObjectMapper();

	public ServerInstance(AgentInfoBo agentInfo, ResponseHistogram histogram) {
        if (agentInfo == null) {
            throw new NullPointerException("agentInfo must not be null");
        }
        this.name = agentInfo.getAgentId();
		this.serviceType = agentInfo.getServiceType();
		this.id = name;// + serviceType;
		this.agentInfo = agentInfo;
		this.histogram = histogram;
	}
	
	public ServerInstance(String name, ServiceType serviceType, ResponseHistogram histogram) {
		this.name = name;
		this.serviceType = serviceType;
		this.id = name + serviceType;
		this.agentInfo = null;
		this.histogram = histogram;
	}

	public String getId() {
		return this.id;
	}

	public AgentInfoBo getAgentInfo() {
		return agentInfo;
	}

	public void setHistogram(ResponseHistogram histogram) {
		this.histogram = histogram;
	}

	public ResponseHistogram getHistogram() {
		return histogram;
	}

	@Override
	public String getJson() {
		String agentInfoJson = "{}";
		try {
			agentInfoJson = objectMapper.writeValueAsString(agentInfo);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		sb.append("\"name\":\"").append(name).append("\",");
		sb.append("\"serviceType\":\"").append(serviceType).append("\",");
		sb.append("\"agentInfo\":").append((agentInfo == null) ? null : agentInfoJson).append(",");
		sb.append("\"histogram\":").append((histogram == null) ? null : histogram.getJson());
		sb.append("}");
		return sb.toString();
	}

	@Override
	public ServerInstance mergeWith(ServerInstance serverInstance) {
		if (!this.id.equals(serverInstance.getId())) {
			throw new IllegalArgumentException("Server instance id is not equal.");
		}

		if (this.histogram == null) {
			this.histogram = serverInstance.getHistogram();
		} else {
			// this.histogram.mergeWith(serverInstance.getHistogram());
		}
		return this;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ServerInstance other = (ServerInstance) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ServerInstance [id=" + id + ", agentInfo=" + agentInfo + ", histogram=" + histogram + "]";
	}

	@Override
	public int compareTo(ServerInstance serverInstance) {
		return this.id.compareTo(serverInstance.id);
	}
}
