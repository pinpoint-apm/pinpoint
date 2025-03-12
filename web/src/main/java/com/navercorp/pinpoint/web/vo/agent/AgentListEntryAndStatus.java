package com.navercorp.pinpoint.web.vo.agent;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import jakarta.annotation.Nullable;

import java.util.Objects;

public class AgentListEntryAndStatus {

    private final AgentListEntry agentListEntry;
    private final AgentStatus status;

    public AgentListEntryAndStatus(AgentListEntry agentListEntry, @Nullable AgentStatus status) {
        this.agentListEntry = Objects.requireNonNull(agentListEntry, "agentListEntry");
        this.status = status;
    }

    @JsonUnwrapped
    public AgentListEntry getAgentListEntry() {
        return agentListEntry;
    }

    public AgentStatus getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return "AgentListEntryAndStatus{" +
                "agentListEntry=" + agentListEntry +
                ", status=" + status +
                '}';
    }
}
