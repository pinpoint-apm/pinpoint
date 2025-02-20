package com.navercorp.pinpoint.profiler.name;

public interface IdValidator {
    boolean validateAgentId(ObjectNameProperty agentId);

    boolean validateApplicationName(ObjectNameProperty applicationName);

    boolean validateAgentName(ObjectNameProperty agentName);

    boolean validateServiceName(ObjectNameProperty serviceName);
}
