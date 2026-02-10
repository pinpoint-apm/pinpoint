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
import com.navercorp.pinpoint.web.vo.agent.AgentListItem;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * @author youngjin.kim2
 */
@Service
public class BatchApplicationIndexServiceImpl implements BatchApplicationIndexService {

    private final ApplicationIndexDao applicationIndexDao;

    private final ApplicationDao applicationDao;
    private final AgentIdDao agentIdDao;

    private final boolean v1Enabled;
    private final boolean v2Enabled;
    private final boolean readV2;

    public BatchApplicationIndexServiceImpl(
            ApplicationIndexDao applicationIndexDao,
            ApplicationDao applicationDao, AgentIdDao agentIdDao,
            @Value("${pinpoint.batch.application.index.v1.enabled:true}") boolean v1Enabled,
            @Value("${pinpoint.batch.application.index.v2.enabled:false}") boolean v2Enabled,
            @Value("${pinpoint.batch.application.index.read.v2:false}") boolean readV2) {
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
                    .toList();
        }
        return this.applicationIndexDao.selectAllApplicationNames()
                .stream()
                .map(Application::getName)
                .toList();
    }

    @Override
    public void remove(String applicationName, int serviceTypeCode) {
        if (v2Enabled) {
            this.applicationDao.deleteApplication(ServiceUid.DEFAULT.getUid(), applicationName, serviceTypeCode);
        }
        // v1 doesn't need application level deletion, just delete all agentIds
    }

    @Override
    public List<String> selectAgentIds(String applicationName) {
        if (isReadV2()) {
            return this.agentIdDao.getAgentListItems(ServiceUid.DEFAULT.getUid(), applicationName).stream()
                    .map(AgentListItem::getAgentId)
                    .distinct()
                    .toList();
        }
        return this.applicationIndexDao.selectAgentIds(applicationName);
    }

    @Override
    public List<String> selectAgentIds(String applicationName, int serviceTypeCode) {
        if (isReadV2()) {
            return this.agentIdDao.getAgentListItems(ServiceUid.DEFAULT.getUid(), applicationName, serviceTypeCode).stream()
                    .map(AgentListItem::getAgentId)
                    .distinct()
                    .toList();
        }
        return this.applicationIndexDao.selectAgentIds(applicationName, serviceTypeCode);
    }

    @Override
    public List<String> selectAgentIds(String applicationName, int serviceTypeCode, long maxTimestamp) {
        if (isReadV2()) {
            return this.agentIdDao.getAgentListItems(ServiceUid.DEFAULT.getUid(), applicationName, serviceTypeCode, maxTimestamp).stream()
                    .map(AgentListItem::getAgentId)
                    .distinct()
                    .toList();
        }
        return this.applicationIndexDao.selectAgentIds(applicationName, serviceTypeCode, maxTimestamp);
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
}
