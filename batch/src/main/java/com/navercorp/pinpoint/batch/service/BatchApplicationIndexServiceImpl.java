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
import com.navercorp.pinpoint.web.dao.AgentIdDao;
import com.navercorp.pinpoint.web.dao.ApplicationDao;
import com.navercorp.pinpoint.web.dao.ApplicationIndexDao;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.agent.AgentIdEntry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * @author youngjin.kim2
 */
@Service
public class BatchApplicationIndexServiceImpl implements BatchApplicationIndexService {

    private final Logger logger = LogManager.getLogger(this.getClass());
    private final ApplicationIndexDao applicationIndexDao;

    private final ApplicationDao applicationDao;
    private final AgentIdDao agentIdDao;
    private final boolean readV2;

    public BatchApplicationIndexServiceImpl(
            ApplicationIndexDao applicationIndexDao,
            ApplicationDao applicationDao, AgentIdDao agentIdDao,
            @Value("${pinpoint.batch.application.index.read.v2:false}") boolean readV2) {
        this.applicationIndexDao = Objects.requireNonNull(applicationIndexDao, "applicationIndexDao");
        this.applicationDao = Objects.requireNonNull(applicationDao, "applicationDao");
        this.agentIdDao = Objects.requireNonNull(agentIdDao, "agentIdDao");
        this.readV2 = readV2;
    }

    @Override
    public List<Application> selectAllApplications() {
        if (readV2) {
            return this.applicationDao.getApplications(ServiceUid.DEFAULT_SERVICE_UID_CODE);
        }
        return this.applicationIndexDao.selectAllApplicationNames();
    }

    @Override
    @Deprecated
    public List<String> selectAllApplicationNames() {
        if (readV2) {
            return this.applicationDao.getApplications(ServiceUid.DEFAULT_SERVICE_UID_CODE).stream()
                    .map(Application::getApplicationName)
                    .toList();
        }
        return this.applicationIndexDao.selectAllApplicationNames()
                .stream()
                .map(Application::getApplicationName)
                .toList();
    }

    @Override
    public List<String> selectAgentIds(String applicationName) {
        if (readV2) {
            return this.agentIdDao.getAgentIdEntry(ServiceUid.DEFAULT_SERVICE_UID_CODE, applicationName).stream()
                    .map(AgentIdEntry::getAgentId)
                    .distinct()
                    .toList();
        }
        return this.applicationIndexDao.selectAgentIds(applicationName);
    }

    @Override
    public List<String> selectAgentIds(String applicationName, int serviceTypeCode) {
        if (readV2) {
            return this.agentIdDao.getAgentIdEntry(ServiceUid.DEFAULT_SERVICE_UID_CODE, applicationName, serviceTypeCode).stream()
                    .map(AgentIdEntry::getAgentId)
                    .distinct()
                    .toList();
        }
        return this.applicationIndexDao.selectAgentIds(applicationName, serviceTypeCode);
    }
}
