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

    private final InstancesListMap<AgentStatusAndLink> instancesListMap;

    private AgentsMapByApplication(InstancesListMap<AgentStatusAndLink> instancesListMap) {
        this.instancesListMap = Objects.requireNonNull(instancesListMap, "agentsListMap");
    }

    public List<InstancesList<AgentStatusAndLink>> getAgentsListsList() {
        return new ArrayList<>(instancesListMap.getListMap());
    }

    public static AgentsMapByApplication newAgentsMapByApplication(AgentInfoFilter filter,
                                                                   HyperLinkFactory hyperLinkFactory,
                                                                   Collection<AgentAndStatus> agentCollection) {
        Objects.requireNonNull(filter, "filter");
        Objects.requireNonNull(hyperLinkFactory, "hyperLinkFactory");
        Objects.requireNonNull(agentCollection, "agentCollection");

        InstancesListMapBuilder<AgentAndStatus, AgentStatusAndLink> instancesListMapBuilder =
                new InstancesListMapBuilder<>(
                        AgentsMapByApplication::byApplicationName,
                        Comparator.naturalOrder(),
                        SortByAgentInfo.agentIdAsc(AgentStatusAndLink::getAgentInfo).getComparator(),
                        agentCollection
                );

        instancesListMapBuilder.withFilter(filter::filter)
                .withFinisher(x -> newAgentStatusAndLink(x, hyperLinkFactory));
        return new AgentsMapByApplication(instancesListMapBuilder.build());
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
                "instancesListMap=" + instancesListMap +
                '}';
    }
}
