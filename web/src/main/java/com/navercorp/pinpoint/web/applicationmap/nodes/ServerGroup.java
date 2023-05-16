package com.navercorp.pinpoint.web.applicationmap.nodes;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.navercorp.pinpoint.web.hyperlink.HyperLink;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

public class ServerGroup {
    private final String hostName;

    private final String status;
    private final List<HyperLink> linkList;

    private final List<ServerInstance> instanceList;

    public ServerGroup(String hostName, @Nullable String status, List<HyperLink> linkList, List<ServerInstance> instanceList) {
        this.hostName = Objects.requireNonNull(hostName, "hostName");
        this.status = status;
        this.linkList = Objects.requireNonNull(linkList, "linkList");
        this.instanceList = instanceList;
    }

    @JsonProperty("name")
    public String getHostName() {
        return hostName;
    }

    public String getStatus() {
        return status;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public List<HyperLink> getLinkList() {
        return linkList;
    }

    public List<ServerInstance> getInstanceList() {
        return instanceList;
    }
}
