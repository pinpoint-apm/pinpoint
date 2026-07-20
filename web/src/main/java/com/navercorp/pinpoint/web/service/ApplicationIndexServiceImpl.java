/*
 * Copyright 2025 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.web.service;

import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.web.dao.AgentIdDao;
import com.navercorp.pinpoint.web.dao.ApplicationDao;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.Service;
import com.navercorp.pinpoint.web.vo.agent.AgentIdEntry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * @author Taejin Koo
 */
@org.springframework.stereotype.Service
public class ApplicationIndexServiceImpl implements ApplicationIndexService {
    private static final int AGENT_ID_ENTRY_DELETE_BATCH_SIZE = 200;

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final ApplicationDao applicationDao;
    private final AgentIdDao agentIdDao;

    public ApplicationIndexServiceImpl(ApplicationDao applicationDao,
                                       AgentIdDao agentIdDao) {
        this.applicationDao = Objects.requireNonNull(applicationDao, "applicationDao");
        this.agentIdDao = Objects.requireNonNull(agentIdDao, "agentIdDao");
    }

    @Override
    public List<Application> selectAllApplications() {
        return selectAllApplications(Service.DEFAULT);
    }

    @Override
    public List<Application> selectAllApplications(Service service) {
        return this.applicationDao.getApplications(service.getServiceUid().getUid());
    }

    @Override
    public List<Application> selectApplication(String applicationName) {
        return this.applicationDao.getApplications(ServiceUid.DEFAULT_SERVICE_UID_CODE, applicationName);
    }

    @Deprecated
    @Override
    public void deleteApplicationName(String applicationName) {
        List<Application> applicationList = this.applicationDao.getApplications(ServiceUid.DEFAULT_SERVICE_UID_CODE, applicationName);
        for (Application application : applicationList) {
            deleteApplication(application.getApplicationName(), application.getServiceTypeCode());
        }
    }

    @Override
    public void deleteApplication(String applicationName, int serviceTypeCode) {
        this.applicationDao.deleteApplication(ServiceUid.DEFAULT_SERVICE_UID_CODE, applicationName, serviceTypeCode);
    }

    @Override
    public boolean isExistApplicationName(String applicationName) {
        if (applicationName == null) {
            return false;
        }
        return !applicationDao.getApplications(ServiceUid.DEFAULT_SERVICE_UID_CODE, applicationName).isEmpty();
    }

    @Override
    public List<String> selectAgentIds(String applicationName) {
        return this.agentIdDao.getAgentIdEntry(ServiceUid.DEFAULT_SERVICE_UID_CODE, applicationName).stream()
                .map(AgentIdEntry::getAgentId)
                .distinct()
                .sorted()
                .toList();
    }

    @Override
    public List<String> selectAgentIds(String applicationName, int serviceTypeCode) {
        return agentIdDao.getAgentIdEntry(ServiceUid.DEFAULT_SERVICE_UID_CODE, applicationName, serviceTypeCode).stream()
                .map(AgentIdEntry::getAgentId)
                .distinct()
                .sorted()
                .toList();
    }

    @Override
    @Deprecated
    public void deleteAgentIds(String applicationName, List<String> agentIds) {
        List<Application> applicationList = this.applicationDao.getApplications(ServiceUid.DEFAULT_SERVICE_UID_CODE, applicationName);
        for (Application application : applicationList) {
            batchDeleteAgentIds(application.getService().getServiceUid().getUid(), application.getApplicationName(), application.getServiceTypeCode(), agentIds);
        }
    }

    @Override
    public void deleteAgentIds(String applicationName, int serviceTypeCode, List<String> agentIds) {
        logger.info("deleteAgentIds applicationName:{}, serviceTypeCode:{}, agentIds:{}", applicationName, serviceTypeCode, agentIds);
        batchDeleteAgentIds(ServiceUid.DEFAULT_SERVICE_UID_CODE, applicationName, serviceTypeCode, agentIds);
    }

    @Override
    @Deprecated
    public void deleteAgentId(String applicationName, String agentId) {
        deleteAgentIds(applicationName, List.of(agentId));
    }

    @Override
    public void deleteAgentId(String applicationName, int serviceTypeCode, String agentId) {
        deleteAgentIds(applicationName, serviceTypeCode, List.of(agentId));
    }

    private void batchDeleteAgentIds(int serviceUid, String applicationName, int serviceTypeCode, List<String> agentIds) {
        List<AgentIdEntry> entries = agentIdDao.getAgentIdEntry(serviceUid, applicationName, serviceTypeCode);
        Set<String> agentIdsSet = new HashSet<>(agentIds);
        List<AgentIdEntry> targets = entries.stream()
                .filter(e -> agentIdsSet.contains(e.getAgentId()))
                .toList();
        batchDeleteEntries(targets);
    }

    private void batchDeleteEntries(List<AgentIdEntry> entries) {
        for (int i = 0; i < entries.size(); i += AGENT_ID_ENTRY_DELETE_BATCH_SIZE) {
            int end = Math.min(i + AGENT_ID_ENTRY_DELETE_BATCH_SIZE, entries.size());
            agentIdDao.delete(entries.subList(i, end));
        }
    }
}
