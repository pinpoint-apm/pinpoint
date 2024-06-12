/*
 * Copyright 2014 NAVER Corp.
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

import com.navercorp.pinpoint.common.id.AgentId;
import com.navercorp.pinpoint.common.id.ApplicationId;
import com.navercorp.pinpoint.common.server.util.time.Range;
import com.navercorp.pinpoint.common.util.CollectionUtils;
import com.navercorp.pinpoint.web.vo.Application;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author netspider
 * @author HyunGil Jeong
 */
@Service
public class AdminServiceImpl implements AdminService {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final ApplicationService applicationService;
    private final AgentInfoService agentInfoService;

    public AdminServiceImpl(
            ApplicationService applicationService,
            AgentInfoService agentInfoService
    ) {
        this.applicationService = Objects.requireNonNull(applicationService, "applicationService");
        this.agentInfoService = Objects.requireNonNull(agentInfoService, "agentInfoService");
    }

    @Override
    public void removeApplicationName(ApplicationId applicationId) {
        this.applicationService.deleteApplication(applicationId);
    }

    @Override
    public void removeAgentId(ApplicationId applicationId, String agentId) {
        this.applicationService.deleteAgent(applicationId, agentId);
    }

    @Override
    @Deprecated
    public void removeInactiveAgents(int durationDays) {
        if (durationDays < MIN_DURATION_DAYS_FOR_INACTIVITY) {
            throw new IllegalArgumentException("duration may not be less than " + MIN_DURATION_DAYS_FOR_INACTIVITY + " days");
        }

        List<ApplicationId> applicationIds = this.applicationService.getApplications()
                .stream()
                .map(Application::id)
                .distinct()
                .collect(Collectors.toList());
        Collections.shuffle(applicationIds);

        int index = 1;
        for (ApplicationId applicationId: applicationIds) {
            logger.info("Cleaning {} ({}/{})", applicationId, index++, applicationIds.size());
            removeInactiveAgentInApplication(applicationId, durationDays);
        }
    }

    @Override
    public int removeInactiveAgentInApplication(ApplicationId applicationId, int durationDays) {
        try {
            return removeInactiveAgentInApplication0(applicationId, durationDays);
        } catch (Exception e) {
            logger.error("Backoff to remove inactive agents in application {}", applicationId, e);
        }
        return 0;
    }

    private int removeInactiveAgentInApplication0(ApplicationId applicationId, int durationDays) {
        final List<String> agentsToDelete = new ArrayList<>(100);
        int deleteCount = 0;

        final List<String> agentIds = this.applicationService.getAgents(applicationId);
        for (String agentId: agentIds) {
            if (!isInactiveAgent(AgentId.of(agentId), durationDays)) {
                continue;
            }

            agentsToDelete.add(agentId);
            deleteCount++;

            if (agentsToDelete.size() >= 100) {
                logger.info("Delete {} of {}", agentsToDelete, applicationId);
                this.applicationService.deleteAgents(Map.of(applicationId, agentsToDelete));
                agentsToDelete.clear();
            }
        }

        if (!agentsToDelete.isEmpty()) {
            logger.info("Delete {} of {}", agentsToDelete, applicationId);
            this.applicationService.deleteAgents(Map.of(applicationId, agentsToDelete));
        }

        logger.info("({}/{}) agents of {} had been cleaned up", deleteCount, agentIds.size(), applicationId);
        return deleteCount;
    }

    @Override
    public Map<String, List<Application>> getAgentIdMap() {
        Map<String, List<Application>> agentIdMap = new TreeMap<>();
        List<Application> applications = this.applicationService.getApplications();
        for (Application application : applications) {
            List<String> agentIds = this.applicationService.getAgents(application.id());
            for (String agentId : agentIds) {
                List<Application> applicationList = agentIdMap.computeIfAbsent(agentId, k -> new ArrayList<>());
                applicationList.add(application);
            }
        }
        return agentIdMap;
    }

    @Override
    public Map<String, List<Application>> getDuplicateAgentIdMap() {
        Map<String, List<Application>> duplicateAgentIdMap = new TreeMap<>();
        Map<String, List<Application>> agentIdMap = this.getAgentIdMap();
        for (Map.Entry<String, List<Application>> entry : agentIdMap.entrySet()) {
            String agentId = entry.getKey();
            List<Application> applications = entry.getValue();
            if (CollectionUtils.hasLength(applications)) {
                duplicateAgentIdMap.put(agentId, applications);
            }
        }
        return duplicateAgentIdMap;
    }

    @Override
    public Map<String, List<Application>> getInactiveAgents(ApplicationId applicationId, int durationDays) {
        Objects.requireNonNull(applicationId, "applicationId");

        if (durationDays < MIN_DURATION_DAYS_FOR_INACTIVITY) {
            throw new IllegalArgumentException("duration may not be less than " + MIN_DURATION_DAYS_FOR_INACTIVITY + " days");
        }

        List<String> agentIds = this.applicationService.getAgents(applicationId);
        if (CollectionUtils.isEmpty(agentIds)) {
            return Collections.emptyMap();
        }
        Map<String, List<Application>> agentIdMap = this.getAgentIdMap();
        Map<String, List<Application>> inactiveAgentMap = new TreeMap<>();
        List<String> inactiveAgentIds = filterInactiveAgents(agentIds, durationDays);
        for (String inactiveAgentId : inactiveAgentIds) {
            List<Application> applications = agentIdMap.get(inactiveAgentId);
            inactiveAgentMap.put(inactiveAgentId, applications);
        }
        return inactiveAgentMap;
    }

    private List<String> filterInactiveAgents(List<String> agentIds, int durationDays) {
        if (CollectionUtils.isEmpty(agentIds)) {
            return Collections.emptyList();
        }

        return agentIds.stream()
                .filter(agentId -> isInactiveAgent(AgentId.of(agentId), durationDays))
                .collect(Collectors.toList());
    }

    private boolean isInactiveAgent(AgentId agentId, int durationDays) {
        long now = System.currentTimeMillis();
        Range range = Range.between(now - TimeUnit.DAYS.toMillis(durationDays), now);

        return !this.agentInfoService.isActiveAgent(agentId, range);
    }

}
