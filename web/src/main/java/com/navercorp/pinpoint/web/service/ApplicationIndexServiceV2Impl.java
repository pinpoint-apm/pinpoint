package com.navercorp.pinpoint.web.service;

import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.web.dao.AgentIdDao;
import com.navercorp.pinpoint.web.dao.ApplicationDao;
import com.navercorp.pinpoint.web.uid.service.ServiceUidService;
import com.navercorp.pinpoint.web.vo.Application;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class ApplicationIndexServiceV2Impl implements ApplicationIndexServiceV2 {
    private final ServiceUidService serviceUidService;

    private final ApplicationDao applicationDao;
    private final AgentIdDao agentIdDao;

    public ApplicationIndexServiceV2Impl(@Autowired(required = false) ServiceUidService serviceUidService,
                                         ApplicationDao applicationDao,
                                         AgentIdDao agentIdDao) {
        this.serviceUidService = serviceUidService;
        this.applicationDao = Objects.requireNonNull(applicationDao, "applicationDao");
        this.agentIdDao = Objects.requireNonNull(agentIdDao, "agentIdDao");
    }

    @Override
    public List<Application> getApplications(String serviceName) {
        ServiceUid serviceUid = handleServiceUid(serviceName);
        return applicationDao.getApplications(serviceUid.getUid());
    }

    @Override
    public List<Application> getApplications(String serviceName, String applicationName) {
        ServiceUid serviceUid = handleServiceUid(serviceName);
        return applicationDao.getApplications(serviceUid.getUid(), applicationName);
    }

    @Override
    public List<String> getAgentIds(String serviceName, String applicationName, int serviceTypeCode) {
        ServiceUid serviceUid = handleServiceUid(serviceName);
        return agentIdDao.getAgentIds(serviceUid.getUid(), applicationName, serviceTypeCode);
    }

    @Override
    public void deleteApplication(String serviceName, String applicationName, int serviceTypeCode) {
        ServiceUid serviceUid = handleServiceUid(serviceName);
        deleteAllAgents(serviceName, applicationName, serviceTypeCode);
        applicationDao.deleteApplication(serviceUid.getUid(), applicationName, serviceTypeCode);
    }

    @Override
    public void deleteAllAgents(String serviceName, String applicationName, int serviceTypeCode) {
        ServiceUid serviceUid = handleServiceUid(serviceName);
        List<String> agentList = agentIdDao.getAgentIds(serviceUid.getUid(), applicationName, serviceTypeCode);
        agentIdDao.deleteAgents(serviceUid.getUid(), applicationName, serviceTypeCode, agentList);
    }

    @Override
    public void deleteAgents(String serviceName, String applicationName, int serviceTypeCode, List<String> agentIdList) {
        ServiceUid serviceUid = handleServiceUid(serviceName);
        agentIdDao.deleteAgents(serviceUid.getUid(), applicationName, serviceTypeCode, agentIdList);
    }

    private ServiceUid handleServiceUid(String serviceName) {
        if (serviceUidService == null || StringUtils.isEmpty(serviceName)) {
            return ServiceUid.DEFAULT;
        }
        ServiceUid serviceUid = serviceUidService.getServiceUid(serviceName);
        if (serviceUid == null) {
            throw new IllegalArgumentException("service not found. name: " + serviceName);
        }
        return serviceUid;
    }
}
