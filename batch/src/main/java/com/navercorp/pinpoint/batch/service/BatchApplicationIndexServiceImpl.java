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

    private final Logger logger = LogManager.getLogger(this.getClass());
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
            return this.applicationDao.getApplications(ServiceUid.DEFAULT_SERVICE_UID_CODE);
        }
        return this.applicationIndexDao.selectAllApplicationNames();
    }

    @Override
    @Deprecated
    public List<String> selectAllApplicationNames() {
        if (isReadV2()) {
            return this.applicationDao.getApplications(ServiceUid.DEFAULT_SERVICE_UID_CODE).stream()
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
            this.applicationDao.deleteApplication(ServiceUid.DEFAULT_SERVICE_UID_CODE, applicationName, serviceTypeCode);
        }
        // v1 doesn't need application level deletion, just delete all agentIds
    }

    @Override
    public List<String> selectAgentIds(String applicationName) {
        if (isReadV2()) {
            return this.agentIdDao.getAgentIdEntry(ServiceUid.DEFAULT_SERVICE_UID_CODE, applicationName).stream()
                    .map(AgentIdEntry::getAgentId)
                    .distinct()
                    .toList();
        }
        return this.applicationIndexDao.selectAgentIds(applicationName);
    }

    @Override
    public List<String> selectAgentIds(String applicationName, int serviceTypeCode) {
        if (isReadV2()) {
            return this.agentIdDao.getAgentIdEntry(ServiceUid.DEFAULT_SERVICE_UID_CODE, applicationName, serviceTypeCode).stream()
                    .map(AgentIdEntry::getAgentId)
                    .distinct()
                    .toList();
        }
        return this.applicationIndexDao.selectAgentIds(applicationName, serviceTypeCode);
    }

    @Override
    public List<String> selectAgentIds(String applicationName, int serviceTypeCode, long maxTimestamp) {
        if (isReadV2()) {
            List<AgentIdEntry> agentIdEntryList = this.agentIdDao.getAgentIdEntry(ServiceUid.DEFAULT_SERVICE_UID_CODE, applicationName, serviceTypeCode);
            // dedupe first so that we can check agentId with the latest startTime
            return dedupeConsecutiveAgentId(agentIdEntryList).stream()
                    .filter(entry -> entry.getLastUpdated() <= maxTimestamp)
                    .map(AgentIdEntry::getAgentId)
                    .distinct()
                    .toList();
        }
        return this.applicationIndexDao.selectAgentIds(applicationName, serviceTypeCode, maxTimestamp);
    }

    private List<AgentIdEntry> dedupeConsecutiveAgentId(List<AgentIdEntry> orderedAgentSummaries) {
        List<AgentIdEntry> result = new ArrayList<>();
        AgentIdEntry previous = null;
        for (AgentIdEntry current : orderedAgentSummaries) {
            if (!isPreviousAgentId(previous, current)) {
                result.add(current);
                previous = current;
            }
        }
        return result;
    }

    private boolean isPreviousAgentId(AgentIdEntry previous, AgentIdEntry current) {
        return previous != null &&
                previous.getAgentId().equals(current.getAgentId()) &&
                previous.getApplication().equals(current.getApplication());
    }


    @Override
    public void deleteAgentIds(String applicationName, int serviceTypeCode, List<String> agentIds) {
        if (v2Enabled) {
            batchDeleteAgentIdsV2(ServiceUid.DEFAULT_SERVICE_UID_CODE, applicationName, serviceTypeCode, agentIds);
        }
        if (v1Enabled) {
            batchDeleteAgentIdsV1(ServiceUid.DEFAULT_SERVICE_UID_CODE, applicationName, serviceTypeCode, agentIds);
        }
    }

    private void batchDeleteAgentIdsV1(int serviceUid, String applicationName, int serviceTypeCode, List<String> agentIds) {
        int batchSize = 200;
        for (int i = 0; i < agentIds.size(); i += batchSize) {
            int end = Math.min(i + batchSize, agentIds.size());
            List<String> agentIdBatch = agentIds.subList(i, end);
            logger.info("Removing agents. serviceUid: {} application: {}@{}, agents: {}", serviceUid, applicationName, serviceTypeCode, agentIdBatch);
            try {
                applicationIndexDao.deleteAgentIds(applicationName, agentIdBatch);
            } catch (Exception e) {
                logger.error("Failed to remove agents. serviceUid: {} application: {}@{}, endIndex: {}", serviceUid, applicationName, serviceTypeCode, end, e);
            }
        }
    }


    private void batchDeleteAgentIdsV2(int serviceUid, String applicationName, int serviceTypeCode, List<String> agentIds) {
        List<AgentIdEntry> agentIdEntryList = agentIdDao.getAgentIdEntry(serviceUid, applicationName, serviceTypeCode);
        List<AgentIdEntry> targetAgentIdEntryList = new ArrayList<>(100);
        Set<String> agentIdsSet = new HashSet<>(agentIds);
        for (AgentIdEntry agentIdEntry : agentIdEntryList) {
            if (agentIdsSet.contains(agentIdEntry.getAgentId())) {
                targetAgentIdEntryList.add(agentIdEntry);
            }

            if (targetAgentIdEntryList.size() >= 100) {
                agentIdDao.delete(targetAgentIdEntryList);
                targetAgentIdEntryList.clear();
            }
        }
        if (!targetAgentIdEntryList.isEmpty()) {
            agentIdDao.delete(targetAgentIdEntryList);
        }
    }
}
