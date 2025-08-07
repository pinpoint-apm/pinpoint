package com.navercorp.pinpoint.web.service;

import com.navercorp.pinpoint.common.server.uid.ApplicationUid;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.common.timeseries.time.Range;
import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.uid.service.AgentIdService;
import com.navercorp.pinpoint.uid.vo.ApplicationUidRow;
import com.navercorp.pinpoint.web.dao.AgentInfoDao;
import com.navercorp.pinpoint.web.dao.AgentLifeCycleDao;
import com.navercorp.pinpoint.web.uid.service.ApplicationUidService;
import com.navercorp.pinpoint.web.uid.service.ServiceUidCachedService;
import com.navercorp.pinpoint.web.vo.agent.AgentAndStatus;
import com.navercorp.pinpoint.web.vo.agent.AgentInfo;
import com.navercorp.pinpoint.web.vo.agent.AgentStatus;
import com.navercorp.pinpoint.web.vo.agent.AgentStatusFilter;
import com.navercorp.pinpoint.web.vo.agent.AgentStatusFilters;
import com.navercorp.pinpoint.web.vo.agent.AgentStatusQuery;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@ConditionalOnProperty(name = "pinpoint.modules.uid.enabled", havingValue = "true")
public class AgentListServiceImpl implements AgentListService {

    private final ServiceUidCachedService serviceUidCachedService;
    private final ApplicationUidService applicationUidService;
    private final AgentIdService agentIdService;

    private final AgentInfoDao agentInfoDao;
    private final AgentLifeCycleDao agentLifeCycleDao;

    public AgentListServiceImpl(ServiceUidCachedService serviceUidCachedService,
                                ApplicationUidService applicationUidService,
                                AgentIdService agentIdService,
                                AgentInfoDao agentInfoDao,
                                AgentLifeCycleDao agentLifeCycleDao) {
        this.serviceUidCachedService = Objects.requireNonNull(serviceUidCachedService, "serviceUidCachedService");
        this.applicationUidService = Objects.requireNonNull(applicationUidService, "applicationUidService");
        this.agentIdService = Objects.requireNonNull(agentIdService, "uidAgentListService");
        this.agentInfoDao = Objects.requireNonNull(agentInfoDao, "agentInfoDao");
        this.agentLifeCycleDao = Objects.requireNonNull(agentLifeCycleDao, "agentLifeCycleDao");
    }

    @Override
    public List<AgentAndStatus> getApplicationAgentList(String serviceName, String applicationName) {
        long currentTimeMillis = System.currentTimeMillis();
        List<String> agentList = getAgentIds(serviceName, applicationName);
        return getAgentAndStatuses(agentList, currentTimeMillis);
    }

    @Override
    public List<AgentAndStatus> getApplicationAgentList(String serviceName, String applicationName, int serviceTypeCode) {
        long currentTimeMillis = System.currentTimeMillis();
        List<String> agentList = getAgentIds(serviceName, applicationName, serviceTypeCode);
        return getAgentAndStatuses(agentList, currentTimeMillis);
    }

    @Override
    public List<AgentAndStatus> getApplicationAgentList(String serviceName, String applicationName, Range range) {
        List<AgentAndStatus> agentStatusList = getApplicationAgentList(serviceName, applicationName);
        return filterActiveStatus(agentStatusList, range);
    }

    @Override
    public List<AgentAndStatus> getApplicationAgentList(String serviceName, String applicationName, int serviceTypeCode, Range range) {
        List<AgentAndStatus> agentStatusList = getApplicationAgentList(serviceName, applicationName, serviceTypeCode);
        return filterActiveStatus(agentStatusList, range);
    }

    private List<AgentAndStatus> filterActiveStatus(List<AgentAndStatus> agentListEntries, Range range) {
        AgentStatusFilter activeStatusPredicate = AgentStatusFilters.recentStatus(range.getFrom());
        return agentListEntries.stream()
                .filter(agentAndStatus -> activeStatusPredicate.test(agentAndStatus.getStatus()))
                .toList();
    }

    @Override
    public int cleanupInactiveAgent(String serviceName, String applicationName, int serviceTypeCode, Range range) {
        Objects.requireNonNull(applicationName, "applicationName");
        ServiceUid serviceUid = handleServiceUid(serviceName);
        ApplicationUid applicationUid = applicationUidService.getApplicationUid(serviceUid, applicationName, serviceTypeCode);
        if (applicationUid == null) {
            return 0;
        }
        List<String> agentIdList = getAgentIds(serviceName, applicationName, serviceTypeCode);
        List<AgentAndStatus> agentAndStatusList = getAgentAndStatuses(agentIdList, range.getTo());

        return deleteInactiveAgent(range, serviceUid, applicationUid, agentIdList, agentAndStatusList);
    }

    private int deleteInactiveAgent(Range range, ServiceUid serviceUid, ApplicationUid applicationUid, List<String> agentIdList, List<AgentAndStatus> agentAndStatusList) {
        AgentStatusFilter activeStatusPredicate = AgentStatusFilters.recentStatus(range.getFrom());
        int deletedCount = 0;
        for (int i = 0; i < agentIdList.size(); i++) {
            AgentAndStatus agentAndStatus = agentAndStatusList.get(i);
            if (agentAndStatus == null || !activeStatusPredicate.test(agentAndStatus.getStatus())) {
                agentIdService.deleteAgent(serviceUid, applicationUid, agentIdList.get(i));
                deletedCount++;
            }
        }
        return deletedCount;
    }

    private ServiceUid handleServiceUid(String serviceName) {
        if (StringUtils.isEmpty(serviceName)) {
            return ServiceUid.DEFAULT;
        }
        return serviceUidCachedService.getServiceUid(serviceName);
    }

    private List<String> getAgentIds(String serviceName, String applicationName) {
        Objects.requireNonNull(applicationName, "applicationName");
        ServiceUid serviceUid = handleServiceUid(serviceName);
        List<ApplicationUid> applicationUidList = applicationUidService.getApplications(serviceUid, applicationName).stream()
                .map(ApplicationUidRow::applicationUid)
                .toList();
        return agentIdService.getAgentId(serviceUid, applicationUidList).stream()
                .flatMap(List::stream)
                .distinct()
                .toList();
    }

    private List<String> getAgentIds(String serviceName, String applicationName, int serviceTypeCode) {
        Objects.requireNonNull(applicationName, "applicationName");
        ServiceUid serviceUid = handleServiceUid(serviceName);
        ApplicationUid applicationUid = applicationUidService.getApplicationUid(serviceUid, applicationName, serviceTypeCode);
        if (applicationUid == null) {
            return Collections.emptyList();
        }
        return agentIdService.getAgentId(serviceUid, applicationUid);
    }

    private List<AgentAndStatus> getAgentAndStatuses(List<String> agentList, long currentTimeMillis) {
        List<AgentInfo> agentInfoList = getAgentInfoList(agentList, currentTimeMillis);
        return addStatus(agentInfoList, currentTimeMillis);
    }

    private List<AgentInfo> getAgentInfoList(List<String> agentIds, long toTimestamp) {
        if (agentIds.isEmpty()) {
            return Collections.emptyList();
        }
        return this.agentInfoDao.getSimpleAgentInfos(agentIds, toTimestamp);
    }

    private List<AgentAndStatus> addStatus(List<AgentInfo> agentInfoList, long toTimestamp) {
        List<AgentAndStatus> agentAndStatusList = new ArrayList<>(agentInfoList.size());
        AgentStatusQuery query = AgentStatusQuery.buildQuery(agentInfoList, toTimestamp);
        List<Optional<AgentStatus>> agentStatus = this.agentLifeCycleDao.getAgentStatus(query);
        for (int i = 0; i < agentStatus.size(); i++) {
            if (agentInfoList.get(i) == null || agentStatus.get(i).isEmpty()) {
                agentAndStatusList.add(null);
            } else {
                AgentAndStatus agentAndStatus = new AgentAndStatus(agentInfoList.get(i), agentStatus.get(i).orElse(null));
                agentAndStatusList.add(agentAndStatus);
            }
        }
        return agentAndStatusList;
    }
}
