package com.profiler.common.bo;

import com.profiler.common.buffer.AutomaticBuffer;
import com.profiler.common.buffer.Buffer;
import com.profiler.common.buffer.FixedBuffer;
import com.profiler.common.dto.thrift.AgentInfo;

/**
 *
 */
public class AgentInfoBo {
    private String hostname;
    private String ports;
    private String agentId;
    private String applicationName;
    private boolean isAlive;
    private long timestamp;
    private short identifier;

    public AgentInfoBo(AgentInfo agentInfo) {
        this.hostname = agentInfo.getHostname();
        this.ports = agentInfo.getPorts();
        this.agentId = agentInfo.getAgentId();
        this.applicationName = agentInfo.getApplicationName();
        this.isAlive = agentInfo.isIsAlive();
        this.timestamp = agentInfo.getTimestamp();
        this.identifier = agentInfo.getIdentifier();
    }

    public AgentInfoBo() {
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

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public short getIdentifier() {
        return identifier;
    }

    public void setIdentifier(short identifier) {
        this.identifier = identifier;
    }

    public byte[] writeValue() {
        Buffer buffer = new AutomaticBuffer();
        buffer.putPrefixedString(this.getHostname());
        buffer.putPrefixedString(this.getPorts());
        buffer.putPrefixedString(this.getApplicationName());
        buffer.put(this.isAlive());
        buffer.put(this.getIdentifier());
        return buffer.getBuffer();
    }

    public int readValue(byte[] value) {
        Buffer buffer = new FixedBuffer(value);
        this.hostname = buffer.readPrefixedString();
        this.ports = buffer.readPrefixedString();
        this.applicationName = buffer.readPrefixedString();
        this.isAlive = buffer.readBoolean();
        this.identifier = buffer.readShort();
        return buffer.getOffset();
    }

    @Override
    public String toString() {
        return "AgentInfoBo{" +
                "hostname='" + hostname + '\'' +
                ", ports='" + ports + '\'' +
                ", agentId='" + agentId + '\'' +
                ", applicationName='" + applicationName + '\'' +
                ", isAlive=" + isAlive +
                ", timestamp=" + timestamp +
                ", identifier=" + identifier +
                '}';
    }
}
