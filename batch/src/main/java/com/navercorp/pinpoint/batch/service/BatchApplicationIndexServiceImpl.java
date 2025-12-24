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
import com.navercorp.pinpoint.web.dao.AgentIdDao;
import com.navercorp.pinpoint.web.dao.ApplicationDao;
import com.navercorp.pinpoint.web.dao.ApplicationIndexDao;
import com.navercorp.pinpoint.web.scatter.dao.ApplicationTraceIndexDao;
import com.navercorp.pinpoint.web.scatter.dao.TraceIndexDao;
import com.navercorp.pinpoint.web.vo.Application;
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

    private final ApplicationDao applicationDao;
    private final AgentIdDao agentIdDao;
    private final TraceIndexDao traceIndexDao;

    private final boolean v1Enabled;
    private final boolean v2Enabled;
    private final boolean readV2;
    private final boolean readTraceIndexV2;

    public BatchApplicationIndexServiceImpl(
            ApplicationIndexDao applicationIndexDao,
            ApplicationTraceIndexDao applicationTraceIndexDao,
            ApplicationDao applicationDao, AgentIdDao agentIdDao,
            TraceIndexDao traceIndexDao,
            @Value("${pinpoint.batch.application.index.v1.enabled:true}") boolean v1Enabled,
            @Value("${pinpoint.batch.application.index.v2.enabled:false}") boolean v2Enabled,
            @Value("${pinpoint.batch.application.index.read.v2:false}") boolean readV2,
            @Value("${pinpoint.batch.trace.index.read.v2:false}") boolean traceIndexReadV2) {
        this.applicationIndexDao = Objects.requireNonNull(applicationIndexDao, "applicationIndexDao");
        this.applicationTraceIndexDao = Objects.requireNonNull(applicationTraceIndexDao, "applicationTraceIndexDao");
        this.applicationDao = Objects.requireNonNull(applicationDao, "applicationDao");
        this.agentIdDao = Objects.requireNonNull(agentIdDao, "agentIdDao");
        this.traceIndexDao = Objects.requireNonNull(traceIndexDao, "traceIndexDao");
        this.v1Enabled = v1Enabled;
        this.v2Enabled = v2Enabled;
        this.readV2 = readV2;
        this.readTraceIndexV2 = traceIndexReadV2;
    }

    private boolean isReadV2() {
        return v2Enabled && readV2;
    }

    @Override
    public List<Application> selectAllApplications() {
        if (isReadV2()) {
            return this.applicationDao.getApplications(ServiceUid.DEFAULT);
        }
        return this.applicationIndexDao.selectAllApplicationNames();
    }

    @Override
    @Deprecated
    public List<String> selectAllApplicationNames() {
        if (isReadV2()) {
            return this.applicationDao.getApplications(ServiceUid.DEFAULT).stream()
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
        if (readTraceIndexV2) {
            List<Application> applicationList = getApplications(applicationName);
            for (Application application : applicationList) {
                if (this.traceIndexDao.hasTraceIndex(ServiceUid.DEFAULT_SERVICE_UID_CODE, application.getName(), application.getServiceTypeCode(), range, false)) {
                    return true;
                }
            }
            return false;
        } else {
            return this.applicationTraceIndexDao.hasTraceIndex(applicationName, range, false);
        }
    }

    private List<Application> getApplications(String applicationName) {
        if (v2Enabled) {
            return this.applicationDao.getApplications(ServiceUid.DEFAULT, applicationName);
        }
        return this.applicationIndexDao.selectApplicationName(applicationName);
    }

    @Override
    public void remove(String applicationName) {
        if (v2Enabled) {
            List<Application> applicationList = this.applicationDao.getApplications(ServiceUid.DEFAULT, applicationName);
            for (Application application : applicationList) {
                this.applicationDao.deleteApplication(ServiceUid.DEFAULT, application.getName(), application.getServiceTypeCode());
            }
        }
        if (v1Enabled) {
            this.applicationIndexDao.deleteApplicationName(applicationName);
        }
    }

    @Override
    public List<String> selectAgentIds(String applicationName) {
        if (isReadV2()) {
            return this.agentIdDao.getAgentIds(ServiceUid.DEFAULT, applicationName);
        }
        return this.applicationIndexDao.selectAgentIds(applicationName);
    }

    @Override
    public void deleteAgentId(String applicationName, String agentId) {
        if (v2Enabled) {
            List<Application> applicationList = this.applicationDao.getApplications(ServiceUid.DEFAULT, applicationName);
            for (Application application : applicationList) {
                this.agentIdDao.deleteAgents(ServiceUid.DEFAULT, application.getName(), application.getServiceTypeCode(), List.of(agentId));
            }
        }
        if (v1Enabled) {
            applicationIndexDao.deleteAgentId(applicationName, agentId);
        }
    }
}
