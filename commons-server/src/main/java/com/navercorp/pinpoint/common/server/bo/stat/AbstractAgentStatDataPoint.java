package com.navercorp.pinpoint.common.server.bo.stat;

import java.util.Objects;

public abstract class AbstractAgentStatDataPoint implements AgentStatDataPoint {

    protected static final long UNCOLLECTED_LONG = -1;
    protected static final int UNCOLLECTED_INT = -1;
    protected static final double UNCOLLECTED_DOUBLE = -1;

    private String agentId;
    private long startTimestamp;
    private long timestamp;

    private final AgentStatType agentStatType;

    public AbstractAgentStatDataPoint(AgentStatType agentStatType) {
        this.agentStatType = Objects.requireNonNull(agentStatType, "agentStatType");
    }

    @Override
    public String getAgentId() {
        return agentId;
    }

    @Override
    public void setAgentId(String agentId) {
        this.agentId = agentId;
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
    public AgentStatType getAgentStatType() {
        return agentStatType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AbstractAgentStatDataPoint that = (AbstractAgentStatDataPoint) o;

        if (startTimestamp != that.startTimestamp) return false;
        if (timestamp != that.timestamp) return false;
        if (!Objects.equals(agentId, that.agentId)) return false;
        return agentStatType == that.agentStatType;
    }

    @Override
    public int hashCode() {
        int result = agentId != null ? agentId.hashCode() : 0;
        result = 31 * result + (int) (startTimestamp ^ (startTimestamp >>> 32));
        result = 31 * result + (int) (timestamp ^ (timestamp >>> 32));
        result = 31 * result + (agentStatType != null ? agentStatType.hashCode() : 0);
        return result;
    }
}
