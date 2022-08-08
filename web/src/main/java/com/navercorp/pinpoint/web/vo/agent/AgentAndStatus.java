package com.navercorp.pinpoint.web.vo.agent;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AgentAndStatus that = (AgentAndStatus) o;

        return agentInfo != null ? agentInfo.equals(that.agentInfo) : that.agentInfo == null;
    }

    @Override
    public int hashCode() {
        return agentInfo != null ? agentInfo.hashCode() : 0;
    }
}
