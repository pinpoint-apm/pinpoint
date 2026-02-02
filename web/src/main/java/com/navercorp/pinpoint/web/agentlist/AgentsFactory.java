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
package com.navercorp.pinpoint.web.agentlist;

import com.navercorp.pinpoint.web.applicationmap.nodes.NodeHistogramSummary;
import com.navercorp.pinpoint.web.applicationmap.nodes.ServerGroup;
import com.navercorp.pinpoint.web.applicationmap.nodes.ServerInstance;
import com.navercorp.pinpoint.web.hyperlink.HyperLink;
import com.navercorp.pinpoint.web.hyperlink.HyperLinkFactory;
import com.navercorp.pinpoint.web.hyperlink.LinkSources;
import com.navercorp.pinpoint.web.vo.agent.AgentAndStatus;
import com.navercorp.pinpoint.web.vo.agent.AgentInfo;
import com.navercorp.pinpoint.web.vo.agent.AgentStatus;
import com.navercorp.pinpoint.web.vo.agent.AgentStatusAndLink;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author intr3p1d
 */
public class AgentsFactory {

    public static List<AgentStatusAndLink> addLinks(
            HyperLinkFactory hyperLinkFactory,
            Collection<AgentAndStatus> agentCollection
    ) {
        return agentCollection.stream()
                .map(agentAndStatus -> {
                    AgentInfo agentInfo = agentAndStatus.getAgentInfo();
                    List<HyperLink> hyperLinks = hyperLinkFactory.build(LinkSources.from(agentInfo));
                    return new AgentStatusAndLink(
                            agentInfo,
                            agentAndStatus.getStatus(),
                            hyperLinks
                    );
                })
                .toList();
    }

    public static List<AgentStatusAndLink> extractVirtualNode(NodeHistogramSummary nodeHistogramSummary, HyperLinkFactory hyperLinkFactory) {
        List<AgentStatusAndLink> agentStatusAndLinks = new ArrayList<>();
        for (ServerGroup group : nodeHistogramSummary.getServerGroupList().getServerGroupList()) {
            for (ServerInstance instance : group.getInstanceList()) {
                AgentInfo agentInfo = new AgentInfo();
                agentInfo.setAgentId(instance.getName());
                agentInfo.setAgentName(instance.getAgentName(), instance.getName());
                agentInfo.setHostName(instance.getHostName());
                agentInfo.setIp(instance.getIp());
                agentInfo.setServiceType(instance.getServiceType());

                AgentStatus agentStatus = new AgentStatus(instance.getName(), instance.getStatus(), 0);

                agentStatusAndLinks.add(new AgentStatusAndLink(
                        agentInfo,
                        agentStatus,
                        newHyperLink(hyperLinkFactory, instance)
                ));
            }
        }
        return agentStatusAndLinks;
    }

    private static List<HyperLink> newHyperLink(HyperLinkFactory hyperLinkFactory, ServerInstance serverInstance) {
        if (serverInstance == null) {
            return List.of();
        }
        return hyperLinkFactory.build(LinkSources.from(serverInstance.getHostName(), serverInstance.getIp(), serverInstance.getServiceType()));
    }

}
