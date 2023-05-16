package com.navercorp.pinpoint.web.vo.agent;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.navercorp.pinpoint.web.hyperlink.HyperLink;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

public class AgentStatusAndLink {
    private final AgentInfo agentInfo;
    private final AgentStatus agentStatus;
    private final List<HyperLink> hyperLinkList;

    public AgentStatusAndLink(AgentInfo agentInfo, @Nullable AgentStatus agentStatus, List<HyperLink> hyperLinkList) {
        this.agentInfo = Objects.requireNonNull(agentInfo, "agentInfo");
        this.agentStatus = agentStatus;
        this.hyperLinkList = Objects.requireNonNull(hyperLinkList, "hyperLinkList");
    }

    @JsonUnwrapped
    public AgentInfo getAgentInfo() {
        return agentInfo;
    }

    public AgentStatus getStatus() {
        return agentStatus;
    }

    @JsonProperty("linkList")
    public List<HyperLink> getHyperLinkList() {
        return hyperLinkList;
    }

    @Override
    public String toString() {
        return "AgentStatusAndLink{" +
                "agentInfo=" + agentInfo +
                ", agentStatus=" + agentStatus +
                ", hyperLinkList=" + hyperLinkList +
                '}';
    }
}
