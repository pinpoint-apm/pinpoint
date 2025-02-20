package com.navercorp.pinpoint.profiler.name;

import java.util.Objects;

public class ObjectNameValidationFailedException extends IllegalArgumentException {
    private final AgentIdType agentIdType;

    public ObjectNameValidationFailedException(AgentIdType agentIdType, String s) {
        super(s);
        this.agentIdType = Objects.requireNonNull(agentIdType, "agentIdType");
    }

    public ObjectNameValidationFailedException(AgentIdType agentIdType, String message, Throwable cause) {
        super(message, cause);
        this.agentIdType = Objects.requireNonNull(agentIdType, "agentIdType");
    }

    public ObjectNameValidationFailedException(AgentIdType agentIdType, Throwable cause) {
        super(cause);
        this.agentIdType = Objects.requireNonNull(agentIdType, "agentIdType");
    }

    public AgentIdType getAgentIdType() {
        return agentIdType;
    }
}
