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

import com.navercorp.pinpoint.common.server.config.AgentProperties;
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
    private static final int AGENT_ID_ENTRY_DELETE_BATCH_SIZE = 200;

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final ApplicationIndexDao applicationIndexDao;
    private final ApplicationDao applicationDao;
    private final AgentIdDao agentIdDao;
    private final Set<Integer> missingHeaderServiceTypeCodes;
    private final boolean v1TableEnabled;
    private final boolean v2TableEnabled;
    private final boolean applicationReadV2;
    private final boolean agentReadV2;

    private static final int UNDEFINED_SERVICE_TYPE_CODE = -1;

    public ApplicationIndexServiceImpl(ApplicationIndexDao applicationIndexDao,
                                       ApplicationDao applicationDao,
                                       AgentIdDao agentIdDao,
                                       AgentProperties agentProperties,
                                       @Value("${pinpoint.web.application.index.v1.enabled:true}") boolean v1TableEnabled,
                                       @Value("${pinpoint.web.application.index.v2.enabled:false}") boolean v2TableEnabled,
                                       @Value("${pinpoint.web.application.read.v2:false}") boolean readApplicationV2,
                                       @Value("${pinpoint.web.agent.read.v2:false}") boolean readAgentV2) {
        this.applicationIndexDao = Objects.requireNonNull(applicationIndexDao, "applicationIndexDao");
        this.applicationDao = Objects.requireNonNull(applicationDao, "applicationDao");
        this.agentIdDao = Objects.requireNonNull(agentIdDao, "agentIdDao");
        this.missingHeaderServiceTypeCodes = agentProperties.getMissingHeaderServiceTypeCodes();
        this.v1TableEnabled = v1TableEnabled;
        this.v2TableEnabled = v2TableEnabled;
        this.applicationReadV2 = readApplicationV2;
        this.agentReadV2 = readAgentV2;
    }

    @Override
    public List<Application> selectAllApplications() {
        if (applicationReadV2) {
            return this.applicationDao.getApplications(ServiceUid.DEFAULT_SERVICE_UID_CODE);
        }
        return this.applicationIndexDao.selectAllApplicationNames();
    }

    @Override
    public List<Application> selectApplication(String applicationName) {
        if (applicationReadV2) {
            return this.applicationDao.getApplications(ServiceUid.DEFAULT_SERVICE_UID_CODE, applicationName);
        }
        return this.applicationIndexDao.selectApplicationName(applicationName);
    }

    @Deprecated
    @Override
    public void deleteApplicationName(String applicationName) {
        if (v2TableEnabled) {
            List<Application> applicationList = this.applicationDao.getApplications(ServiceUid.DEFAULT_SERVICE_UID_CODE, applicationName);
            for (Application application : applicationList) {
                deleteApplicationV2(application.getApplicationName(), application.getServiceTypeCode());
            }
        }
        if (v1TableEnabled) {
            applicationIndexDao.deleteApplicationName(applicationName);
        }
    }

    @Override
    public void deleteApplication(String applicationName, int serviceTypeCode) {
        if (v2TableEnabled) {
            deleteApplicationV2(applicationName, serviceTypeCode);
        }
        if (v1TableEnabled) {
            List<String> agentIds = applicationIndexDao.selectAgentIds(applicationName, serviceTypeCode);
            applicationIndexDao.deleteAgentIds(applicationName, agentIds);
        }
    }

    private void deleteApplicationV2(String applicationName, int serviceTypeCode) {
        this.applicationDao.deleteApplication(ServiceUid.DEFAULT_SERVICE_UID_CODE, applicationName, serviceTypeCode);
//        List<AgentIdEntry> entries = queryAgentIdEntries(ServiceUid.DEFAULT_SERVICE_UID_CODE, applicationName, serviceTypeCode);
//        batchDeleteEntries(entries);
    }

    @Override
    public boolean isExistApplicationName(String applicationName) {
        if (applicationName == null) {
            return false;
        }
        if (applicationReadV2) {
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
        if (agentReadV2) {
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
        if (agentReadV2) {
            return queryAgentIdEntries(ServiceUid.DEFAULT_SERVICE_UID_CODE, applicationName, serviceTypeCode).stream()
                    .map(AgentIdEntry::getAgentId)
                    .distinct()
                    .toList();
        }
        return applicationIndexDao.selectAgentIds(applicationName, serviceTypeCode);
    }

    @Override
    @Deprecated
    public void deleteAgentIds(String applicationName, List<String> agentIds) {
        if (v2TableEnabled) {
            List<Application> applicationList = this.applicationDao.getApplications(ServiceUid.DEFAULT_SERVICE_UID_CODE, applicationName);
            for (Application application : applicationList) {
                batchDeleteAgentIdsV2(application.getService().getUid(), application.getApplicationName(), application.getServiceTypeCode(), agentIds);
            }
        }
        if (v1TableEnabled) {
            applicationIndexDao.deleteAgentIds(applicationName, agentIds);
        }
    }

    @Override
    public void deleteAgentIds(String applicationName, int serviceTypeCode, List<String> agentIds) {
        if (v2TableEnabled) {
            logger.info("deleteAgentIds v2 applicationName:{}, serviceTypeCode:{}, agentIds:{}", applicationName, serviceTypeCode, agentIds);
            batchDeleteAgentIdsV2(ServiceUid.DEFAULT_SERVICE_UID_CODE, applicationName, serviceTypeCode, agentIds);
        }
        if (v1TableEnabled) {
            logger.info("deleteAgentIds v1 applicationName:{}, serviceTypeCode:{}, agentIds:{}", applicationName, serviceTypeCode, agentIds);
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

    private List<AgentIdEntry> queryAgentIdEntries(int serviceUid, String applicationName, int serviceTypeCode) {
        List<AgentIdEntry> entries = agentIdDao.getAgentIdEntry(serviceUid, applicationName, serviceTypeCode);
        if (missingHeaderServiceTypeCodes.contains(serviceTypeCode)) {
            entries = new ArrayList<>(entries);
            entries.addAll(agentIdDao.getAgentIdEntry(serviceUid, applicationName, UNDEFINED_SERVICE_TYPE_CODE));
        }
        return entries;
    }

    private void batchDeleteAgentIdsV2(int serviceUid, String applicationName, int serviceTypeCode, List<String> agentIds) {
        List<AgentIdEntry> entries = queryAgentIdEntries(serviceUid, applicationName, serviceTypeCode);
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
