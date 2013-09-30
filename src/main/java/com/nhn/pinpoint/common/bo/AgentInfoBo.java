package com.nhn.pinpoint.common.bo;

import com.nhn.pinpoint.common.buffer.AutomaticBuffer;
import com.nhn.pinpoint.common.buffer.Buffer;
import com.nhn.pinpoint.common.buffer.FixedBuffer;
import com.nhn.pinpoint.thrift.dto.AgentInfo;

/**
 *
 */
public class AgentInfoBo implements Comparable<AgentInfoBo> {
	private String ip;
    private String hostname;
    private String ports;
    private String agentId;
    private String applicationName;
    private boolean isAlive;
    private long startTime;
    private short identifier;

    public AgentInfoBo(AgentInfo agentInfo) {
    	this.ip = agentInfo.getIp();
        this.hostname = agentInfo.getHostname();
        this.ports = agentInfo.getPorts();
        this.agentId = agentInfo.getAgentId();
        this.applicationName = agentInfo.getApplicationName();
        this.isAlive = agentInfo.isIsAlive();
        this.startTime = agentInfo.getTimestamp();
        this.identifier = agentInfo.getIdentifier();
    }

    public AgentInfoBo() {
    }
    
    public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getPorts() {
        return ports;
    }

    public void setPorts(String ports) {
        this.ports = ports;
    }

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public boolean isAlive() {
        return isAlive;
    }

    public void setAlive(boolean alive) {
        isAlive = alive;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public short getIdentifier() {
        return identifier;
    }

    public void setIdentifier(short identifier) {
        this.identifier = identifier;
    }

    public byte[] writeValue() {
        Buffer buffer = new AutomaticBuffer();
        buffer.putPrefixedString(this.getIp());
        buffer.putPrefixedString(this.getHostname());
        buffer.putPrefixedString(this.getPorts());
        buffer.putPrefixedString(this.getApplicationName());
        buffer.put(this.isAlive());
        buffer.put(this.getIdentifier());
        return buffer.getBuffer();
    }

    public int readValue(byte[] value) {
        Buffer buffer = new FixedBuffer(value);
		this.ip = buffer.readPrefixedString();
        this.hostname = buffer.readPrefixedString();
        this.ports = buffer.readPrefixedString();
        this.applicationName = buffer.readPrefixedString();
        this.isAlive = buffer.readBoolean();
        this.identifier = buffer.readShort();
        return buffer.getOffset();
    }
    
    @Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((agentId == null) ? 0 : agentId.hashCode());
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
		AgentInfoBo other = (AgentInfoBo) obj;
		if (agentId == null) {
			if (other.agentId != null)
				return false;
		} else if (!agentId.equals(other.agentId))
			return false;
		return true;
	}

	public String getJson() {
		StringBuilder sb = new StringBuilder();
		
		sb.append("{");
		sb.append("\t\"ip\" : \"").append(ip).append("\",");
		sb.append("\t\"ports\" : \"").append(ports).append("\",");
		sb.append("\t\"agentId\" : \"").append(agentId).append("\",");
		sb.append("\t\"uptime\" : \"").append(startTime).append("\"");
		sb.append("}");
		
		return sb.toString();
	}
	
	@Override
    public String toString() {
        return "AgentInfoBo{" +
        		"ip='" + ip + '\'' +
                "hostname='" + hostname + '\'' +
                ", ports='" + ports + '\'' +
                ", agentId='" + agentId + '\'' +
                ", applicationName='" + applicationName + '\'' +
                ", isAlive=" + isAlive +
                ", startTime=" + startTime +
                ", identifier=" + identifier +
                '}';
    }

	@Override
	public int compareTo(AgentInfoBo agentInfoBo) {
		return this.agentId.compareTo(agentInfoBo.agentId);
	}
}
