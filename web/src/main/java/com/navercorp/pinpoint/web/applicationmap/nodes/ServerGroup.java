package com.navercorp.pinpoint.web.applicationmap.nodes;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

public class ServerGroup {
    private final String hostName;

    private final String status;

    private final List<ServerInstance> instanceList;

    public ServerGroup(String hostName, @Nullable String status, List<ServerInstance> instanceList) {
        this.hostName = Objects.requireNonNull(hostName, "hostName");
        this.status = status;
        this.instanceList = Objects.requireNonNull(instanceList, "instanceList");
    }

    @JsonProperty("name")
    public String getHostName() {
        return hostName;
    }

    public String getStatus() {
        return status;
    }

    public List<ServerInstance> getInstanceList() {
        return instanceList;
    }
}
