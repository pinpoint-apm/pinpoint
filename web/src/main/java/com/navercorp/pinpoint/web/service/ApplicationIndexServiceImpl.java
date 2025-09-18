/*
 * Copyright 2017 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.web.service;

import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.web.dao.ApplicationIndexDao;
import com.navercorp.pinpoint.web.vo.Application;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * @author Taejin Koo
 */
@Service
public class ApplicationIndexServiceImpl implements ApplicationIndexService {
    private final String defaultServiceName = ServiceUid.DEFAULT_SERVICE_UID_NAME;

    private final ApplicationIndexDao applicationIndexDao;
    private final ApplicationIndexServiceV2 applicationUidServiceV2;
    private final boolean readV2;
    private final boolean v2TableEnabled;

    public ApplicationIndexServiceImpl(ApplicationIndexDao applicationIndexDao,
                                       ApplicationIndexServiceV2 applicationUidServiceV2,
                                       @Value("${pinpoint.web.application.index.v2.enabled:false}") boolean readV2,
                                       @Value("${pinpoint.web.application.index.v2.table.enabled:false}") boolean v2TableEnabled) {
        this.applicationIndexDao = Objects.requireNonNull(applicationIndexDao, "applicationIndexDao");
        this.applicationUidServiceV2 = Objects.requireNonNull(applicationUidServiceV2, "applicationUidServiceV2");
        this.readV2 = readV2;
        this.v2TableEnabled = v2TableEnabled;
    }

    private boolean isReadV2() {
        return readV2 && v2TableEnabled;
    }

    private boolean isV2WriteEnabled() {
        return readV2 || v2TableEnabled;
    }

    @Override
    public List<Application> selectAllApplications() {
        if (isReadV2()) {
            return this.applicationUidServiceV2.getApplications(defaultServiceName);
        }
        return this.applicationIndexDao.selectAllApplicationNames();
    }

    @Override
    public List<String> selectAllApplicationNames() {
        if (isReadV2()) {
            return this.applicationUidServiceV2.getApplications(defaultServiceName).stream()
                    .map(Application::getName)
                    .toList();
        }
        return this.applicationIndexDao.selectAllApplicationNames()
                .stream()
                .map(Application::getName)
                .toList();
    }

    @Override
    public List<Application> selectApplication(String applicationName) {
        if (isReadV2()) {
            return this.applicationUidServiceV2.getApplications(defaultServiceName, applicationName);
        }

        return this.applicationIndexDao.selectApplicationName(applicationName);
    }

    @Override
    public void deleteApplicationName(String applicationName) {
        applicationIndexDao.deleteApplicationName(applicationName);

        if (isV2WriteEnabled()) {
            List<Application> applicationList = this.applicationUidServiceV2.getApplications(defaultServiceName, applicationName);
            for (Application application : applicationList) {
                this.applicationUidServiceV2.deleteApplication(defaultServiceName, application.getName(), application.getServiceTypeCode());
            }
        }
    }

    @Override
    public void deleteApplication(String applicationName, int serviceTypeCode) {
        List<String> agentIds = applicationIndexDao.selectAgentIds(applicationName, serviceTypeCode);
        applicationIndexDao.deleteAgentIds(Map.of(applicationName, agentIds));
        if (isV2WriteEnabled()) {
            this.applicationUidServiceV2.deleteApplication(defaultServiceName, applicationName, serviceTypeCode);
        }
    }

    @Override
    public boolean isExistApplicationName(String applicationName) {
        if (applicationName == null) {
            return false;
        }

        if (isReadV2()) {
            return !applicationUidServiceV2.getApplications(defaultServiceName, applicationName).isEmpty();
        }

        List<Application> applications = applicationIndexDao.selectApplicationName(applicationName);
        for (Application application : applications) {
            if (applicationName.equals(application.getName())) {
                return true;
            }
        }

        return false;
    }

    @Override
    public List<String> selectAgentIds(String applicationName) {
        if (isReadV2()) {
            List<Application> applicationList = this.applicationUidServiceV2.getApplications(defaultServiceName, applicationName);
            Set<String> agentIdSet = new HashSet<>();
            for (Application application : applicationList) {
                List<String> agentIdList = this.applicationUidServiceV2.getAgentIds(defaultServiceName, application.getName(), application.getServiceTypeCode());
                agentIdSet.addAll(agentIdList);
            }
            return new ArrayList<>(agentIdSet);
        }

        return applicationIndexDao.selectAgentIds(applicationName);
    }

    @Override
    public void deleteAgentIds(Map<String, List<String>> applicationAgentIdMap) {
        applicationIndexDao.deleteAgentIds(applicationAgentIdMap);
        if (isV2WriteEnabled()) {
            for (Map.Entry<String, List<String>> entry : applicationAgentIdMap.entrySet()) {
                deleteAgents(entry.getKey(), entry.getValue());
            }
        }
    }

    @Override
    public void deleteAgentId(String applicationName, String agentId) {
        applicationIndexDao.deleteAgentId(applicationName, agentId);
        if (isV2WriteEnabled()) {
            deleteAgents(applicationName, List.of(agentId));
        }
    }

    private void deleteAgents(String applicationName, List<String> agentIds) {
        List<Application> applicationList = this.applicationUidServiceV2.getApplications(defaultServiceName, applicationName);
        for (Application application : applicationList) {
            this.applicationUidServiceV2.deleteAgents(defaultServiceName, application.getName(), application.getServiceTypeCode(), agentIds);
        }
    }

    @Override
    public void deleteAgentId(String applicationName, int serviceTypeCode, String agentId) {
        applicationIndexDao.deleteAgentId(applicationName, agentId);
        if (isV2WriteEnabled()) {
            applicationUidServiceV2.deleteAgents(defaultServiceName, applicationName, serviceTypeCode, List.of(agentId));
        }
    }
}
