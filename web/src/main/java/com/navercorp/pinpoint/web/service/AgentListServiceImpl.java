package com.navercorp.pinpoint.web.service;

import com.navercorp.pinpoint.common.server.uid.AgentIdentifier;
import com.navercorp.pinpoint.common.server.uid.ApplicationUid;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.common.timeseries.time.Range;
import com.navercorp.pinpoint.web.dao.AgentLifeCycleDao;
import com.navercorp.pinpoint.web.uid.service.AgentNameService;
import com.navercorp.pinpoint.web.vo.agent.AgentListEntry;
import com.navercorp.pinpoint.web.vo.agent.AgentStatus;
import com.navercorp.pinpoint.web.vo.agent.AgentStatusFilter;
import com.navercorp.pinpoint.web.vo.agent.AgentStatusFilters;
import com.navercorp.pinpoint.web.vo.agent.AgentStatusQuery;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class AgentListServiceImpl implements AgentListService {

    private final AgentNameService agentNameService;
    private final AgentLifeCycleDao agentLifeCycleDao;

    public AgentListServiceImpl(AgentNameService agentNameService, AgentLifeCycleDao agentLifeCycleDao) {
        this.agentNameService = Objects.requireNonNull(agentNameService, "uidAgentListService");
        this.agentLifeCycleDao = Objects.requireNonNull(agentLifeCycleDao, "agentLifeCycleDao");
    }

    @Override
    public List<AgentListEntry> getAllAgentList(ServiceUid serviceUid) {
        List<AgentIdentifier> agentList = agentNameService.getAgentIdentifier(serviceUid);
        return createAgentListEntryWithNullStatus(agentList);
    }

    @Override
    public List<AgentListEntry> getAllAgentList(ServiceUid serviceUid, Range range) {
        List<AgentIdentifier> agentList = agentNameService.getAgentIdentifier(serviceUid);
        List<AgentListEntry> agentListEntries = createAgentListEntry(agentList, range);

        AgentStatusFilter activeStatusPredicate = AgentStatusFilters.recentStatus(range.getFrom());
        return agentListEntries.stream()
                .filter(agentAndStatus -> activeStatusPredicate.test(agentAndStatus.getAgentStatus()))
                .toList();
    }

    @Override
    public List<AgentListEntry> getApplicationAgentList(ServiceUid serviceUid, ApplicationUid applicationUid) {
        List<AgentIdentifier> agentList = agentNameService.getAgentIdentifier(serviceUid, applicationUid);
        return createAgentListEntryWithNullStatus(agentList);
    }

    @Override
    public List<AgentListEntry> getApplicationAgentList(ServiceUid serviceUid, ApplicationUid applicationUid, Range range) {
        List<AgentIdentifier> agentList = agentNameService.getAgentIdentifier(serviceUid, applicationUid);
        List<AgentListEntry> agentListEntries = createAgentListEntry(agentList, range);

        AgentStatusFilter activeStatusPredicate = AgentStatusFilters.recentStatus(range.getFrom());
        return agentListEntries.stream()
                .filter(agentAndStatus -> activeStatusPredicate.test(agentAndStatus.getAgentStatus()))
                .toList();
    }

    @Override
    public int cleanupInactiveAgent(ServiceUid serviceUid, ApplicationUid applicationUid, Range range) {
        List<AgentIdentifier> agentList = agentNameService.getAgentIdentifier(serviceUid, applicationUid);
        List<AgentListEntry> agentListEntries = createAgentListEntry(agentList, range);

        AgentStatusFilter activeStatusPredicate = AgentStatusFilters.recentStatus(range.getFrom());
        List<AgentListEntry> inactiveAgents = agentListEntries.stream()
                .filter(agentAndStatus -> !activeStatusPredicate.test(agentAndStatus.getAgentStatus()))
                .toList();
        for (AgentListEntry agentListEntry : inactiveAgents) {
            agentNameService.deleteAgent(serviceUid, applicationUid, agentListEntry.getId());
        }

        return inactiveAgents.size();
    }

    private AgentListEntry createAgentListEntry(AgentIdentifier agentInfo, AgentStatus agentStatus) {
        return new AgentListEntry(agentInfo.getId(), agentInfo.getName(), agentInfo.getStartTimestamp(), agentStatus);
    }

    private List<AgentListEntry> createAgentListEntryWithNullStatus(List<AgentIdentifier> agentList) {
        return agentList.stream()
                .filter(Objects::nonNull)
                .map(agentInfo -> createAgentListEntry(agentInfo, null))
                .toList();
    }

    private List<AgentListEntry> createAgentListEntry(List<AgentIdentifier> agentListEntries, Range range) {
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

    private AgentStatusQuery buildAgentStatusQuery(Collection<AgentIdentifier> agentMetadata, long toTimestamp) {
        AgentStatusQuery.Builder builder = AgentStatusQuery.newBuilder();
        for (AgentIdentifier entry : agentMetadata) {
            if (entry.getStartTimestamp() <= toTimestamp) {
                builder.addAgentKey(entry.getId(), entry.getStartTimestamp());
            }
        }
        return builder.build(toTimestamp);
    }

}
