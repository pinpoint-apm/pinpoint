package com.navercorp.pinpoint.common.server.bo.grpc;

import com.navercorp.pinpoint.grpc.Header;

import java.util.Objects;

public class BindAttribute {
    private final String agentId;
    private final String applicationName;
    private final long agentStartTime;
    private final long acceptedTime;

    public static BindAttribute of(Header header, long acceptedTime) {
        return new BindAttribute(header.getAgentId(),
                header.getApplicationName(),
                header.getAgentStartTime(),
                acceptedTime);
    }

    public BindAttribute(String agentId, String applicationName, long agentStartTime, long acceptedTime) {
        this.agentId = Objects.requireNonNull(agentId, "agentId");
        this.applicationName = Objects.requireNonNull(applicationName, "applicationName");
        this.agentStartTime = agentStartTime;
        this.acceptedTime = acceptedTime;
    }

    public long getAcceptedTime() {
        return acceptedTime;
    }

    public String getAgentId() {
        return this.agentId;
    }

    public String getApplicationName() {
        return this.applicationName;
    }

    public long getAgentStartTime() {
        return this.agentStartTime;
    }
}
