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

import com.navercorp.pinpoint.web.dao.ApplicationIndexDao;
import com.navercorp.pinpoint.web.dao.ApplicationIndexDaoV2;
import com.navercorp.pinpoint.web.vo.Application;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * @author Taejin Koo
 */
@Service
public class ApplicationServiceImpl implements ApplicationService {

    private final ApplicationIndexDao applicationIndexDao;
    private final ApplicationIndexDaoV2 applicationIndexDaoV2;
    private final ApplicationInfoService applicationInfoService;

    public ApplicationServiceImpl(
            ApplicationIndexDao applicationIndexDao,
            ApplicationIndexDaoV2 applicationIndexDaoV2,
            ApplicationInfoService applicationInfoService
    ) {
        this.applicationIndexDao = Objects.requireNonNull(applicationIndexDao, "applicationIndexDao");
        this.applicationIndexDaoV2 = Objects.requireNonNull(applicationIndexDaoV2, "applicationIndexDaoV2");
        this.applicationInfoService = Objects.requireNonNull(applicationInfoService, "applicationInfoService");
    }

    @Override
    public boolean isExistApplicationName(String applicationName) {
        if (applicationName == null) {
            return false;
        }

        UUID applicationId = this.applicationInfoService.getApplicationId(applicationName);
        List<Application> applicationsV2 = this.applicationIndexDaoV2.selectApplicationName(applicationId);
        List<Application> applications = this.applicationIndexDao.selectApplicationName(applicationName);

        return applicationsV2.size() > 0 || applications.size() > 0;
    }
    @Override
    public List<Application> getApplications() {
        List<Application> applications1Origin = this.applicationIndexDao.selectAllApplicationNames();
        List<Application> applications1 = augmentApplicationId(applications1Origin);

        List<Application> applications2Origin = applicationIndexDaoV2.selectAllApplicationNames();
        List<Application> applications2 = augmentApplicationName(applications2Origin);

        List<Application> applications = new ArrayList<>(applications1.size() + applications2.size());
        applications.addAll(applications1);
        applications.addAll(applications2);

        return applications.stream()
                .distinct()
                .toList();
    }

    @Override
    public List<String> getAgents(UUID applicationId) {
        String applicationName = this.applicationInfoService.getApplicationName(applicationId);

        List<String> agents1 = this.applicationIndexDao.selectAgentIds(applicationName);
        List<String> agents2 = this.applicationIndexDaoV2.selectAgentIds(applicationId);

        List<String> agents = new ArrayList<>(agents1.size() + agents2.size());
        agents.addAll(agents1);
        agents.addAll(agents2);

        return agents.stream()
                .distinct()
                .toList();
    }

    @Override
    public void deleteApplication(UUID applicationId) {
        String applicationName = this.applicationInfoService.getApplicationName(applicationId);
        this.applicationIndexDao.deleteApplicationName(applicationName);
        this.applicationIndexDaoV2.deleteApplication(applicationId);
    }

    @Override
    public void deleteAgents(Map<UUID, List<String>> applicationAgentIdMap) {
        Map<String, List<String>> applicationAgentIdMap2 = new HashMap<>();
        for (Map.Entry<UUID, List<String>> entry : applicationAgentIdMap.entrySet()) {
            UUID applicationId = entry.getKey();
            String applicationName = this.applicationInfoService.getApplicationName(applicationId);
            applicationAgentIdMap2.put(applicationName, entry.getValue());
        }

        this.applicationIndexDao.deleteAgentIds(applicationAgentIdMap2);
        this.applicationIndexDaoV2.deleteAgentIds(applicationAgentIdMap);
    }

    @Override
    public void deleteAgent(UUID applicationId, String agentId) {
        String applicationName = this.applicationInfoService.getApplicationName(applicationId);

        this.applicationIndexDao.deleteAgentId(applicationName, agentId);
        this.applicationIndexDaoV2.deleteAgentId(applicationId, agentId);
    }

    private List<Application> augmentApplicationName(List<Application> applications) {
        List<Application> result = new ArrayList<>(applications.size());
        for (Application application : applications) {
            String applicationName = this.applicationInfoService.getApplicationName(application.id());
            result.add(new Application(application.id(), applicationName, application.serviceType()));
        }
        return result;
    }

    private List<Application> augmentApplicationId(List<Application> applications) {
        List<Application> result = new ArrayList<>(applications.size());
        for (Application application : applications) {
            UUID applicationId = this.applicationInfoService.getApplicationId(application.name());
            result.add(new Application(applicationId, application.name(), application.serviceType()));
        }
        return result;
    }

}
