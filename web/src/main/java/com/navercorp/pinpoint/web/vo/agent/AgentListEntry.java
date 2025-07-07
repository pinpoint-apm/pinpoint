package com.navercorp.pinpoint.web.vo.agent;

import java.util.Objects;

public class AgentListEntry {

    private final String id;
    private final String name;
    private final long startTimestamp;
    private final AgentStatus agentStatus;

    public AgentListEntry(String agentId, String agentName, long agentStartTime, AgentStatus agentStatus) {
        this.id = Objects.requireNonNull(agentId, "agentId");
        this.name = Objects.requireNonNull(agentName, "agentName");
        this.startTimestamp = agentStartTime;
        this.agentStatus = agentStatus;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public long getStartTimestamp() {
        return startTimestamp;
    }

    public AgentStatus getAgentStatus() {
        return agentStatus;
    }

    @Override
    public String toString() {
        return "AgentListEntry{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", startTimestamp=" + startTimestamp +
                ", agentStatus=" + agentStatus +
                '}';
    }
}
