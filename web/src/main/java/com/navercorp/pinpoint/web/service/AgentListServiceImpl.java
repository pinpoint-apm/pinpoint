package com.navercorp.pinpoint.web.service;

import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.common.timeseries.time.Range;
import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.web.dao.AgentIdDao;
import com.navercorp.pinpoint.web.dao.AgentInfoDao;
import com.navercorp.pinpoint.web.dao.AgentLifeCycleDao;
import com.navercorp.pinpoint.web.uid.service.ServiceUidService;
import com.navercorp.pinpoint.web.vo.agent.AgentAndStatus;
import com.navercorp.pinpoint.web.vo.agent.AgentInfo;
import com.navercorp.pinpoint.web.vo.agent.AgentStatus;
import com.navercorp.pinpoint.web.vo.agent.AgentStatusFilter;
import com.navercorp.pinpoint.web.vo.agent.AgentStatusFilters;
import com.navercorp.pinpoint.web.vo.agent.AgentStatusQuery;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class AgentListServiceImpl implements AgentListService {
    private final ServiceUidService serviceUidService;
    private final AgentIdDao agentIdDao;

    private final AgentInfoDao agentInfoDao;
    private final AgentLifeCycleDao agentLifeCycleDao;

    public AgentListServiceImpl(ServiceUidService serviceUidService,
                                AgentIdDao agentIdDao,
                                AgentInfoDao agentInfoDao,
                                AgentLifeCycleDao agentLifeCycleDao) {
        this.serviceUidService = Objects.requireNonNull(serviceUidService, "serviceUidService");
        this.agentIdDao = Objects.requireNonNull(agentIdDao, "agentIdDao");
        this.agentInfoDao = Objects.requireNonNull(agentInfoDao, "agentInfoDao");
        this.agentLifeCycleDao = Objects.requireNonNull(agentLifeCycleDao, "agentLifeCycleDao");
    }

    @Override
    public List<AgentAndStatus> getApplicationAgentList(String serviceName, String applicationName) {
        long currentTimeMillis = System.currentTimeMillis();
        ServiceUid serviceUid = handleServiceUid(serviceName);
        List<String> agentList = agentIdDao.getAgentIds(serviceUid, applicationName);
        return getAgentAndStatuses(agentList, currentTimeMillis);
    }

    @Override
    public List<AgentAndStatus> getApplicationAgentList(String serviceName, String applicationName, int serviceTypeCode) {
        long currentTimeMillis = System.currentTimeMillis();
        ServiceUid serviceUid = handleServiceUid(serviceName);
        List<String> agentList = agentIdDao.getAgentIds(serviceUid, applicationName, serviceTypeCode);
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

    public void deleteAllAgents(String serviceName, String applicationName, int serviceTypeCode) {
        ServiceUid serviceUid = handleServiceUid(serviceName);
        agentIdDao.deleteAllAgents(serviceUid, applicationName, serviceTypeCode);
    }

    public void deleteAgents(String serviceName, String applicationName, int serviceTypeCode, List<String> agentIdList) {
        ServiceUid serviceUid = handleServiceUid(serviceName);
        agentIdDao.deleteAgents(serviceUid, applicationName, serviceTypeCode, agentIdList);
    }

    public int cleanupInactiveAgent(String serviceName, String applicationName, int serviceTypeCode, Range range) {
        Objects.requireNonNull(applicationName, "applicationName");
        ServiceUid serviceUid = handleServiceUid(serviceName);
        List<String> agentIdList = agentIdDao.getAgentIds(serviceUid, applicationName, serviceTypeCode);
        List<AgentAndStatus> agentAndStatusList = getAgentAndStatuses(agentIdList, range.getTo());

        return deleteInactiveAgent(range, serviceUid, applicationName, serviceTypeCode, agentIdList, agentAndStatusList);
    }

    private int deleteInactiveAgent(Range range, ServiceUid serviceUid, String applicationName, int serviceTypeCode, List<String> agentIdList, List<AgentAndStatus> agentAndStatusList) {
        AgentStatusFilter activeStatusPredicate = AgentStatusFilters.recentStatus(range.getFrom());
        List<String> agentIdsToDelete = new ArrayList<>();
        for (int i = 0; i < agentIdList.size(); i++) {
            AgentAndStatus agentAndStatus = agentAndStatusList.get(i);
            if (agentAndStatus == null || !activeStatusPredicate.test(agentAndStatus.getStatus())) {
                agentIdsToDelete.add(agentIdList.get(i));
            }
        }
        agentIdDao.deleteAgents(serviceUid, applicationName, serviceTypeCode, agentIdsToDelete);
        return agentIdsToDelete.size();
    }

    private ServiceUid handleServiceUid(String serviceName) {
        if (StringUtils.isEmpty(serviceName)) {
            return ServiceUid.DEFAULT;
        }
        return serviceUidService.getServiceUid(serviceName);
    }

    private List<AgentAndStatus> getAgentAndStatuses(List<String> agentList, long currentTimeMillis) {
        if (agentList.isEmpty()) {
            return Collections.emptyList();
        }

        List<AgentInfo> agentInfoList = agentInfoDao.getSimpleAgentInfos(agentList, currentTimeMillis);
        return addStatus(agentInfoList, currentTimeMillis);
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
