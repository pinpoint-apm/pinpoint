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
import com.navercorp.pinpoint.web.vo.agent.AgentListItem;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * @author Taejin Koo
 */
@Service
public class ApplicationIndexServiceImpl implements ApplicationIndexService {
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
            return this.applicationDao.getApplications(ServiceUid.DEFAULT.getUid());
        }
        return this.applicationIndexDao.selectAllApplicationNames();
    }

    @Override
    @Deprecated
    public List<String> selectAllApplicationNames() {
        if (isReadV2()) {
            return this.applicationDao.getApplications(ServiceUid.DEFAULT.getUid()).stream()
                    .map(Application::getName)
                    .distinct()
                    .toList();
        }
        return this.applicationIndexDao.selectAllApplicationNames()
                .stream()
                .map(Application::getName)
                .distinct()
                .toList();
    }

    @Override
    public List<Application> selectApplication(String applicationName) {
        if (isReadV2()) {
            return this.applicationDao.getApplications(ServiceUid.DEFAULT.getUid(), applicationName);
        }
        return this.applicationIndexDao.selectApplicationName(applicationName);
    }

    @Deprecated
    @Override
    public void deleteApplicationName(String applicationName) {
        if (v2Enabled) {
            List<Application> applicationList = this.applicationDao.getApplications(ServiceUid.DEFAULT.getUid(), applicationName);
            for (Application application : applicationList) {
                this.applicationDao.deleteApplication(ServiceUid.DEFAULT.getUid(), application.getName(), application.getServiceTypeCode());
            }
            List<AgentListItem> agentListItems = this.agentIdDao.getAgentListItems(ServiceUid.DEFAULT.getUid(), applicationName);
            for (AgentListItem agentListItem : agentListItems) {
                this.agentIdDao.delete(agentListItem.getServiceUid(),
                        agentListItem.getApplication().getName(), agentListItem.getApplication().getServiceTypeCode(),
                        agentListItem.getAgentId());
            }
        }
        if (v1Enabled) {
            applicationIndexDao.deleteApplicationName(applicationName);
        }
    }

    @Override
    public void deleteApplication(String applicationName, int serviceTypeCode) {
        if (v2Enabled) {
            this.applicationDao.deleteApplication(ServiceUid.DEFAULT.getUid(), applicationName, serviceTypeCode);
            List<AgentListItem> agentListItems = this.agentIdDao.getAgentListItems(ServiceUid.DEFAULT.getUid(), applicationName, serviceTypeCode);
            for (AgentListItem agentListItem : agentListItems) {
                this.agentIdDao.delete(agentListItem.getServiceUid(),
                        agentListItem.getApplication().getName(), agentListItem.getApplication().getServiceTypeCode(),
                        agentListItem.getAgentId());
            }
        }
        if (v1Enabled) {
            List<String> agentIds = applicationIndexDao.selectAgentIds(applicationName, serviceTypeCode);
            applicationIndexDao.deleteAgentIds(applicationName, agentIds);
        }
    }

    @Override
    public boolean isExistApplicationName(String applicationName) {
        if (applicationName == null) {
            return false;
        }

        if (isReadV2()) {
            return !applicationDao.getApplications(ServiceUid.DEFAULT.getUid(), applicationName).isEmpty();
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
            return this.agentIdDao.getAgentListItems(ServiceUid.DEFAULT.getUid(), applicationName).stream()
                    .map(AgentListItem::getAgentId)
                    .distinct()
                    .toList();
        }

        return applicationIndexDao.selectAgentIds(applicationName);
    }

    @Override
    public List<String> selectAgentIds(String applicationName, int serviceTypeCode) {
        if (isReadV2()) {
            return this.agentIdDao.getAgentListItems(ServiceUid.DEFAULT.getUid(), applicationName, serviceTypeCode).stream()
                    .map(AgentListItem::getAgentId)
                    .distinct()
                    .toList();
        }

        return applicationIndexDao.selectAgentIds(applicationName, serviceTypeCode);
    }

    @Override
    @Deprecated
    public void deleteAgentIds(String applicationName, List<String> agentIds) {
        if (v2Enabled) {
            deleteAgentsV2(applicationName, agentIds);
        }
        if (v1Enabled) {
            applicationIndexDao.deleteAgentIds(applicationName, agentIds);
        }
    }

    @Override
    public void deleteAgentIds(String applicationName, int serviceTypeCode, List<String> agentIds) {
        if (v2Enabled) {
            for (String agentId : agentIds) {
                this.agentIdDao.delete(ServiceUid.DEFAULT.getUid(), applicationName, serviceTypeCode, agentId);
            }
        }
        if (v1Enabled) {
            applicationIndexDao.deleteAgentIds(applicationName, agentIds);
        }
    }

    @Override
    @Deprecated
    public void deleteAgentId(String applicationName, String agentId) {
        if (v2Enabled) {
            deleteAgentsV2(applicationName, List.of(agentId));
        }
        if (v1Enabled) {
            applicationIndexDao.deleteAgentId(applicationName, agentId);
        }
    }

    private void deleteAgentsV2(String applicationName, List<String> agentIds) {
        List<Application> applicationList = this.applicationDao.getApplications(ServiceUid.DEFAULT.getUid(), applicationName);
        for (Application application : applicationList) {
            for (String agentId : agentIds) {
                this.agentIdDao.delete(ServiceUid.DEFAULT.getUid(), application.getName(), application.getServiceTypeCode(), agentId);
            }
        }
    }

    @Override
    public void deleteAgentId(String applicationName, int serviceTypeCode, String agentId) {
        if (v2Enabled) {
            agentIdDao.delete(ServiceUid.DEFAULT.getUid(), applicationName, serviceTypeCode, agentId);
        }
        if (v1Enabled) {
            applicationIndexDao.deleteAgentId(applicationName, agentId);
        }
    }
}
