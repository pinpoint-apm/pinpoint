package com.navercorp.pinpoint.web.vo.agent;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

import java.util.Objects;

/**
 * @author Woonduk Kang(emeroad)
 */
public class DetailedAgentAndStatus {
    private final DetailedAgentInfo detailedAgentInfo;
    private final AgentStatus status;

    public DetailedAgentAndStatus(DetailedAgentInfo detailedAgentInfo, AgentStatus status) {
        this.detailedAgentInfo = Objects.requireNonNull(detailedAgentInfo, "detailedAgentInfo");
        this.status = Objects.requireNonNull(status, "status");
    }

    @JsonUnwrapped
    public DetailedAgentInfo getDetailedAgentInfo() {
        return detailedAgentInfo;
    }

    public AgentStatus getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return "DetailedAgentAndStatus{" +
                "detailedAgentInfo=" + detailedAgentInfo +
                ", status=" + status +
                '}';
    }
}
