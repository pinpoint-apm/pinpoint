package com.navercorp.pinpoint.web.service;

import com.navercorp.pinpoint.common.server.util.AgentLifeCycleState;
import com.navercorp.pinpoint.common.timeseries.time.Range;
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
import com.navercorp.pinpoint.web.vo.agent.AgentInfoFilters;
import com.navercorp.pinpoint.web.vo.agent.AgentStatus;
import com.navercorp.pinpoint.web.vo.agent.AgentStatusFilter;
import com.navercorp.pinpoint.web.vo.agent.AgentStatusFilters;
import com.navercorp.pinpoint.web.vo.agent.AgentStatusQuery;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;


@Service
public class ApplicationAgentListServiceImpl implements ApplicationAgentListService {
    private final Logger logger = LogManager.getLogger(this.getClass());

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

    boolean isValidServiceType(ServiceType serviceType) {
        return serviceType != null && serviceType != ServiceType.UNDEFINED;
    }

    Predicate<AgentInfo> getServiceTypeFilter(ServiceType serviceType) {
        if (isValidServiceType(serviceType)) {
            return AgentInfoFilters.exactServiceType(serviceType.getName());
        }
        return AgentInfoFilters.acceptAll();
    }

    @Override
    public List<AgentAndStatus> allAgentList(String applicationName, ServiceType serviceType, Range range, Predicate<AgentInfo> agentInfoPredicate) {
        final List<String> agentIds = this.applicationIndexDao.selectAgentIds(applicationName);
        final List<AgentInfo> agentInfoList = getNullHandledAgentInfo(applicationName, serviceType, agentIds, range.getTo());
        final Predicate<AgentInfo> agentServiceTypeFilter = getServiceTypeFilter(serviceType);

        List<AgentAndStatus> agentAndStatusList = agentInfoList.stream()
                .filter(agentServiceTypeFilter)
                .filter(agentInfoPredicate)
                .map(agentInfo -> new AgentAndStatus(agentInfo, null))
                .collect(Collectors.toList());
        return agentAndStatusList;
    }

    @Override
    public List<AgentAndStatus> activeStatusAgentList(String applicationName, ServiceType serviceType, Range range, Predicate<AgentInfo> agentInfoPredicate) {
        final List<String> agentIds = this.applicationIndexDao.selectAgentIds(applicationName);
        final List<AgentInfo> agentInfoList = getNullHandledAgentInfo(applicationName, serviceType, agentIds, range.getTo());
        final Predicate<AgentInfo> agentServiceTypeFilter = getServiceTypeFilter(serviceType);

        List<AgentInfo> filteredAgentInfoList = agentInfoList.stream()
                .filter(ACTUAL_AGENT_INFO_PREDICATE) // filter out not actual agent info
                .filter(agentServiceTypeFilter)
                .filter(agentInfoPredicate)
                .collect(Collectors.toList());

        List<AgentAndStatus> agentAndStatusList = getAgentAndStatuses(filteredAgentInfoList, range);

        AgentStatusFilter agentStatusFilter = AgentStatusFilters.recentStatus(range.getFrom());
        final List<AgentAndStatus> result = agentAndStatusList.stream()
                .filter(agentAndStatus -> isActiveAgentPredicate(agentAndStatus, agentStatusFilter, range))
                .collect(Collectors.toList());
        return result;
    }

    private List<AgentAndStatus> getAgentAndStatuses(List<AgentInfo> agentInfoList, Range range) {
        List<AgentAndStatus> agentAndStatusList = new ArrayList<>(agentInfoList.size());

        AgentStatusQuery query = AgentStatusQuery.buildQuery(agentInfoList, range.getTo());
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

        return false;
    }

    @Override
    public List<AgentAndStatus> activeStatisticsAgentList(String applicationName, ServiceType serviceType, Range range, Predicate<AgentInfo> agentInfoPredicate) {
        final List<String> agentIds = getActiveAgentIdsFromStatistics(applicationName, serviceType, range);
        final List<AgentInfo> agentInfoList = getNullHandledAgentInfo(applicationName, serviceType, agentIds, range.getTo());

        List<AgentAndStatus> result = agentInfoList.stream()
                .filter(agentInfoPredicate)
                .map(agentInfo ->
                        new AgentAndStatus(agentInfo, new AgentStatus(agentInfo.getAgentId(), AgentLifeCycleState.RUNNING, range.getTo())))
                .collect(Collectors.toList());
        return result;
    }

    private List<String> getActiveAgentIdsFromStatistics(String applicationName, ServiceType serviceType, Range range) {
        if (isValidServiceType(serviceType)) {
            return new ArrayList<>(getAgentIdsFromStatistics(new Application(applicationName, serviceType), range));
        }

        // find all serviceType with applicationName
        Set<String> result = new HashSet<>();
        List<Application> applications = applicationIndexDao.selectApplicationName(applicationName);
        for (Application application : applications) {
            result.addAll(getAgentIdsFromStatistics(application, range));
        }
        return new ArrayList<>(result);
    }

    private Set<String> getAgentIdsFromStatistics(Application application, Range range) {
        List<ResponseTime> responseTimes = mapResponseDao.selectResponseTime(application, range);
        return responseTimes.stream()
                .map(ResponseTime::getAgentIds)
                .flatMap(Set::stream)
                .collect(Collectors.toSet());
    }

    private List<AgentInfo> getNullHandledAgentInfo(String applicationName, ServiceType serviceType, List<String> agentIds, long toTimestamp) {
        final List<AgentInfo> agentInfos = this.agentInfoDao.getSimpleAgentInfos(agentIds, toTimestamp);
        List<AgentInfo> result = new ArrayList<>(agentIds.size());
        for (int i = 0; i < agentIds.size(); i++) {
            result.add(Objects.requireNonNullElse(agentInfos.get(i), createNotFoundAgentInfo(applicationName, serviceType, agentIds.get(i))));
        }
        return result;
    }

    private AgentInfo createNotFoundAgentInfo(String applicationName, ServiceType serviceType, String agentId) {
        AgentInfo agentInfo = new AgentInfo();
        agentInfo.setApplicationName(applicationName);
        agentInfo.setAgentId(agentId);

        // for Json serialization
        if (serviceType == null) {
            agentInfo.setServiceType(ServiceType.UNDEFINED);
        } else {
            agentInfo.setServiceType(serviceType);
        }
        // for hostName grouping
        agentInfo.setHostName(AGENT_INFO_NOT_FOUND_HOSTNAME);
        return agentInfo;
    }

    @Override
    public List<AgentAndStatus> activeAllAgentList(String applicationName, ServiceType serviceType, Range range, Predicate<AgentInfo> agentInfoPredicate) {
        final List<AgentAndStatus> activeStatusAgentList = activeStatusAgentList(applicationName, serviceType, range, agentInfoPredicate);
        final List<AgentAndStatus> activeResponseAgentList = activeStatisticsAgentList(applicationName, serviceType, range, agentInfoPredicate);

        Set<AgentAndStatus> result = new HashSet<>();
        result.addAll(activeStatusAgentList);
        result.addAll(activeResponseAgentList);

        if (result.size() != activeStatusAgentList.size() || result.size() != activeResponseAgentList.size()) {
            logger.info("active agent check result is different. applicationName:{}, serviceType:{}, activeStatusAgentList:{}, activeResponseAgentList:{}, result:{}",
                    applicationName, serviceType, activeStatusAgentList.size(), activeResponseAgentList.size(), result.size());
        }
        return new ArrayList<>(result);
    }
}
