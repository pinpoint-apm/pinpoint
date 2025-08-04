package com.navercorp.pinpoint.web.service;

import com.navercorp.pinpoint.common.server.uid.AgentIdentifier;
import com.navercorp.pinpoint.common.server.uid.ApplicationUid;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.common.timeseries.time.Range;
import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.uid.service.AgentNameService;
import com.navercorp.pinpoint.uid.vo.ApplicationUidRow;
import com.navercorp.pinpoint.web.dao.AgentLifeCycleDao;
import com.navercorp.pinpoint.web.uid.service.ApplicationUidService;
import com.navercorp.pinpoint.web.uid.service.ServiceUidCachedService;
import com.navercorp.pinpoint.web.vo.agent.AgentListEntry;
import com.navercorp.pinpoint.web.vo.agent.AgentStatus;
import com.navercorp.pinpoint.web.vo.agent.AgentStatusFilter;
import com.navercorp.pinpoint.web.vo.agent.AgentStatusFilters;
import com.navercorp.pinpoint.web.vo.agent.AgentStatusQuery;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@ConditionalOnProperty(name = "pinpoint.modules.uid.enabled", havingValue = "true")
public class AgentListServiceImpl implements AgentListService {

    private final ServiceUidCachedService serviceUidCachedService;
    private final ApplicationUidService applicationUidService;
    private final AgentNameService agentNameService;

    private final AgentLifeCycleDao agentLifeCycleDao;

    public AgentListServiceImpl(ServiceUidCachedService serviceUidCachedService, ApplicationUidService applicationUidService,
                                AgentNameService agentNameService, AgentLifeCycleDao agentLifeCycleDao) {
        this.serviceUidCachedService = Objects.requireNonNull(serviceUidCachedService, "serviceUidCachedService");
        this.applicationUidService = Objects.requireNonNull(applicationUidService, "applicationUidService");
        this.agentNameService = Objects.requireNonNull(agentNameService, "uidAgentListService");
        this.agentLifeCycleDao = Objects.requireNonNull(agentLifeCycleDao, "agentLifeCycleDao");
    }

    @Override
    public List<AgentListEntry> getApplicationAgentList(String serviceName, String applicationName) {
        List<AgentIdentifier> agentList = getAgentIdentifiers(serviceName, applicationName);
        return createAgentListWithNullStatus(agentList);
    }


    @Override
    public List<AgentListEntry> getApplicationAgentList(String serviceName, String applicationName, int serviceTypeCode) {
        List<AgentIdentifier> agentList = getAgentIdentifiers(serviceName, applicationName, serviceTypeCode);
        return createAgentListWithNullStatus(agentList);
    }

    @Override
    public List<AgentListEntry> getApplicationAgentList(String serviceName, String applicationName, Range range) {
        List<AgentIdentifier> agentList = getAgentIdentifiers(serviceName, applicationName);
        List<AgentIdentifier> filteredAgentList = filterStartTimeAndDuplicatedAgentId(agentList, range);
        List<AgentListEntry> agentListEntries = createAgentListWithStatus(filteredAgentList, range);
        return filterActiveStatus(range, agentListEntries);
    }

    @Override
    public List<AgentListEntry> getApplicationAgentList(String serviceName, String applicationName, int serviceTypeCode, Range range) {
        List<AgentIdentifier> agentList = getAgentIdentifiers(serviceName, applicationName, serviceTypeCode);
        List<AgentIdentifier> filteredAgentList = filterStartTimeAndDuplicatedAgentId(agentList, range);
        List<AgentListEntry> agentListEntries = createAgentListWithStatus(filteredAgentList, range);
        return filterActiveStatus(range, agentListEntries);
    }

    private List<AgentIdentifier> getAgentIdentifiers(String serviceName, String applicationName) {
        Objects.requireNonNull(applicationName, "applicationName");
        ServiceUid serviceUid = getServiceUid(serviceName);
        List<ApplicationUid> applicationUidList = applicationUidService.getApplications(serviceUid, applicationName).stream()
                .map(ApplicationUidRow::applicationUid)
                .toList();
        return agentNameService.getAgentIdentifier(serviceUid, applicationUidList).stream()
                .flatMap(List::stream)
                .toList();
    }

    private List<AgentIdentifier> getAgentIdentifiers(String serviceName, String applicationName, int serviceTypeCode) {
        Objects.requireNonNull(applicationName, "applicationName");
        ServiceUid serviceUid = getServiceUid(serviceName);
        ApplicationUid applicationUid = applicationUidService.getApplicationUid(serviceUid, applicationName, serviceTypeCode);
        if (applicationUid != null) {
            return agentNameService.getAgentIdentifier(serviceUid, applicationUid);
        }
        return Collections.emptyList();
    }


    private List<AgentIdentifier> filterStartTimeAndDuplicatedAgentId(Collection<AgentIdentifier> agentIdentifiers, Range range) {
        Collection<AgentIdentifier> filteredResult = agentIdentifiers.stream()
                .filter(agentIdentifier -> agentIdentifier.getStartTimestamp() < range.getTo())
                .collect(Collectors.toMap(
                        AgentIdentifier::getId,
                        Function.identity(),
                        (r1, r2) -> r1.getStartTimestamp() >= r2.getStartTimestamp() ? r1 : r2 // keep latest
                )).values();
        return new ArrayList<>(filteredResult);
    }

    @Override
    public int cleanupInactiveAgent(String serviceName, String applicationName, int serviceTypeCode, Range range) {
        Objects.requireNonNull(applicationName, "applicationName");
        ServiceUid serviceUid = getServiceUid(serviceName);
        ApplicationUid applicationUid = applicationUidService.getApplicationUid(serviceUid, applicationName, serviceTypeCode);
        if (applicationUid == null) {
            return 0;
        }

        List<AgentIdentifier> agentList = agentNameService.getAgentIdentifier(serviceUid, applicationUid);
        List<AgentListEntry> agentListEntries = createAgentListWithStatus(agentList, range);

        AgentStatusFilter activeStatusPredicate = AgentStatusFilters.recentStatus(range.getFrom());
        List<AgentListEntry> inactiveAgents = agentListEntries.stream()
                .filter(agentAndStatus -> !activeStatusPredicate.test(agentAndStatus.getAgentStatus()))
                .toList();
        for (AgentListEntry agentListEntry : inactiveAgents) {
            agentNameService.deleteAgent(serviceUid, applicationUid, agentListEntry.getId());
        }

        return inactiveAgents.size();
    }

    private List<AgentListEntry> createAgentListWithNullStatus(List<AgentIdentifier> agentList) {
        return agentList.stream()
                .filter(Objects::nonNull)
                .map(agentInfo -> createAgentListEntry(agentInfo, null))
                .toList();
    }

    private List<AgentListEntry> createAgentListWithStatus(List<AgentIdentifier> agentListEntries, Range range) {
        List<AgentListEntry> result = new ArrayList<>(agentListEntries.size());

        AgentStatusQuery query = buildAgentStatusQuery(agentListEntries, range.getTo());
        List<Optional<AgentStatus>> agentStatus = this.agentLifeCycleDao.getAgentStatus(query);
        for (int i = 0; i < agentStatus.size(); i++) {
            Optional<AgentStatus> status = agentStatus.get(i);
            AgentIdentifier agentInfo = agentListEntries.get(i);
            result.add(createAgentListEntry(agentInfo, status.orElse(null)));
        }
        return result;
    }

    private List<AgentListEntry> filterActiveStatus(Range range, List<AgentListEntry> agentListEntries) {
        AgentStatusFilter activeStatusPredicate = AgentStatusFilters.recentStatus(range.getFrom());
        return agentListEntries.stream()
                .filter(agentAndStatus -> activeStatusPredicate.test(agentAndStatus.getAgentStatus()))
                .toList();
    }

    private AgentStatusQuery buildAgentStatusQuery(Collection<AgentIdentifier> agentMetadata, long toTimestamp) {
        AgentStatusQuery.Builder builder = AgentStatusQuery.newBuilder();
        for (AgentIdentifier entry : agentMetadata) {
            if (entry.getStartTimestamp() <= toTimestamp) {
                builder.addAgentKey(entry.getId(), entry.getStartTimestamp());
            }
        }
        return builder.build(toTimestamp);
    }

    private AgentListEntry createAgentListEntry(AgentIdentifier agentInfo, AgentStatus agentStatus) {
        return new AgentListEntry(agentInfo.getId(), agentInfo.getName(), agentInfo.getStartTimestamp(), agentStatus);
    }

    private ServiceUid getServiceUid(String serviceName) {
        if (StringUtils.isEmpty(serviceName)) {
            return ServiceUid.DEFAULT;
        }
        return serviceUidCachedService.getServiceUid(serviceName);
    }
}
