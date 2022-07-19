package com.navercorp.pinpoint.web.vo;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

import javax.annotation.Nullable;
import java.util.Objects;

public class AgentAndStatus {
    private final AgentInfo agentInfo;
    private final AgentStatus status;

    public AgentAndStatus(AgentInfo agentInfo, @Nullable AgentStatus status) {
        this.agentInfo = Objects.requireNonNull(agentInfo, "agentInfo");
        this.status = status;
    }

    public AgentAndStatus(AgentInfo agentInfo) {
        this(agentInfo, null);
    }

    @JsonUnwrapped
    public AgentInfo getAgentInfo() {
        return agentInfo;
    }

    public AgentStatus getStatus() {
        return status;
    }
}
