package com.navercorp.pinpoint.common.server.bo.stat;

import com.navercorp.pinpoint.common.server.util.StringPrecondition;

import java.util.Objects;

public class DefaultDataPoint implements DataPoint {

    private final String agentId;
    private final String applicationName;
    private final long startTimestamp;
    private final long timestamp;

    DefaultDataPoint(String agentId, String applicationName, long startTimestamp, long timestamp) {
        this.agentId = StringPrecondition.requireHasLength(agentId, "agentId");
        this.applicationName = StringPrecondition.requireHasLength(applicationName, "applicationName");
        this.startTimestamp = startTimestamp;
        this.timestamp = timestamp;
    }

    @Override
    public String getAgentId() {
        return agentId;
    }

    @Override
    public long getStartTimestamp() {
        return startTimestamp;
    }


    @Override
    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public String getApplicationName() {
        return applicationName;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        DefaultDataPoint that = (DefaultDataPoint) o;
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

    @Override
    public String toString() {
        return "{id=" + applicationName + "/" + agentId +
                ", startTimestamp=" + startTimestamp +
                ", timestamp=" + timestamp +
                '}';
    }
}
