package com.nhn.pinpoint.web.applicationmap;

import org.codehaus.jackson.map.ObjectMapper;

import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.common.bo.AgentInfoBo;
import com.nhn.pinpoint.web.util.JsonSerializable;

/**
 * 
 * @author netspider
 * @author emeroad
 */
public class ServerInstance implements JsonSerializable {

	private final String name;
	private final ServiceType serviceType;
	
	private final String id;
	private final AgentInfoBo agentInfo;

	private static final ObjectMapper MAPPER = new ObjectMapper();

	public ServerInstance(AgentInfoBo agentInfo) {
        if (agentInfo == null) {
            throw new NullPointerException("agentInfo must not be null");
        }
        this.name = agentInfo.getAgentId();
		this.serviceType = agentInfo.getServiceType();
		this.id = name;// + serviceType;
		this.agentInfo = agentInfo;
	}
	
	public ServerInstance(String name, ServiceType serviceType) {
        if (name == null) {
            throw new NullPointerException("name must not be null");
        }
        if (serviceType == null) {
            throw new NullPointerException("serviceType must not be null");
        }
        this.name = name;
		this.serviceType = serviceType;
		this.id = name + serviceType;
		this.agentInfo = null;
	}

	public String getId() {
		return this.id;
	}

	public AgentInfoBo getAgentInfo() {
		return agentInfo;
	}


	@Override
	public String getJson() {
		String agentInfoJson = "{}";
		try {
			agentInfoJson = MAPPER.writeValueAsString(agentInfo);
		} catch (Exception e) {
			throw new RuntimeException("json create fail,", e);
		}
		
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		sb.append("\"name\":\"").append(name).append("\",");
		sb.append("\"serviceType\":\"").append(serviceType).append("\",");
		sb.append("\"agentInfo\":").append((agentInfo == null) ? null : agentInfoJson);
		sb.append("}");
		return sb.toString();
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
		return "ServerInstance [id=" + id + ", agentInfo=" + agentInfo + "]";
	}

}
