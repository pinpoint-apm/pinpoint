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
import com.navercorp.pinpoint.uid.service.AgentNameService;
import com.navercorp.pinpoint.uid.vo.ApplicationUidRow;
import com.navercorp.pinpoint.web.component.ApplicationFactory;
import com.navercorp.pinpoint.web.dao.ApplicationIndexDao;
import com.navercorp.pinpoint.web.uid.service.ApplicationUidService;
import com.navercorp.pinpoint.web.vo.Application;
import jakarta.annotation.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * @author Taejin Koo
 */
@Service
public class ApplicationServiceImpl implements ApplicationService {

    private final ApplicationIndexDao applicationIndexDao;
    private final ApplicationFactory applicationFactory;

    private final ApplicationUidService applicationUidService;
    private final AgentNameService agentNameService;
    private final boolean uidApplicationListEnable;

    public ApplicationServiceImpl(ApplicationIndexDao applicationIndexDao,
                                  ApplicationFactory applicationFactory,
                                  @Nullable ApplicationUidService applicationUidService,
                                  @Nullable AgentNameService agentNameService,
                                  @Value("${pinpoint.web.uid.application.list.enabled:false}") boolean uidApplicationListEnable) {
        this.applicationIndexDao = Objects.requireNonNull(applicationIndexDao, "applicationIndexDao");
        this.applicationFactory = applicationFactory;
        this.applicationUidService = applicationUidService;
        this.agentNameService = agentNameService;
        this.uidApplicationListEnable = uidApplicationListEnable;
    }

    private boolean isUidApplicationListEnable() {
        return uidApplicationListEnable && applicationUidService != null;
    }

    @Override
    public List<Application> selectAllApplications() {
        if (isUidApplicationListEnable()) {
            return this.applicationUidService.getApplications(ServiceUid.DEFAULT).stream()
                    .map(row -> applicationFactory.createApplication(row.applicationName(), row.serviceTypeCode()))
                    .toList();
        }

        return this.applicationIndexDao.selectAllApplicationNames();
    }

    @Override
    public List<String> selectAllApplicationNames() {
        if (isUidApplicationListEnable()) {
            return this.applicationUidService.getApplications(ServiceUid.DEFAULT).stream()
                    .map(ApplicationUidRow::applicationName)
                    .toList();
        }
        return this.applicationIndexDao.selectAllApplicationNames()
                .stream()
                .map(Application::getName)
                .toList();
    }

    @Override
    public void deleteApplicationName(String applicationName) {
        applicationIndexDao.deleteApplicationName(applicationName);

        if (applicationUidService != null) {
            for (ApplicationUidRow row : applicationUidService.getApplications(ServiceUid.DEFAULT, applicationName)) {
                if (agentNameService != null) {
                    agentNameService.deleteAllAgents(row.serviceUid(), row.applicationUid());
                }
                applicationUidService.deleteApplication(row.serviceUid(), row.applicationName(), row.serviceTypeCode());
            }
        }
    }

    @Override
    public boolean isExistApplicationName(String applicationName) {
        if (applicationName == null) {
            return false;
        }

        if (isUidApplicationListEnable()) {
            List<ApplicationUidRow> applicationUid = applicationUidService.getApplications(ServiceUid.DEFAULT, applicationName);
            return !applicationUid.isEmpty();
        }

        List<Application> applications = applicationIndexDao.selectApplicationName(applicationName);
        for (Application application : applications) {
            if (applicationName.equals(application.getName())) {
                return true;
            }
        }

        return false;
    }

}
