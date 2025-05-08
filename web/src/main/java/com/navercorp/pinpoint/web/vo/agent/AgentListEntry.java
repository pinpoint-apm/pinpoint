package com.navercorp.pinpoint.web.vo.agent;

import java.util.Objects;

public class AgentListEntry {

    private final String agentId;
    private final String agentName;
    private final long startTimestamp;

    public AgentListEntry(String agentId, String agentName, long agentStartTime) {
        this.agentId = Objects.requireNonNull(agentId, "agentId");
        this.agentName = Objects.requireNonNull(agentName, "agentName");
        this.startTimestamp = agentStartTime;
    }

    public String getAgentId() {
        return agentId;
    }

    public String getAgentName() {
        return agentName;
    }

    public long getStartTimestamp() {
        return startTimestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AgentListEntry that = (AgentListEntry) o;

        return agentId.equals(that.agentId);
    }

    @Override
    public int hashCode() {
        return agentId.hashCode();
    }

    @Override
    public String toString() {
        return "AgentListEntry{" +
                "agentId=" + agentId +
                ", agentName='" + agentName + '\'' +
                ", startTimestamp=" + startTimestamp +
                '}';
    }
}
