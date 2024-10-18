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
import com.navercorp.pinpoint.web.vo.agent.AgentStatus;
import com.navercorp.pinpoint.web.vo.agent.AgentStatusFilter;
import com.navercorp.pinpoint.web.vo.agent.AgentStatusFilters;
import com.navercorp.pinpoint.web.vo.agent.AgentStatusQuery;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
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
    public List<AgentAndStatus> allAgentList(String applicationName, Range range, AgentInfoFilter agentInfoFilter) {
        List<String> agentIds = this.applicationIndexDao.selectAgentIds(applicationName);
        List<AgentInfo> agentInfoList = getNullHandledAgentInfo(agentIds, range.getTo());

        List<AgentAndStatus> agentAndStatusList = agentInfoList.stream()
                .filter(agentInfoFilter)
                .map(agentInfo -> new AgentAndStatus(agentInfo, null))
                .collect(Collectors.toList());
        return agentAndStatusList;
    }

    @Override
    public List<AgentAndStatus> activeStatusAgentList(String applicationName, Range range, AgentInfoFilter agentInfoFilter) {
        List<String> agentIds = this.applicationIndexDao.selectAgentIds(applicationName);
        List<AgentInfo> agentInfoList = this.agentInfoDao.getSimpleAgentInfos(agentIds, range.getTo());

        List<AgentInfo> filteredAgentInfoList = agentInfoList.stream()
                .filter(Objects::nonNull)
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
    public List<AgentAndStatus> activeResponseAgentList(String applicationName, Range range, AgentInfoFilter agentInfoFilter) {
        List<AgentAndStatus> result = new ArrayList<>();
        List<Application> applications = applicationIndexDao.selectApplicationName(applicationName);
        for (Application application : applications) {
            result.addAll(activeResponseAgentList(application, range, agentInfoFilter));
        }
        return result;
    }

    @Override
    public List<AgentAndStatus> activeResponseAgentList(Application application, Range range, AgentInfoFilter agentInfoFilter) {
        List<ResponseTime> responseTimes = mapResponseDao.selectResponseTime(application, range);

        List<String> agentIds = responseTimes.stream()
                .map(ResponseTime::getAgentIds)
                .flatMap(Set::stream)
                .distinct()
                .toList();
        List<AgentInfo> agentInfoList = getNullHandledAgentInfo(agentIds, range.getTo());

        List<AgentAndStatus> result = agentInfoList.stream()
                .filter(agentInfoFilter)
                .map(agentInfo ->
                        new AgentAndStatus(agentInfo, new AgentStatus(agentInfo.getAgentId(), AgentLifeCycleState.RUNNING, range.getTo())))
                .toList();
        return result;
    }

    private List<AgentInfo> getNullHandledAgentInfo(List<String> agentIds, long toTimestamp) {
        List<AgentInfo> agentInfos = this.agentInfoDao.getSimpleAgentInfos(agentIds, toTimestamp);
        List<AgentInfo> result = new ArrayList<>(agentIds.size());
        for (int i = 0; i < agentIds.size(); i++) {
            result.add(Objects.requireNonNullElse(agentInfos.get(i), createNotFoundAgentInfo(agentIds.get(i))));
        }
        return result;
    }

    private AgentInfo createNotFoundAgentInfo(String agentId) {
        AgentInfo agentInfo = new AgentInfo();
        agentInfo.setAgentId(agentId);
        // for hostName grouping
        agentInfo.setHostName(NO_AGENT_INFO);
        // for Json serialization
        agentInfo.setServiceType(ServiceType.UNKNOWN_GROUP);
        return agentInfo;
    }
}
