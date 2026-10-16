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

import com.navercorp.pinpoint.common.server.bo.AgentIdEntry;
import com.navercorp.pinpoint.common.server.bo.Application;
import com.navercorp.pinpoint.common.server.dao.AgentIdDao;
import com.navercorp.pinpoint.common.server.dao.ApplicationDao;
import com.navercorp.pinpoint.common.server.uid.Service;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;

import java.util.List;
import java.util.Objects;

/**
 * @author youngjin.kim2
 */
public class BatchApplicationIndexV2ServiceImpl implements BatchApplicationIndexService {

    private final ApplicationDao applicationDao;
    private final AgentIdDao agentIdDao;

    public BatchApplicationIndexV2ServiceImpl(ApplicationDao applicationDao, AgentIdDao agentIdDao) {
        this.applicationDao = Objects.requireNonNull(applicationDao, "applicationDao");
        this.agentIdDao = Objects.requireNonNull(agentIdDao, "agentIdDao");
    }

    @Override
    public List<Application> selectApplications(Service service) {
        return this.applicationDao.getApplications(service.getServiceUid());
    }

    @Override
    @Deprecated
    public List<Application> selectAllApplications() {
        return selectApplications(Service.DEFAULT);
    }

    @Override
    public List<String> selectAgentIds(Application application) {
        return this.agentIdDao.getAgentIdEntry(application.getService().getServiceUid(),
                        application.getApplicationName(), application.getServiceTypeCode()).stream()
                .map(AgentIdEntry::getAgentId)
                .distinct()
                .toList();
    }

    @Override
    public List<String> selectAgentIds(String applicationName) {
        return this.agentIdDao.getAgentIdEntry(ServiceUid.DEFAULT_SERVICE_UID_CODE, applicationName).stream()
                .map(AgentIdEntry::getAgentId)
                .distinct()
                .toList();
    }
}
