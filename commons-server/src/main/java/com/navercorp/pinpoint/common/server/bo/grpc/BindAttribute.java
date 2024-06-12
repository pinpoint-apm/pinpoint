package com.navercorp.pinpoint.common.server.bo.grpc;

import com.navercorp.pinpoint.common.id.AgentId;
import com.navercorp.pinpoint.common.id.ApplicationId;
import com.navercorp.pinpoint.grpc.Header;

import java.util.Objects;

public class BindAttribute {
    private final AgentId agentId;
    private final String applicationName;
    private final ApplicationId applicationId;
    private final long agentStartTime;
    private final long acceptedTime;

    public static BindAttribute of(Header header, ApplicationId applicationId, long acceptedTime) {
        return new BindAttribute(header.getAgentId(),
                header.getApplicationName(),
                applicationId,
                header.getAgentStartTime(),
                acceptedTime);
    }

    public BindAttribute(AgentId agentId, String applicationName, ApplicationId applicationId, long agentStartTime, long acceptedTime) {
        this.agentId = Objects.requireNonNull(agentId, "agentId");
        this.applicationName = Objects.requireNonNull(applicationName, "applicationName");
        this.applicationId = Objects.requireNonNull(applicationId, "applicationId");
        this.agentStartTime = agentStartTime;
        this.acceptedTime = acceptedTime;
    }

    public long getAcceptedTime() {
        return acceptedTime;
    }

    public AgentId getAgentId() {
        return this.agentId;
    }

    public String getApplicationName() {
        return this.applicationName;
    }

    public ApplicationId getApplicationId() {
        return this.applicationId;
    }

    public long getAgentStartTime() {
        return this.agentStartTime;
    }
}
