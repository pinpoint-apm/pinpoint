package com.navercorp.pinpoint.web.service;

import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.web.dao.AgentIdDao;
import com.navercorp.pinpoint.web.dao.ApplicationDao;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.agent.AgentIdEntry;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class ApplicationServiceV2Impl implements ApplicationServiceV2 {

    private final ApplicationDao applicationDao;
    private final AgentIdDao agentIdDao;

    public ApplicationServiceV2Impl(ApplicationDao applicationDao,
                                    AgentIdDao agentIdDao) {
        this.applicationDao = Objects.requireNonNull(applicationDao, "applicationDao");
        this.agentIdDao = Objects.requireNonNull(agentIdDao, "agentIdDao");
    }

    @Override
    public List<Application> getApplications(ServiceUid serviceUid) {
        return applicationDao.getApplications(serviceUid.getUid());
    }

    @Override
    public List<Application> getApplications(ServiceUid serviceUid, String applicationName) {
        return applicationDao.getApplications(serviceUid.getUid(), applicationName);
    }

    @Override
    public void deleteApplication(ServiceUid serviceUid, String applicationName, int serviceTypeCode) {
        applicationDao.deleteApplication(serviceUid.getUid(), applicationName, serviceTypeCode);
        deleteAllAgents(serviceUid, applicationName, serviceTypeCode);
    }

    private void deleteAllAgents(ServiceUid serviceUid, String applicationName, int serviceTypeCode) {
        List<AgentIdEntry> agentList = agentIdDao.getAgentIdEntry(serviceUid.getUid(), applicationName, serviceTypeCode);
        if (!agentList.isEmpty()) {
            agentIdDao.delete(agentList);
        }
    }
}
