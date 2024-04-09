package com.navercorp.pinpoint.web.vo.tree;

import com.navercorp.pinpoint.web.vo.agent.AgentAndStatus;
import com.navercorp.pinpoint.web.vo.agent.AgentInfo;
import com.navercorp.pinpoint.web.vo.agent.AgentStatus;
import com.navercorp.pinpoint.web.vo.agent.AgentStatusFilter;
import com.navercorp.pinpoint.web.vo.agent.DetailedAgentAndStatus;
import com.navercorp.pinpoint.web.vo.agent.DetailedAgentInfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public class AgentsMapByApplication<T> {

    private final InstancesListMap<T> instancesListMap;

    private AgentsMapByApplication(InstancesListMap<T> instancesListMap) {
        this.instancesListMap = Objects.requireNonNull(instancesListMap, "agentsListMap");
    }

    public List<InstancesList<T>> getAgentsListsList() {
        return new ArrayList<>(instancesListMap.getListMap());
    }

    public static AgentsMapByApplication<AgentAndStatus> newAgentAndStatusMap(
            AgentStatusFilter agentStatusFilter,
            Collection<AgentAndStatus> agentAndStatuses
    ) {
        return AgentsMapByApplication.newAgentsMapByApplication(
                agentStatusFilter,
                agentAndStatuses,
                AgentAndStatus::getAgentInfo,
                AgentAndStatus::getStatus,
                Function.identity()
        );
    }

    public static AgentsMapByApplication<DetailedAgentInfo> newDetailedAgentInfoMap(
            AgentStatusFilter agentStatusPredicate,
            Collection<DetailedAgentAndStatus> agentAndStatuses
    ) {
        return AgentsMapByApplication.newAgentsMapByApplication(
                agentStatusPredicate,
                agentAndStatuses,
                DetailedAgentInfo::getAgentInfo,
                DetailedAgentAndStatus::getStatus,
                DetailedAgentAndStatus::getDetailedAgentInfo
        );
    }

    public static <I, T> AgentsMapByApplication<T> newAgentsMapByApplication(
            AgentStatusFilter agentStatusPredicate,
            Collection<I> agentCollection,
            Function<T, AgentInfo> agentInfoFn,
            Function<I, AgentStatus> agentStatusFn,
            Function<I, T> finisherFn
    ) {
        Objects.requireNonNull(agentStatusPredicate, "agentStatusPredicate");
        Objects.requireNonNull(agentCollection, "agentCollection");

        InstancesListMap<T> instancesListMap =
                new InstancesListMapBuilder<>(
                        agentInfoFn.andThen(AgentsMapByApplication::byApplicationName),
                        Comparator.naturalOrder(),
                        SortByAgentInfo.agentIdAsc(agentInfoFn).getComparator(),
                        agentCollection,
                        finisherFn
                )
                        .withFilterBefore((I x) -> agentStatusPredicate.test(agentStatusFn.apply(x)))
                        .build();

        return new AgentsMapByApplication<>(instancesListMap);
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