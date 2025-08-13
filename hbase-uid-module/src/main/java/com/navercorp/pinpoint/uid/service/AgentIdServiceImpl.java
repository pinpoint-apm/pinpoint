package com.navercorp.pinpoint.uid.service;

import com.navercorp.pinpoint.common.server.uid.ApplicationUid;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.uid.dao.AgentIdDao;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class AgentIdServiceImpl implements AgentIdService {

    private final AgentIdDao agentIdDao;

    public AgentIdServiceImpl(AgentIdDao agentIdDao) {
        this.agentIdDao = Objects.requireNonNull(agentIdDao, "agentListDao");
    }

    @Override
    public void insert(ServiceUid serviceUid, ApplicationUid applicationUid, String agentId) {
        Objects.requireNonNull(serviceUid, "serviceUid");
        Objects.requireNonNull(applicationUid, "applicationUid");
        Objects.requireNonNull(agentId, "agentId");
        agentIdDao.insert(serviceUid, applicationUid, agentId);
    }

    @Override
    public List<String> getAgentId(ServiceUid serviceUid, ApplicationUid applicationUid) {
        Objects.requireNonNull(serviceUid, "serviceUid");
        Objects.requireNonNull(applicationUid, "applicationUid");
        return agentIdDao.scanAgentId(serviceUid, applicationUid);
    }

    @Override
    public List<List<String>> getAgentId(ServiceUid serviceUid, List<ApplicationUid> applicationUidList) {
        Objects.requireNonNull(serviceUid, "serviceUid");
        Objects.requireNonNull(applicationUidList, "applicationUidList");
        if (applicationUidList.isEmpty()) {
            return List.of();
        }
        return agentIdDao.scanAgentId(serviceUid, applicationUidList);
    }

    @Override
    public void deleteAllAgent(ServiceUid serviceUid, ApplicationUid applicationUid) {
        Objects.requireNonNull(serviceUid, "serviceUid");
        Objects.requireNonNull(applicationUid, "applicationUid");
        List<String> agentIds = agentIdDao.scanAgentId(serviceUid, applicationUid);
        deleteAgent(serviceUid, applicationUid, agentIds);
    }

    @Override
    public void deleteAgent(ServiceUid serviceUid, ApplicationUid applicationUid, String agentId) {
        Objects.requireNonNull(serviceUid, "serviceUid");
        Objects.requireNonNull(applicationUid, "applicationUid");
        agentIdDao.deleteAgents(serviceUid, applicationUid, List.of(agentId));
    }

    @Override
    public void deleteAgent(ServiceUid serviceUid, ApplicationUid applicationUid, List<String> agentIds) {
        Objects.requireNonNull(serviceUid, "serviceUid");
        Objects.requireNonNull(applicationUid, "applicationUid");
        if (agentIds.isEmpty()) {
            return;
        }
        agentIdDao.deleteAgents(serviceUid, applicationUid, agentIds);
    }
}
