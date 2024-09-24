package com.navercorp.pinpoint.web.service;

import com.navercorp.pinpoint.common.server.util.AgentLifeCycleState;
import com.navercorp.pinpoint.common.server.util.time.Range;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.web.applicationmap.dao.MapResponseDao;
import com.navercorp.pinpoint.web.dao.AgentInfoDao;
import com.navercorp.pinpoint.web.dao.AgentLifeCycleDao;
import com.navercorp.pinpoint.web.dao.ApplicationIndexDao;
import com.navercorp.pinpoint.web.service.component.LegacyAgentCompatibility;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.ResponseTime;
import com.navercorp.pinpoint.web.vo.agent.AgentAndStatus;
import com.navercorp.pinpoint.web.vo.agent.AgentInfo;
import com.navercorp.pinpoint.web.vo.agent.AgentInfoFilter;
import com.navercorp.pinpoint.web.vo.agent.AgentInfoFilters;
import com.navercorp.pinpoint.web.vo.agent.AgentStatus;
import com.navercorp.pinpoint.web.vo.agent.AgentStatusFilter;
import com.navercorp.pinpoint.web.vo.agent.AgentStatusFilters;
import com.navercorp.pinpoint.web.vo.agent.AgentStatusQuery;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;


@Service
public class ApplicationAgentListServiceImpl implements ApplicationAgentListService {
    // add exclamatory mark to indicate that the agent info is not available and prioritize it higher in the natural order comparison.
    private static final String NO_AGENT_INFO = "!noAgentInfo";

    private final ApplicationIndexDao applicationIndexDao;
    private final AgentInfoDao agentInfoDao;

    private final AgentLifeCycleDao agentLifeCycleDao;
    private final LegacyAgentCompatibility legacyAgentCompatibility;

    private final MapResponseDao mapResponseDao;

    public ApplicationAgentListServiceImpl(ApplicationIndexDao applicationIndexDao, AgentInfoDao agentInfoDao, AgentLifeCycleDao agentLifeCycleDao, LegacyAgentCompatibility legacyAgentCompatibility, MapResponseDao mapResponseDao) {
        this.applicationIndexDao = Objects.requireNonNull(applicationIndexDao, "applicationIndexDao");
        this.agentInfoDao = Objects.requireNonNull(agentInfoDao, "agentInfoDao");
        this.agentLifeCycleDao = Objects.requireNonNull(agentLifeCycleDao, "agentLifeCycleDao");
        this.legacyAgentCompatibility = legacyAgentCompatibility;
        this.mapResponseDao = Objects.requireNonNull(mapResponseDao, "mapResponseDao");
    }

    @Override
    public List<AgentAndStatus> allAgentList(Application application, Range range, AgentInfoFilter agentInfoFilter) {
        final ServiceType serviceType = application.getServiceType();
        final List<String> agentIds = this.applicationIndexDao.selectAgentIds(application.getName());
        final List<AgentInfo> agentInfoList = getNullHandledAgentInfo(application, agentIds, range.getTo());

        List<AgentAndStatus> agentAndStatusList = agentInfoList.stream()
                .filter(serviceType != ServiceType.UNDEFINED ? AgentInfoFilters.exactServiceType(serviceType.getName()) : agentInfo -> true)
                .filter(agentInfoFilter)
                .map(agentInfo -> new AgentAndStatus(agentInfo, null))
                .collect(Collectors.toList());
        return agentAndStatusList;
    }

    @Override
    public List<AgentAndStatus> activeStatusAgentList(Application application, Range range, AgentInfoFilter agentInfoFilter) {
        final ServiceType serviceType = application.getServiceType();
        final List<String> agentIds = this.applicationIndexDao.selectAgentIds(application.getName());
        final List<AgentInfo> agentInfoList = this.agentInfoDao.getSimpleAgentInfos(agentIds, range.getTo());

        List<AgentInfo> filteredAgentInfoList = agentInfoList.stream()
                .filter(Objects::nonNull)
                .filter(serviceType != ServiceType.UNDEFINED ? AgentInfoFilters.exactServiceType(serviceType.getName()) : agentInfo -> true)
                .filter(agentInfoFilter)
                .toList();

        List<AgentAndStatus> agentAndStatusList = getAgentAndStatuses(filteredAgentInfoList, range);

        AgentStatusFilter agentStatusFilter = AgentStatusFilters.recentStatus(range.getFrom());
        List<AgentAndStatus> result = agentAndStatusList.stream()
                .filter(agentAndStatus -> isActiveAgentPredicate(agentAndStatus, agentStatusFilter, range))
                .collect(Collectors.toList());
        return result;
    }

    private List<AgentAndStatus> getAgentAndStatuses(List<AgentInfo> agentInfoList, Range range) {
        List<AgentAndStatus> agentAndStatusList = new ArrayList<>(agentInfoList.size());

        AgentStatusQuery query = AgentStatusQuery.buildQuery(agentInfoList, Instant.ofEpochMilli(range.getTo()));
        List<Optional<AgentStatus>> agentStatus = this.agentLifeCycleDao.getAgentStatus(query);
        for (int i = 0; i < agentStatus.size(); i++) {
            Optional<AgentStatus> status = agentStatus.get(i);
            AgentInfo agentInfo = agentInfoList.get(i);
            agentAndStatusList.add(new AgentAndStatus(agentInfo, status.orElse(null)));
        }
        return agentAndStatusList;
    }

    private boolean isActiveAgentPredicate(AgentAndStatus agentAndStatus, AgentStatusFilter agentStatusFilter, Range range) {
        if (agentStatusFilter.test(agentAndStatus.getStatus())) {
            return true;
        }
        AgentInfo agentInfo = agentAndStatus.getAgentInfo();
        if (legacyAgentCompatibility.isLegacyAgent(agentInfo.getServiceTypeCode(), agentInfo.getAgentVersion())) {
            return legacyAgentCompatibility.isActiveAgent(agentInfo.getAgentId(), range);
        }

        return false;
    }

    @Override
    public List<AgentAndStatus> activeResponseAgentList(Application application, Range range, AgentInfoFilter agentInfoFilter) {
        List<String> agentIds = getActiveAgentIdsFromResponse(application, range);
        List<AgentInfo> agentInfoList = getNullHandledAgentInfo(application, agentIds, range.getTo());

        List<AgentAndStatus> result = agentInfoList.stream()
                .filter(agentInfoFilter)
                .map(agentInfo ->
                        new AgentAndStatus(agentInfo, new AgentStatus(agentInfo.getAgentId(), AgentLifeCycleState.RUNNING, range.getTo())))
                .toList();
        return result;
    }

    private List<String> getActiveAgentIdsFromResponse(Application application, Range range) {
        if (application.getServiceType() == ServiceType.UNDEFINED) {
            return new ArrayList<>(getAgentIdSetFromResponse(application.getName(), range));
        }
        return new ArrayList<>(getAgentIdSetFromResponse(application, range));
    }

    private Set<String> getAgentIdSetFromResponse(String applicationName, Range range) {
        Set<String> result = new HashSet<>();
        List<Application> applications = applicationIndexDao.selectApplicationName(applicationName);
        for (Application application : applications) {
            result.addAll(getAgentIdSetFromResponse(application, range));
        }
        return result;
    }

    private Set<String> getAgentIdSetFromResponse(Application application, Range range) {
        List<ResponseTime> responseTimes = mapResponseDao.selectResponseTime(application, range);
        return responseTimes.stream()
                .map(ResponseTime::getAgentIds)
                .flatMap(Set::stream)
                .collect(Collectors.toSet());
    }

    private List<AgentInfo> getNullHandledAgentInfo(Application application, List<String> agentIds, long toTimestamp) {
        List<AgentInfo> agentInfos = this.agentInfoDao.getSimpleAgentInfos(agentIds, toTimestamp);
        List<AgentInfo> result = new ArrayList<>(agentIds.size());
        for (int i = 0; i < agentIds.size(); i++) {
            result.add(Objects.requireNonNullElse(agentInfos.get(i), createNotFoundAgentInfo(application, agentIds.get(i))));
        }
        return result;
    }

    private AgentInfo createNotFoundAgentInfo(Application application, String agentId) {
        AgentInfo agentInfo = new AgentInfo();
        agentInfo.setApplicationName(application.getName());
        agentInfo.setAgentId(agentId);

        // for Json serialization and serviceType filter
        agentInfo.setServiceType(application.getServiceType());
        // for hostName grouping
        agentInfo.setHostName(NO_AGENT_INFO);
        return agentInfo;
    }
}
