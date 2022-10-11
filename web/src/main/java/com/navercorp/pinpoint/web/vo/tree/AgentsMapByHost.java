package com.navercorp.pinpoint.web.vo.tree;

import com.navercorp.pinpoint.web.vo.agent.AgentAndStatus;
import com.navercorp.pinpoint.web.vo.agent.AgentInfoFilter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class AgentsMapByHost {
    private final AgentsListMap<AgentAndStatus> agentsListMap;

    public static final String CONTAINER = "Container";
    private static final Comparator<String> CONTAINER_GOES_UP = Comparator.comparing((String s) -> !s.equals(CONTAINER))
            .thenComparing(Comparator.naturalOrder());

    private AgentsMapByHost(AgentsListMap<AgentAndStatus> agentsListMap) {
        this.agentsListMap = Objects.requireNonNull(agentsListMap, "agentsListMap");
    }

    public List<AgentsList<AgentAndStatus>> getAgentsListsList() {
        return new ArrayList<>(agentsListMap.getListMap());
    }

    public static AgentsMapByHost newAgentsMapByHost(AgentInfoFilter filter,
                                                     SortBy<AgentAndStatus> sortBy,
                                                     Collection<AgentAndStatus> agentCollection) {
        Objects.requireNonNull(filter, "filter");
        Objects.requireNonNull(sortBy, "sortBy");
        Objects.requireNonNull(agentCollection, "agentCollection");

        AgentsListMapBuilder<AgentAndStatus, AgentAndStatus> agentsListMapBuilder =
                new AgentsListMapBuilder<>(
                        AgentsMapByHost::containerOrPhysical,
                        CONTAINER_GOES_UP,
                        sortBy,
                        agentCollection
                );
        agentsListMapBuilder.withFilter(filter::filter);

        return new AgentsMapByHost(agentsListMapBuilder.build());
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
                "agentsListMap=" + agentsListMap +
                '}';
    }
}
