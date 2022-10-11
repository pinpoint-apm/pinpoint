package com.navercorp.pinpoint.web.vo.tree;

import com.navercorp.pinpoint.web.hyperlink.HyperLink;
import com.navercorp.pinpoint.web.hyperlink.HyperLinkFactory;
import com.navercorp.pinpoint.web.hyperlink.LinkSources;
import com.navercorp.pinpoint.web.vo.agent.AgentAndStatus;
import com.navercorp.pinpoint.web.vo.agent.AgentInfo;
import com.navercorp.pinpoint.web.vo.agent.AgentInfoFilter;
import com.navercorp.pinpoint.web.vo.agent.AgentStatus;
import com.navercorp.pinpoint.web.vo.agent.AgentStatusAndLink;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class AgentsMapByApplication {

    private final AgentsListMap<AgentStatusAndLink> agentsListMap;

    private AgentsMapByApplication(AgentsListMap<AgentStatusAndLink> agentsListMap) {
        this.agentsListMap = Objects.requireNonNull(agentsListMap, "agentsListMap");
    }

    public List<AgentsList<AgentStatusAndLink>> getAgentsListsList() {
        return new ArrayList<>(agentsListMap.getListMap());
    }

    public static AgentsMapByApplication newAgentsMapByApplication(AgentInfoFilter filter,
                                                                   HyperLinkFactory hyperLinkFactory,
                                                                   Collection<AgentAndStatus> agentCollection) {
        Objects.requireNonNull(filter, "filter");
        Objects.requireNonNull(hyperLinkFactory, "hyperLinkFactory");
        Objects.requireNonNull(agentCollection, "agentCollection");

        AgentsListMapBuilder<AgentAndStatus, AgentStatusAndLink> agentsListMapBuilder =
                new AgentsListMapBuilder<>(
                        AgentsMapByApplication::byApplicationName,
                        Comparator.naturalOrder(),
                        SortBy.agentIdAsc(AgentStatusAndLink::getAgentInfo),
                        agentCollection
                );

        agentsListMapBuilder.withFilter(filter::filter)
                .withFinisher(x -> newAgentStatusAndLink(x, hyperLinkFactory));
        return new AgentsMapByApplication(agentsListMapBuilder.build());
    }

    private static String byApplicationName(AgentStatusAndLink agentStatusAndLink) {
        return agentStatusAndLink.getAgentInfo().getApplicationName();
    }

    private static AgentStatusAndLink newAgentStatusAndLink(AgentAndStatus agentAndStatus, HyperLinkFactory hyperLinkFactory) {
        AgentInfo agentInfo = agentAndStatus.getAgentInfo();
        AgentStatus status = agentAndStatus.getStatus();
        List<HyperLink> hyperLinks = hyperLinkFactory.build(LinkSources.from(agentInfo));
        return new AgentStatusAndLink(agentInfo, status, hyperLinks);
    }

    @Override
    public String toString() {
        return "AgentsMapByApplication{" +
                "agentsListMap=" + agentsListMap +
                '}';
    }

}
