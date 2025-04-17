package com.navercorp.pinpoint.web.applicationmap.view;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.navercorp.pinpoint.common.server.util.AgentLifeCycleState;
import com.navercorp.pinpoint.web.applicationmap.nodes.ServerInstance;

import java.util.Objects;

public class ServerInstanceView {
    private final ServerInstance serverInstance;

    public ServerInstanceView(ServerInstance serverInstance) {
        this.serverInstance = Objects.requireNonNull(serverInstance, "serverInstance");
    }

    @JsonProperty("hasInspector")
    public boolean hasInspector() {
        return serverInstance.getServiceType().isWas();
    }

    public String getName() {
        return serverInstance.getName();
    }

    public String getAgentName() {
        return serverInstance.getAgentName();
    }


    public String getServiceType() {
        return serverInstance.getServiceType().getName();
    }

    public AgentLifeCycleState getStatus() {
        return serverInstance.getStatus();
    }

}
