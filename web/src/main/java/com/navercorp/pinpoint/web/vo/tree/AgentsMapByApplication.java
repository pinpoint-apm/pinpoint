package com.navercorp.pinpoint.web.vo.tree;

import com.navercorp.pinpoint.web.vo.agent.AgentAndStatus;
import com.navercorp.pinpoint.web.vo.agent.AgentInfoFilter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class AgentsMapByApplication {

    private final InstancesListMap<AgentAndStatus> instancesListMap;

    private AgentsMapByApplication(InstancesListMap<AgentAndStatus> instancesListMap) {
        this.instancesListMap = Objects.requireNonNull(instancesListMap, "agentsListMap");
    }

    public List<InstancesList<AgentAndStatus>> getAgentsListsList() {
        return new ArrayList<>(instancesListMap.getListMap());
    }

    public static AgentsMapByApplication newAgentsMapByApplication(AgentInfoFilter filter,
                                                                   Collection<AgentAndStatus> agentCollection) {
        Objects.requireNonNull(filter, "filter");
        Objects.requireNonNull(agentCollection, "agentCollection");

        InstancesListMapBuilder<AgentAndStatus, AgentAndStatus> instancesListMapBuilder =
                new InstancesListMapBuilder<>(
                        AgentsMapByApplication::byApplicationName,
                        Comparator.naturalOrder(),
                        SortByAgentInfo.agentIdAsc(AgentAndStatus::getAgentInfo).getComparator(),
                        agentCollection
                );

        instancesListMapBuilder.withFilter(filter::filter);
        return new AgentsMapByApplication(instancesListMapBuilder.build());
    }

    private static String byApplicationName(AgentAndStatus agentAndStatus) {
        return agentAndStatus.getAgentInfo().getApplicationName();
    }

    @Override
    public String toString() {
        return "AgentsMapByApplication{" +
                "instancesListMap=" + instancesListMap +
                '}';
    }
}