package com.navercorp.pinpoint.web.view.id;

import com.navercorp.pinpoint.web.vo.Application;

import java.util.Objects;

public record AgentNameView(String agentName) {

    public static AgentNameView of(Application application) {
        Objects.requireNonNull(application, "application");
        return new AgentNameView(application.name());
    }

    public AgentNameView(String agentName) {
        this.agentName = Objects.requireNonNull(agentName, "agentName");
    }

    @Override
    public String toString() {
        return agentName;
    }
}
