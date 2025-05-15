package com.navercorp.pinpoint.web.uid.service;

import com.navercorp.pinpoint.common.server.uid.AgentIdentifier;
import com.navercorp.pinpoint.common.server.uid.ApplicationUid;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.web.uid.dao.AgentNameDao;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@ConditionalOnProperty(name = "pinpoint.web.application.uid.enable", havingValue = "true")
public class AgentNameServiceImpl implements AgentNameService {

    private final AgentNameDao agentNameDao;

    public AgentNameServiceImpl(AgentNameDao agentNameDao) {
        this.agentNameDao = Objects.requireNonNull(agentNameDao, "agentListDao");
    }

    @Override
    public List<AgentIdentifier> getAgentIdentifier(ServiceUid serviceUid) {
        Objects.requireNonNull(serviceUid, "serviceUid");

        return agentNameDao.selectAgentIdentifiers(serviceUid).stream()
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public List<AgentIdentifier> getAgentIdentifier(ServiceUid serviceUid, ApplicationUid applicationUid) {
        Objects.requireNonNull(serviceUid, "serviceUid");
        Objects.requireNonNull(applicationUid, "applicationUid");

        return agentNameDao.selectAgentIdentifiers(serviceUid, applicationUid).stream()
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public List<AgentIdentifier> getAgentIdentifier(ServiceUid serviceUid, ApplicationUid applicationUid, String agentId) {
        Objects.requireNonNull(serviceUid, "serviceUid");
        Objects.requireNonNull(applicationUid, "applicationUid");
        Objects.requireNonNull(agentId, "agentId");

        return agentNameDao.selectAgentIdentifiers(serviceUid, applicationUid, agentId).stream()
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public void deleteAllAgents(ServiceUid serviceUid, ApplicationUid applicationUid) {
        List<AgentIdentifier> agentListEntries = this.getAgentIdentifier(serviceUid, applicationUid);
        deleteAgents(agentListEntries, serviceUid, applicationUid);
    }

    @Override
    public void deleteAgent(ServiceUid serviceUid, ApplicationUid applicationUid, String agentId) {
        Objects.requireNonNull(serviceUid, "serviceUid");
        Objects.requireNonNull(applicationUid, "applicationUid");

        List<AgentIdentifier> agentListEntries = agentNameDao.selectAgentIdentifiers(serviceUid, applicationUid, agentId);
        deleteAgents(agentListEntries, serviceUid, applicationUid);
    }

    private void deleteAgents(List<AgentIdentifier> agentListEntries, ServiceUid serviceUid, ApplicationUid applicationUid) {
        if (agentListEntries.isEmpty()) {
            return;
        }
        agentNameDao.deleteAgents(serviceUid, applicationUid, agentListEntries);
    }
}
