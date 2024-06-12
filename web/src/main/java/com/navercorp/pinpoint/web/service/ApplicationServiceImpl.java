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

import com.navercorp.pinpoint.common.id.ApplicationId;
import com.navercorp.pinpoint.common.id.ServiceId;
import com.navercorp.pinpoint.common.server.bo.ApplicationSelector;
import com.navercorp.pinpoint.web.dao.ApplicationIndexDao;
import com.navercorp.pinpoint.web.dao.ApplicationIndexDaoV2;
import com.navercorp.pinpoint.web.vo.Application;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author Taejin Koo
 */
@Service
public class ApplicationServiceImpl implements ApplicationService {

    private final ApplicationIndexDao applicationIndexDao;
    private final ApplicationIndexDaoV2 applicationIndexDaoV2;
    private final ApplicationInfoService applicationInfoService;
    private final ServiceInfoService serviceInfoService;

    public ApplicationServiceImpl(
            ApplicationIndexDao applicationIndexDao,
            ApplicationIndexDaoV2 applicationIndexDaoV2,
            ApplicationInfoService applicationInfoService,
            ServiceInfoService serviceInfoService
    ) {
        this.applicationIndexDao = Objects.requireNonNull(applicationIndexDao, "applicationIndexDao");
        this.applicationIndexDaoV2 = Objects.requireNonNull(applicationIndexDaoV2, "applicationIndexDaoV2");
        this.applicationInfoService = Objects.requireNonNull(applicationInfoService, "applicationInfoService");
        this.serviceInfoService = Objects.requireNonNull(serviceInfoService, "serviceInfoService");
    }

    @Override
    public boolean isExistApplicationName(ApplicationId applicationId) {
        String applicationName = this.applicationInfoService.getApplication(applicationId).name();
        List<Application> applicationsV2 = this.applicationIndexDaoV2.selectApplicationName(applicationId);
        List<Application> applications = this.applicationIndexDao.selectApplicationByName(applicationName);
        return applicationsV2.size() > 0 || applications.size() > 0;
    }
    @Override
    public List<Application> getApplications() {
        List<Application> applications1Origin = this.applicationIndexDao.selectAllApplications();
        List<Application> applications1 = augmentApplicationId(applications1Origin);

        List<Application> applications2Origin = applicationIndexDaoV2.selectAllApplications();
        List<Application> applications2 = augmentApplicationName(applications2Origin);

        List<Application> applications = new ArrayList<>(applications1.size() + applications2.size());
        applications.addAll(applications1);
        applications.addAll(applications2);

        return applications.stream()
                .distinct()
                .toList();
    }

    @Override
    public List<Application> getApplications(ServiceId serviceId) {
        List<Application> applications = new ArrayList<>();
        if (serviceId.equals(ServiceId.DEFAULT_ID)) {
            List<Application> legacyApplications = this.applicationIndexDao.selectAllApplications();
            applications.addAll(augmentApplicationId(legacyApplications));
        }
        List<ApplicationId> applicationIds = this.serviceInfoService.getApplicationIds(serviceId);
        for (ApplicationId applicationId : applicationIds) {
            Application application = this.applicationInfoService.getApplication(applicationId);
            if (application != null) {
                applications.add(application);
            }
        }
        return applications.stream().distinct().toList();
    }

    @Override
    public List<String> getAgents(ApplicationId applicationId) {
        Application application = this.applicationInfoService.getApplication(applicationId);

        List<String> agents1 = this.applicationIndexDao.selectAgentIds(application.name());
        List<String> agents2 = this.applicationIndexDaoV2.selectAgentIds(applicationId);

        List<String> agents = new ArrayList<>(agents1.size() + agents2.size());
        agents.addAll(agents1);
        agents.addAll(agents2);

        return agents.stream()
                .distinct()
                .toList();
    }

    @Override
    public void deleteApplication(ApplicationId applicationId) {
        Application application = this.applicationInfoService.getApplication(applicationId);
        this.applicationIndexDao.deleteApplicationName(application.name());
        this.applicationIndexDaoV2.deleteApplication(applicationId);
    }

    @Override
    public void deleteAgents(Map<ApplicationId, List<String>> applicationAgentIdMap) {
        Map<String, List<String>> applicationAgentIdMap2 = new HashMap<>();
        for (Map.Entry<ApplicationId, List<String>> entry : applicationAgentIdMap.entrySet()) {
            ApplicationId applicationId = entry.getKey();
            Application application = this.applicationInfoService.getApplication(applicationId);
            applicationAgentIdMap2.put(application.name(), entry.getValue());
        }

        this.applicationIndexDao.deleteAgentIds(applicationAgentIdMap2);
        this.applicationIndexDaoV2.deleteAgentIds(applicationAgentIdMap);
    }

    @Override
    public void deleteAgent(ApplicationId applicationId, String agentId) {
        Application application = this.applicationInfoService.getApplication(applicationId);

        this.applicationIndexDao.deleteAgentId(application.name(), agentId);
        this.applicationIndexDaoV2.deleteAgentId(applicationId, agentId);
    }

    private List<Application> augmentApplicationName(List<Application> applications) {
        List<Application> result = new ArrayList<>(applications.size());
        for (Application application : applications) {
            Application helper = this.applicationInfoService.getApplication(application.id());
            if (helper != null) {
                result.add(new Application(helper.id(), helper.name(), helper.serviceType()));
            }
        }
        return result;
    }

    private List<Application> augmentApplicationId(List<Application> applications) {
        List<Application> result = new ArrayList<>(applications.size());
        for (Application application : applications) {
            ApplicationSelector appSelector = new ApplicationSelector(
                    ServiceId.DEFAULT_ID, application.name(), application.getServiceTypeCode());
            ApplicationId applicationId = this.applicationInfoService.getApplicationId(appSelector);
            result.add(new Application(applicationId, application.name(), application.serviceType()));
        }
        return result;
    }

}
