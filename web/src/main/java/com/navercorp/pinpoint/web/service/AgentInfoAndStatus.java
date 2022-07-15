package com.navercorp.pinpoint.web.service;

import com.navercorp.pinpoint.web.vo.AgentInfo;
import com.navercorp.pinpoint.web.vo.AgentStatus;

import java.util.Objects;

public class AgentInfoAndStatus {
    private final AgentInfo agentInfo;
    private final AgentStatus status;

    public AgentInfoAndStatus(AgentInfo agentInfo, AgentStatus status) {
        this.agentInfo = Objects.requireNonNull(agentInfo, "agentInfo");
        this.status = Objects.requireNonNull(status, "status");
    }

    public AgentInfo getAgentInfo() {
        return agentInfo;
    }

    public AgentStatus getStatus() {
        return status;
    }
}
