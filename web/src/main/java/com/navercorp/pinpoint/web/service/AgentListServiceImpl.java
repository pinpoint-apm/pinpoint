package com.navercorp.pinpoint.web.service;

import com.navercorp.pinpoint.common.server.uid.ApplicationUid;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.common.server.util.time.Range;
import com.navercorp.pinpoint.web.dao.AgentLifeCycleDao;
import com.navercorp.pinpoint.web.dao.AgentListDao;
import com.navercorp.pinpoint.web.vo.agent.AgentListEntry;
import com.navercorp.pinpoint.web.vo.agent.AgentListEntryAndStatus;
import com.navercorp.pinpoint.web.vo.agent.AgentStatus;
import com.navercorp.pinpoint.web.vo.agent.AgentStatusFilter;
import com.navercorp.pinpoint.web.vo.agent.AgentStatusFilters;
import com.navercorp.pinpoint.web.vo.agent.AgentStatusQuery;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@ConditionalOnProperty(name = "pinpoint.web.application.uid.enable", havingValue = "true")
public class AgentListServiceImpl implements AgentListService {

    private final AgentListDao agentListDao;
    private final AgentLifeCycleDao agentLifeCycleDao;

    public AgentListServiceImpl(AgentListDao agentListDao, AgentLifeCycleDao agentLifeCycleDao) {
        this.agentListDao = Objects.requireNonNull(agentListDao, "agentListDao");
        this.agentLifeCycleDao = Objects.requireNonNull(agentLifeCycleDao, "agentLifeCycleDao");
    }

    @Override
    public List<AgentListEntryAndStatus> getAgentList(ServiceUid serviceUid, ApplicationUid applicationUid) {
        Objects.requireNonNull(serviceUid, "serviceUid");
        Objects.requireNonNull(applicationUid, "applicationUid");

        List<AgentListEntry> agentListEntries = agentListDao.selectAgentListEntry(serviceUid, applicationUid);

        return agentListEntries.stream()
                .map(agentInfo -> new AgentListEntryAndStatus(agentInfo, null))
                .toList();
    }

    @Override
    public List<AgentListEntryAndStatus> getActiveAgentList(ServiceUid serviceUid, ApplicationUid applicationUid, Range range) {
        Objects.requireNonNull(serviceUid, "serviceUid");
        Objects.requireNonNull(applicationUid, "applicationUid");

        List<AgentListEntry> agentListEntries = agentListDao.selectAgentListEntry(serviceUid, applicationUid);
        List<AgentListEntryAndStatus> agentListEntryAndStatuses = getAgentListEntryAndStatus(agentListEntries, range);

        AgentStatusFilter agentStatusFilter = AgentStatusFilters.recentStatus(range.getFrom());
        return agentListEntryAndStatuses.stream()
                .filter(agentAndStatus -> agentStatusFilter.test(agentAndStatus.getStatus()))
                .collect(Collectors.toList());
    }

    private List<AgentListEntryAndStatus> getAgentListEntryAndStatus(List<AgentListEntry> agentListEntries, Range range) {
        List<AgentListEntryAndStatus> result = new ArrayList<>(agentListEntries.size());

        AgentStatusQuery query = buildAgentStatusQuery(agentListEntries, range.getTo());
        List<Optional<AgentStatus>> agentStatus = this.agentLifeCycleDao.getAgentStatus(query);
        for (int i = 0; i < agentStatus.size(); i++) {
            Optional<AgentStatus> status = agentStatus.get(i);
            AgentListEntry agentInfo = agentListEntries.get(i);
            result.add(new AgentListEntryAndStatus(agentInfo, status.orElse(null)));
        }
        return result;
    }

    private AgentStatusQuery buildAgentStatusQuery(Collection<AgentListEntry> agentListEntry, long toTimestamp) {
        AgentStatusQuery.Builder builder = AgentStatusQuery.newBuilder();
        for (AgentListEntry entry : agentListEntry) {
            if (entry.getStartTimestamp() <= toTimestamp) {
                builder.addAgentKey(entry.getAgentId(), entry.getStartTimestamp());
            }
        }
        return builder.build(Instant.ofEpochMilli(toTimestamp));
    }

    @Override
    public void deleteAgents(ServiceUid serviceUid, ApplicationUid applicationUid) {
        Objects.requireNonNull(serviceUid, "serviceUid");
        Objects.requireNonNull(applicationUid, "applicationUid");

        List<AgentListEntry> agentListEntries = agentListDao.selectAgentListEntry(serviceUid, applicationUid);
        deleteAgents(agentListEntries, serviceUid, applicationUid);
    }

    @Override
    public void deleteAgent(ServiceUid serviceUid, ApplicationUid applicationUid, String agentId) {
        Objects.requireNonNull(serviceUid, "serviceUid");
        Objects.requireNonNull(applicationUid, "applicationUid");

        List<AgentListEntry> agentListEntries = agentListDao.selectAgentListEntry(serviceUid, applicationUid, agentId);
        deleteAgents(agentListEntries, serviceUid, applicationUid);
    }

    private void deleteAgents(List<AgentListEntry> agentListEntries, ServiceUid serviceUid, ApplicationUid applicationUid) {
        if (agentListEntries.isEmpty()) {
            return;
        }
        agentListDao.deleteAgents(serviceUid, applicationUid, agentListEntries);
    }
}
