package com.navercorp.pinpoint.uid.service;

import com.navercorp.pinpoint.common.server.uid.AgentIdentifier;
import com.navercorp.pinpoint.common.server.uid.ApplicationUid;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.uid.dao.AgentNameDao;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class AgentNameServiceImpl implements AgentNameService {

    private final AgentNameDao agentNameDao;

    public AgentNameServiceImpl(AgentNameDao agentNameDao) {
        this.agentNameDao = Objects.requireNonNull(agentNameDao, "agentListDao");
    }

    @Override
    public List<AgentIdentifier> getAgentIdentifier(ServiceUid serviceUid, ApplicationUid applicationUid) {
        Objects.requireNonNull(serviceUid, "serviceUid");
        Objects.requireNonNull(applicationUid, "applicationUid");

        return agentNameDao.selectAgentIdentifiers(serviceUid, applicationUid);
    }

    @Override
    public List<List<AgentIdentifier>> getAgentIdentifier(ServiceUid serviceUid, List<ApplicationUid> applicationUidList) {
        Objects.requireNonNull(serviceUid, "serviceUid");
        Objects.requireNonNull(applicationUidList, "applicationUidList");

        if (applicationUidList.isEmpty()) {
            return List.of();
        }
        return agentNameDao.selectAgentIdentifiers(serviceUid, applicationUidList);
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
