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
import com.navercorp.pinpoint.web.dao.ApplicationIndexDao;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.agent.AgentIdEntry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * @author Taejin Koo
 */
@Service
public class ApplicationIndexServiceImpl implements ApplicationIndexService {
    private final Logger logger = LogManager.getLogger(this.getClass());

    private final ApplicationIndexDao applicationIndexDao;
    private final ApplicationDao applicationDao;
    private final AgentIdDao agentIdDao;
    private final boolean v1Enabled;
    private final boolean v2Enabled;
    private final boolean readV2;

    public ApplicationIndexServiceImpl(ApplicationIndexDao applicationIndexDao,
                                       ApplicationDao applicationDao,
                                       AgentIdDao agentIdDao,
                                       @Value("${pinpoint.web.application.index.v1.enabled:true}") boolean v1Enabled,
                                       @Value("${pinpoint.web.application.index.v2.enabled:false}") boolean v2Enabled,
                                       @Value("${pinpoint.web.application.index.read.v2:false}") boolean readV2) {
        this.applicationIndexDao = Objects.requireNonNull(applicationIndexDao, "applicationIndexDao");
        this.applicationDao = Objects.requireNonNull(applicationDao, "applicationDao");
        this.agentIdDao = Objects.requireNonNull(agentIdDao, "agentIdDao");
        this.v1Enabled = v1Enabled;
        this.v2Enabled = v2Enabled;
        this.readV2 = readV2;
    }

    private boolean isReadV2() {
        return v2Enabled && readV2;
    }

    @Override
    public List<Application> selectAllApplications() {
        if (isReadV2()) {
            return this.applicationDao.getApplications(ServiceUid.DEFAULT_SERVICE_UID_CODE);
        }
        return this.applicationIndexDao.selectAllApplicationNames();
    }

    @Override
    @Deprecated
    public List<String> selectAllApplicationNames() {
        if (isReadV2()) {
            return this.applicationDao.getApplications(ServiceUid.DEFAULT_SERVICE_UID_CODE).stream()
                    .map(Application::getApplicationName)
                    .distinct()
                    .toList();
        }
        return this.applicationIndexDao.selectAllApplicationNames()
                .stream()
                .map(Application::getApplicationName)
                .distinct()
                .toList();
    }

    @Override
    public List<Application> selectApplication(String applicationName) {
        if (isReadV2()) {
            return this.applicationDao.getApplications(ServiceUid.DEFAULT_SERVICE_UID_CODE, applicationName);
        }
        return this.applicationIndexDao.selectApplicationName(applicationName);
    }

    @Deprecated
    @Override
    public void deleteApplicationName(String applicationName) {
        if (v2Enabled) {
            List<Application> applicationList = this.applicationDao.getApplications(ServiceUid.DEFAULT_SERVICE_UID_CODE, applicationName);
            for (Application application : applicationList) {
                deleteApplicationV2(application.getApplicationName(), application.getServiceTypeCode());
            }
        }
        if (v1Enabled) {
            applicationIndexDao.deleteApplicationName(applicationName);
        }
    }

    @Override
    public void deleteApplication(String applicationName, int serviceTypeCode) {
        if (v2Enabled) {
            deleteApplicationV2(applicationName, serviceTypeCode);
        }
        if (v1Enabled) {
            List<String> agentIds = applicationIndexDao.selectAgentIds(applicationName, serviceTypeCode);
            applicationIndexDao.deleteAgentIds(applicationName, agentIds);
        }
    }

    private void deleteApplicationV2(String applicationName, int serviceTypeCode) {
        // delete application
        this.applicationDao.deleteApplication(ServiceUid.DEFAULT_SERVICE_UID_CODE, applicationName, serviceTypeCode);
        // delete agentIds
        List<String> agentIds = selectAgentIds(applicationName, serviceTypeCode);
        batchDeleteAgentIdsV2(ServiceUid.DEFAULT_SERVICE_UID_CODE, applicationName, serviceTypeCode, agentIds);
    }

    @Override
    public boolean isExistApplicationName(String applicationName) {
        if (applicationName == null) {
            return false;
        }
        if (isReadV2()) {
            return !applicationDao.getApplications(ServiceUid.DEFAULT_SERVICE_UID_CODE, applicationName).isEmpty();
        }
        List<Application> applications = applicationIndexDao.selectApplicationName(applicationName);
        for (Application application : applications) {
            if (applicationName.equals(application.getApplicationName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public List<String> selectAgentIds(String applicationName) {
        if (isReadV2()) {
            return this.agentIdDao.getAgentIdEntry(ServiceUid.DEFAULT_SERVICE_UID_CODE, applicationName).stream()
                    .map(AgentIdEntry::getAgentId)
                    .distinct()
                    .sorted()
                    .toList();
        }
        return applicationIndexDao.selectAgentIds(applicationName);
    }

    @Override
    public List<String> selectAgentIds(String applicationName, int serviceTypeCode) {
        if (isReadV2()) {
            return this.agentIdDao.getAgentIdEntry(ServiceUid.DEFAULT_SERVICE_UID_CODE, applicationName, serviceTypeCode).stream()
                    .map(AgentIdEntry::getAgentId)
                    .distinct()
                    .toList();
        }
        return applicationIndexDao.selectAgentIds(applicationName, serviceTypeCode);
    }

    @Override
    @Deprecated
    public void deleteAgentIds(String applicationName, List<String> agentIds) {
        if (v2Enabled) {
            List<Application> applicationList = this.applicationDao.getApplications(ServiceUid.DEFAULT_SERVICE_UID_CODE, applicationName);
            for (Application application : applicationList) {
                batchDeleteAgentIdsV2(application.getService().getUid(), application.getApplicationName(), application.getServiceTypeCode(), agentIds);
            }
        }
        if (v1Enabled) {
            applicationIndexDao.deleteAgentIds(applicationName, agentIds);
        }
    }

    @Override
    public void deleteAgentIds(String applicationName, int serviceTypeCode, List<String> agentIds) {
        if (v2Enabled) {
            batchDeleteAgentIdsV2(ServiceUid.DEFAULT_SERVICE_UID_CODE, applicationName, serviceTypeCode, agentIds);
        }
        if (v1Enabled) {
            applicationIndexDao.deleteAgentIds(applicationName, agentIds);
        }
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

    private void deleteAgentIdV2(int serviceUid, String applicationName, int serviceTypeCode, String agentId) {
        List<AgentIdEntry> agentIdEntryList = agentIdDao.getAgentIdEntry(serviceUid, applicationName, serviceTypeCode, agentId);
        if (agentIdEntryList.isEmpty()) {
            logger.warn("AgentIdEntry not found. serviceUid: {} application: {}@{}, agentId: {}", serviceUid, applicationName, serviceTypeCode, agentId);
        } else {
            agentIdDao.delete(agentIdEntryList);
        }
    }

    private void batchDeleteAgentIdsV2(int serviceUid, String applicationName, int serviceTypeCode, List<String> agentIds) {
        List<AgentIdEntry> agentIdEntryList = agentIdDao.getAgentIdEntry(serviceUid, applicationName, serviceTypeCode);
        List<AgentIdEntry> targetAgentIdEntryList = new ArrayList<>(100);
        Set<String> agentIdsSet = new HashSet<>(agentIds);
        for (AgentIdEntry agentIdEntry : agentIdEntryList) {
            if (agentIdsSet.contains(agentIdEntry.getAgentId())) {
                targetAgentIdEntryList.add(agentIdEntry);
            }

            if (targetAgentIdEntryList.size() >= 100) {
                agentIdDao.delete(targetAgentIdEntryList);
                targetAgentIdEntryList.clear();
            }
        }
        if (!targetAgentIdEntryList.isEmpty()) {
            agentIdDao.delete(targetAgentIdEntryList);
        }
    }
}
