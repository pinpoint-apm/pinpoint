package com.navercorp.pinpoint.web.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.navercorp.pinpoint.web.hyperlink.HyperLink;

import java.util.List;
import java.util.Objects;

public class AgentInfoAndLink {
    private final AgentInfo agentInfo;
    private final List<HyperLink> hyperLinkList;

    public AgentInfoAndLink(AgentInfo agentInfo, List<HyperLink> hyperLinkList) {
        this.agentInfo = Objects.requireNonNull(agentInfo, "agentInfo");
        this.hyperLinkList = Objects.requireNonNull(hyperLinkList, "hyperLinkList");
    }

    @JsonUnwrapped
    public AgentInfo getAgentInfo() {
        return agentInfo;
    }

    @JsonProperty("linkList")
    public List<HyperLink> getHyperLinkList() {
        return hyperLinkList;
    }

    @Override
    public String toString() {
        return "AgentInfoAndLink{" +
                "agentInfo=" + agentInfo +
                ", hyperLinkList=" + hyperLinkList +
                '}';
    }
}
