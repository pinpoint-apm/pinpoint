/*
 * Copyright 2026 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.navercorp.pinpoint.web.vo.agent;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.navercorp.pinpoint.web.hyperlink.HyperLink;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Objects;

/**
 * Groups agents sharing the same agentName.
 * Base info (agentId, hostName, ip, etc.) is taken from the agent with the latest startTimestamp.
 *
 * @author intr3p1d
 */
public class AgentNameGroupView {
    private final AgentInfo agentInfo;
    private final List<String> agentIds;
    @Nullable
    private final AgentStatus agentStatus;
    private final List<HyperLink> hyperLinkList;

    public AgentNameGroupView(
            AgentInfo agentInfo,
            List<String> agentIds,
            @Nullable AgentStatus agentStatus,
            List<HyperLink> hyperLinkList) {
        this.agentInfo = Objects.requireNonNull(agentInfo, "agentInfo");
        this.agentIds = Objects.requireNonNull(agentIds, "agentIds");
        this.agentStatus = agentStatus;
        this.hyperLinkList = Objects.requireNonNull(hyperLinkList, "hyperLinkList");
    }

    @JsonUnwrapped
    public AgentInfo getAgentInfo() {
        return agentInfo;
    }

    @JsonProperty("agentIds")
    public List<String> getAgentIds() {
        return agentIds;
    }

    @Nullable
    public AgentStatus getStatus() {
        return agentStatus;
    }

    @JsonProperty("hasInspector")
    public boolean hasInspector() {
        return agentInfo.getServiceType().isWas();
    }

    @JsonProperty("linkList")
    public List<HyperLink> getHyperLinkList() {
        return hyperLinkList;
    }

    @Override
    public String toString() {
        return "AgentNameGroupView{" +
                "agentInfo=" + agentInfo +
                ", agentIds=" + agentIds +
                ", agentStatus=" + agentStatus +
                ", hyperLinkList=" + hyperLinkList +
                '}';
    }
}
