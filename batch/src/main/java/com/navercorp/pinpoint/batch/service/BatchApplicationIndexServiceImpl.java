/*
 * Copyright 2024 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.batch.service;

import com.navercorp.pinpoint.common.server.uid.ApplicationUid;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.common.timeseries.time.Range;
import com.navercorp.pinpoint.uid.service.AgentIdService;
import com.navercorp.pinpoint.uid.service.BaseApplicationUidService;
import com.navercorp.pinpoint.uid.vo.ApplicationUidRow;
import com.navercorp.pinpoint.web.component.ApplicationFactory;
import com.navercorp.pinpoint.web.dao.ApplicationIndexDao;
import com.navercorp.pinpoint.web.dao.ApplicationTraceIndexDao;
import com.navercorp.pinpoint.web.vo.Application;
import jakarta.annotation.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Objects;

/**
 * @author youngjin.kim2
 */
@Service
public class BatchApplicationIndexServiceImpl implements BatchApplicationIndexService {

    private final ApplicationIndexDao applicationIndexDao;
    private final ApplicationTraceIndexDao applicationTraceIndexDao;
    private final ApplicationFactory applicationFactory;

    private final BaseApplicationUidService baseApplicationUidService;
    private final AgentIdService agentIdService;
    private final boolean uidApplicationListEnable;
    private final boolean uidAgentListEnable;

    public BatchApplicationIndexServiceImpl(
            ApplicationIndexDao applicationIndexDao,
            ApplicationTraceIndexDao applicationTraceIndexDao,
            ApplicationFactory applicationFactory,
            @Nullable BaseApplicationUidService baseApplicationUidService,
            @Nullable AgentIdService agentIdService,
            @Value("${pinpoint.batch.uid.application.list.enabled:false}") boolean uidApplicationListEnable,
            @Value("${pinpoint.batch.uid.agent.list.enabled:false}") boolean uidAgentListEnable) {
        this.applicationIndexDao = Objects.requireNonNull(applicationIndexDao, "applicationIndexDao");
        this.applicationTraceIndexDao = Objects.requireNonNull(applicationTraceIndexDao, "applicationTraceIndexDao");
        this.applicationFactory = Objects.requireNonNull(applicationFactory, "applicationFactory");
        this.baseApplicationUidService = baseApplicationUidService;
        this.agentIdService = agentIdService;
        this.uidApplicationListEnable = uidApplicationListEnable;
        this.uidAgentListEnable = uidAgentListEnable;
    }

    private boolean isUidApplicationListEnable() {
        return uidApplicationListEnable && baseApplicationUidService != null;
    }

    private boolean isUidAgentListEnable() {
        return uidAgentListEnable && baseApplicationUidService != null && agentIdService != null;
    }

    @Override
    public List<Application> selectAllApplications() {
        if (isUidApplicationListEnable()) {
            return this.baseApplicationUidService.getApplications(ServiceUid.DEFAULT).stream()
                    .map(attr -> applicationFactory.createApplication(attr.applicationName(), attr.serviceTypeCode()))
                    .toList();
        }
        return this.applicationIndexDao.selectAllApplicationNames();
    }

    @Override
    public List<String> selectAllApplicationNames() {
        if (isUidApplicationListEnable()) {
            return this.baseApplicationUidService.getApplications(ServiceUid.DEFAULT).stream()
                    .map(ApplicationUidRow::applicationName)
                    .toList();
        }
        return this.applicationIndexDao.selectAllApplicationNames()
                .stream()
                .map(Application::getName)
                .toList();
    }

    @Override
    public boolean isActive(String applicationName, Duration duration) {
        long now = System.currentTimeMillis();
        Range range = Range.between(now - duration.toMillis(), now);
        return hasTrace(applicationName, range);
    }

    private boolean hasTrace(String applicationName, Range range) {
        return this.applicationTraceIndexDao.hasTraceIndex(applicationName, range, false);
    }

    @Override
    public void remove(String applicationName) {
        this.applicationIndexDao.deleteApplicationName(applicationName);
        if (baseApplicationUidService != null) {
            List<ApplicationUidRow> applications = baseApplicationUidService.getApplications(ServiceUid.DEFAULT);
            for (ApplicationUidRow application : applications) {
                baseApplicationUidService.deleteApplication(ServiceUid.DEFAULT, application.applicationName(), application.serviceTypeCode());
            }
        }
    }

    @Override
    public List<String> selectAgentIds(String applicationName) {
        if (isUidAgentListEnable()) {
            List<ApplicationUid> applicationUidList = baseApplicationUidService.getApplications(ServiceUid.DEFAULT, applicationName).stream()
                    .map(ApplicationUidRow::applicationUid)
                    .toList();
            return agentIdService.getAgentId(ServiceUid.DEFAULT, applicationUidList).stream()
                    .flatMap(List::stream)
                    .toList();
        }
        return this.applicationIndexDao.selectAgentIds(applicationName);
    }

    @Override
    public void deleteAgentId(String applicationName, String agentId) {
        applicationIndexDao.deleteAgentId(applicationName, agentId);
        if (baseApplicationUidService != null && agentIdService != null) {
            deleteUidAgent(applicationName, List.of(agentId));
        }
    }

    private void deleteUidAgent(String applicationName, List<String> agentIds) {
        List<ApplicationUid> applicationUidList = baseApplicationUidService.getApplications(ServiceUid.DEFAULT, applicationName).stream()
                .map(ApplicationUidRow::applicationUid)
                .toList();
        for (ApplicationUid applicationUid : applicationUidList) {
            agentIdService.deleteAgent(ServiceUid.DEFAULT, applicationUid, agentIds);
        }
    }
}
