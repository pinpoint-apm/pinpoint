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

import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.common.timeseries.time.Range;
import com.navercorp.pinpoint.web.dao.ApplicationIndexDao;
import com.navercorp.pinpoint.web.dao.ApplicationTraceIndexDao;
import com.navercorp.pinpoint.web.service.ApplicationIndexServiceV2;
import com.navercorp.pinpoint.web.vo.Application;
import jakarta.annotation.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * @author youngjin.kim2
 */
@Service
public class BatchApplicationIndexServiceImpl implements BatchApplicationIndexService {

    private final ApplicationIndexDao applicationIndexDao;
    private final ApplicationTraceIndexDao applicationTraceIndexDao;

    private final ApplicationIndexServiceV2 applicationIndexServiceV2;
    private final boolean readV2;
    private final boolean v2TableEnabled;

    public BatchApplicationIndexServiceImpl(
            ApplicationIndexDao applicationIndexDao,
            ApplicationTraceIndexDao applicationTraceIndexDao,
            @Nullable ApplicationIndexServiceV2 applicationIndexServiceV2,
            @Value("${pinpoint.batch.application.index.v2.enabled:false}") boolean readV2,
            @Value("${pinpoint.batch.application.index.v2.table.enabled:false}") boolean v2TableEnabled) {
        this.applicationIndexDao = Objects.requireNonNull(applicationIndexDao, "applicationIndexDao");
        this.applicationTraceIndexDao = Objects.requireNonNull(applicationTraceIndexDao, "applicationTraceIndexDao");
        this.applicationIndexServiceV2 = Objects.requireNonNull(applicationIndexServiceV2, "applicationIndexServiceV2");
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
            return this.applicationIndexServiceV2.getApplications(ServiceUid.DEFAULT_SERVICE_UID_NAME);
        }
        return this.applicationIndexDao.selectAllApplicationNames();
    }

    @Override
    public List<String> selectAllApplicationNames() {
        if (isReadV2()) {
            return this.applicationIndexServiceV2.getApplications(ServiceUid.DEFAULT_SERVICE_UID_NAME).stream()
                    .map(Application::getName)
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
        if (isV2WriteEnabled()) {
            List<Application> applicationList = this.applicationIndexServiceV2.getApplications(ServiceUid.DEFAULT_SERVICE_UID_NAME, applicationName);
            for (Application application : applicationList) {
                this.applicationIndexServiceV2.deleteApplication(ServiceUid.DEFAULT_SERVICE_UID_NAME, application.getName(), application.getServiceTypeCode());
            }
        }
    }

    @Override
    public List<String> selectAgentIds(String applicationName) {
        if (isReadV2()) {
            List<Application> applicationList = this.applicationIndexServiceV2.getApplications(ServiceUid.DEFAULT_SERVICE_UID_NAME, applicationName);
            Set<String> agentIdSet = new HashSet<>();
            for (Application application : applicationList) {
                List<String> agentIdList = this.applicationIndexServiceV2.getAgentIds(ServiceUid.DEFAULT_SERVICE_UID_NAME, application.getName(), application.getServiceTypeCode());
                agentIdSet.addAll(agentIdList);
            }
            return new ArrayList<>(agentIdSet);
        }
        return this.applicationIndexDao.selectAgentIds(applicationName);
    }

    @Override
    public void deleteAgentId(String applicationName, String agentId) {
        applicationIndexDao.deleteAgentId(applicationName, agentId);
        applicationIndexDao.deleteAgentId(applicationName, agentId);
        if (isV2WriteEnabled()) {
            deleteAgents(applicationName, List.of(agentId));
        }
    }

    private void deleteAgents(String applicationName, List<String> agentIds) {
        List<Application> applicationList = this.applicationIndexServiceV2.getApplications(ServiceUid.DEFAULT_SERVICE_UID_NAME, applicationName);
        for (Application application : applicationList) {
            this.applicationIndexServiceV2.deleteAgents(ServiceUid.DEFAULT_SERVICE_UID_NAME, application.getName(), application.getServiceTypeCode(), agentIds);
        }
    }
}
