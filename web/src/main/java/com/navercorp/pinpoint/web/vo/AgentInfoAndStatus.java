package com.navercorp.pinpoint.web.vo;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

import java.util.Objects;

public class AgentInfoAndStatus {
    private final AgentInfo agentInfo;
    private final AgentStatus status;

    public AgentInfoAndStatus(AgentInfo agentInfo, AgentStatus status) {
        this.agentInfo = Objects.requireNonNull(agentInfo, "agentInfo");
        this.status = Objects.requireNonNull(status, "status");
    }

    @JsonUnwrapped
    public AgentInfo getAgentInfo() {
        return agentInfo;
    }

    public AgentStatus getStatus() {
        return status;
    }
}
