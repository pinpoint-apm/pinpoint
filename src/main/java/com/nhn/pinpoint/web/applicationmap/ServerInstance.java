package com.nhn.pinpoint.web.applicationmap;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.common.bo.AgentInfoBo;

/**
 * 
 * @author netspider
 * @author emeroad
 */
public class ServerInstance {

    private final String hostName;

    private final String name;
	private final ServiceType serviceType;

    private final ServerType serverType;

	private final AgentInfoBo agentInfo;

	public ServerInstance(AgentInfoBo agentInfo) {
        if (agentInfo == null) {
            throw new NullPointerException("agentInfo must not be null");
        }
        this.hostName = agentInfo.getHostname();
        this.name = agentInfo.getAgentId();
		this.serviceType = agentInfo.getServiceType();
		this.agentInfo = agentInfo;
        this.serverType = ServerType.Physical;
	}
	
	public ServerInstance(String hostName, String physicalName, ServiceType serviceType) {
        if (hostName == null) {
            throw new NullPointerException("hostName must not be null");
        }
        if (physicalName == null) {
            throw new NullPointerException("logicalName must not be null");
        }
        if (serviceType == null) {
            throw new NullPointerException("serviceType must not be null");
        }
        this.hostName = hostName;
        this.name = physicalName;
		this.serviceType = serviceType;
		this.agentInfo = null;
        this.serverType = ServerType.Logical;
	}

    @JsonIgnore
    public String getHostName() {
        return hostName;
    }

    @JsonProperty("name")
    public String getName() {
        return name;
    }

    @JsonProperty("serviceType")
    public ServiceType getServiceType() {
        return serviceType;
    }

    @JsonIgnore
    public ServerType getServerType() {
        return serverType;
    }

    @JsonProperty("agentInfo")
	public AgentInfoBo getAgentInfo() {
		return agentInfo;
	}


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ServerInstance that = (ServerInstance) o;

        if (!name.equals(that.name)) return false;
        if (serverType != that.serverType) return false;
        if (serviceType != that.serviceType) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + serverType.hashCode();
        result = 31 * result + serviceType.hashCode();
        return result;
    }


}
