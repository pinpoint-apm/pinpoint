package com.navercorp.pinpoint.web.vo.tree;

import com.navercorp.pinpoint.web.vo.agent.AgentAndStatus;
import com.navercorp.pinpoint.web.vo.agent.AgentInfo;
import com.navercorp.pinpoint.web.vo.agent.AgentStatusFilter;
import com.navercorp.pinpoint.web.vo.agent.AgentStatus;
import com.navercorp.pinpoint.web.vo.agent.DetailedAgentAndStatus;
import com.navercorp.pinpoint.web.vo.agent.DetailedAgentInfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

public class AgentsMapByApplication<T> {

    private final InstancesListMap<T> instancesListMap;

    private AgentsMapByApplication(InstancesListMap<T> instancesListMap) {
        this.instancesListMap = Objects.requireNonNull(instancesListMap, "agentsListMap");
    }

    public List<InstancesList<T>> getAgentsListsList() {
        return new ArrayList<>(instancesListMap.getListMap());
    }

    public static AgentsMapByApplication<AgentAndStatus> newAgentAndStatusMap(
            AgentStatusFilter filter,
            Collection<AgentAndStatus> agentAndStatuses
    ) {
        return AgentsMapByApplication.newAgentsMapByApplication(
                filter,
                agentAndStatuses,
                AgentAndStatus::getAgentInfo,
                AgentAndStatus::getStatus,
                Function.identity()
        );
    }

    public static AgentsMapByApplication<DetailedAgentInfo> newDetailedAgentInfoMap(
            AgentStatusFilter filter,
            Collection<DetailedAgentAndStatus> agentAndStatuses
    ) {
        return AgentsMapByApplication.newAgentsMapByApplication(
                filter,
                agentAndStatuses,
                DetailedAgentInfo::getAgentInfo,
                DetailedAgentAndStatus::getStatus,
                DetailedAgentAndStatus::getDetailedAgentInfo
        );
    }

    public static <I, T> AgentsMapByApplication<T> newAgentsMapByApplication(
            AgentStatusFilter filter,
            Collection<I> agentCollection,
            Function<T, AgentInfo> agentInfoSupplier,
            Function<I, AgentStatus> agentStatusSupplier,
            Function<I, T> finisher
    ) {
        Objects.requireNonNull(filter, "filter");
        Objects.requireNonNull(agentCollection, "agentCollection");

        InstancesListMapBuilder<I, T> instancesListMapBuilder =
                new InstancesListMapBuilder<I, T>(
                        agentInfoSupplier.andThen(AgentsMapByApplication::byApplicationName),
                        Comparator.naturalOrder(),
                        SortByAgentInfo.agentIdAsc(agentInfoSupplier).getComparator(),
                        agentCollection
                );

        instancesListMapBuilder.withFilter((I x) -> filter.filter(agentStatusSupplier.apply(x)));
        instancesListMapBuilder.withFinisher(finisher);
        return new AgentsMapByApplication<T>(instancesListMapBuilder.build());
    }

    private static String byApplicationName(AgentInfo agentInfo) {
        return agentInfo.getApplicationName();
    }

    @Override
    public String toString() {
        return "AgentsMapByApplication{" +
                "instancesListMap=" + instancesListMap +
                '}';
    }
}