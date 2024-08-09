package com.navercorp.pinpoint.web.vo.tree;

import com.navercorp.pinpoint.web.hyperlink.HyperLink;
import com.navercorp.pinpoint.web.hyperlink.HyperLinkFactory;
import com.navercorp.pinpoint.web.hyperlink.LinkSources;
import com.navercorp.pinpoint.web.vo.agent.AgentAndStatus;
import com.navercorp.pinpoint.web.vo.agent.AgentInfo;
import com.navercorp.pinpoint.web.vo.agent.AgentStatus;
import com.navercorp.pinpoint.web.vo.agent.AgentStatusAndLink;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public class AgentsMapByHost {
    private final InstancesListMap<AgentStatusAndLink> instancesListMap;

    public static final String CONTAINER = "Container";
    private static final Comparator<String> CONTAINER_GOES_UP = Comparator.comparing((String s) -> !s.equals(CONTAINER))
            .thenComparing(Comparator.naturalOrder());

    public AgentsMapByHost(InstancesListMap<AgentStatusAndLink> instancesListMap) {
        this.instancesListMap = Objects.requireNonNull(instancesListMap, "agentsListMap");
    }

    public List<InstancesList<AgentStatusAndLink>> getAgentsListsList() {
        return new ArrayList<>(instancesListMap.getListMap());
    }

    public static AgentsMapByHost newAgentsMapByHost(Predicate<AgentAndStatus> agentStatusPredicate,
                                                     SortByAgentInfo<AgentStatusAndLink> sortByAgentInfo,
                                                     HyperLinkFactory hyperLinkFactory,
                                                     Collection<AgentAndStatus> agentCollection) {
        Objects.requireNonNull(agentStatusPredicate, "agentStatusPredicate");
        Objects.requireNonNull(sortByAgentInfo, "sortBy");
        Objects.requireNonNull(agentCollection, "agentCollection");

        InstancesListMap<AgentStatusAndLink> instancesListMap =
                new InstancesListMapBuilder<>(
                        AgentsMapByHost::containerOrPhysical,
                        CONTAINER_GOES_UP,
                        sortByAgentInfo.getComparator(),
                        agentCollection,
                        x -> newAgentStatusAndLink(x, hyperLinkFactory)
                )
                        .withFilterBefore(agentStatusPredicate)
                        .build();

        return new AgentsMapByHost(instancesListMap);
    }

    private static String containerOrPhysical(AgentStatusAndLink agentStatusAndLink) {
        if (agentStatusAndLink.getAgentInfo().isContainer()) {
            return CONTAINER;
        }
        return agentStatusAndLink.getAgentInfo().getHostName();
    }

    private static AgentStatusAndLink newAgentStatusAndLink(AgentAndStatus agentAndStatus, HyperLinkFactory hyperLinkFactory) {
        AgentInfo agentInfo = agentAndStatus.getAgentInfo();
        AgentStatus status = agentAndStatus.getStatus();
        List<HyperLink> hyperLinks = hyperLinkFactory.build(LinkSources.from(agentInfo));
        return new AgentStatusAndLink(agentInfo, status, hyperLinks);
    }

    public int size() {
        return instancesListMap.size();
    }

    @Override
    public String toString() {
        return "AgentsMapByHost{" +
                "instancesListMap=" + instancesListMap +
                '}';
    }
}
