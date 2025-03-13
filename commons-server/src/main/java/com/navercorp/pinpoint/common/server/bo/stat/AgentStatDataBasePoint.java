package com.navercorp.pinpoint.common.server.bo.stat;

import com.navercorp.pinpoint.common.server.util.StringPrecondition;

import java.util.Objects;

public abstract class AgentStatDataBasePoint implements AgentStatDataPoint {
    protected String agentId;
    protected String applicationName;
    protected long startTimestamp;
    protected long timestamp;

    @Override
    public String getAgentId() {
        return agentId;
    }

    @Override
    public void setAgentId(String agentId) {
        this.agentId = StringPrecondition.requireHasLength(agentId, "agentId");
    }

    @Override
    public long getStartTimestamp() {
        return startTimestamp;
    }

    @Override
    public void setStartTimestamp(long startTimestamp) {
        this.startTimestamp = startTimestamp;
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String getApplicationName() {
        return applicationName;
    }

    @Override
    public void setApplicationName(String applicationName) {
        this.applicationName = StringPrecondition.requireHasLength(applicationName, "applicationName");
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        AgentStatDataBasePoint that = (AgentStatDataBasePoint) o;
        return startTimestamp == that.startTimestamp && timestamp == that.timestamp && Objects.equals(agentId, that.agentId) && Objects.equals(applicationName, that.applicationName);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(agentId);
        result = 31 * result + Objects.hashCode(applicationName);
        result = 31 * result + Long.hashCode(startTimestamp);
        result = 31 * result + Long.hashCode(timestamp);
        return result;
    }
}
