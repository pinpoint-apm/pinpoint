package com.navercorp.pinpoint.web.vo.tree;

import com.navercorp.pinpoint.web.vo.agent.AgentAndStatus;
import com.navercorp.pinpoint.web.vo.agent.AgentInfoFilter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class AgentsMapByHost {
    private final InstancesListMap<AgentAndStatus> instancesListMap;

    public static final String CONTAINER = "Container";
    private static final Comparator<String> CONTAINER_GOES_UP = Comparator.comparing((String s) -> !s.equals(CONTAINER))
            .thenComparing(Comparator.naturalOrder());

    private AgentsMapByHost(InstancesListMap<AgentAndStatus> instancesListMap) {
        this.instancesListMap = Objects.requireNonNull(instancesListMap, "agentsListMap");
    }

    public List<InstancesList<AgentAndStatus>> getAgentsListsList() {
        return new ArrayList<>(instancesListMap.getListMap());
    }

    public static AgentsMapByHost newAgentsMapByHost(AgentInfoFilter filter,
                                                     SortByAgentInfo<AgentAndStatus> sortByAgentInfo,
                                                     Collection<AgentAndStatus> agentCollection) {
        Objects.requireNonNull(filter, "filter");
        Objects.requireNonNull(sortByAgentInfo, "sortBy");
        Objects.requireNonNull(agentCollection, "agentCollection");

        InstancesListMapBuilder<AgentAndStatus, AgentAndStatus> instancesListMapBuilder =
                new InstancesListMapBuilder<>(
                        AgentsMapByHost::containerOrPhysical,
                        CONTAINER_GOES_UP,
                        sortByAgentInfo.getComparator(),
                        agentCollection
                );
        instancesListMapBuilder.withFilter(filter::filter);

        return new AgentsMapByHost(instancesListMapBuilder.build());
    }

    private static String containerOrPhysical(AgentAndStatus agentAndStatus) {
        if (agentAndStatus.getAgentInfo().isContainer()) {
            return CONTAINER;
        }
        return agentAndStatus.getAgentInfo().getHostName();
    }

    @Override
    public String toString() {
        return "AgentsMapByHost{" +
                "instancesListMap=" + instancesListMap +
                '}';
    }
}
