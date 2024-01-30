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

import com.navercorp.pinpoint.common.server.util.time.Range;
import com.navercorp.pinpoint.common.util.CollectionUtils;
import com.navercorp.pinpoint.web.dao.ApplicationIndexDao;
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

    private final ApplicationIndexDao applicationIndexDao;

    private final AgentInfoService agentInfoService;

    public AdminServiceImpl(ApplicationIndexDao applicationIndexDao, AgentInfoService agentInfoService) {
        this.applicationIndexDao = Objects.requireNonNull(applicationIndexDao, "applicationIndexDao");
        this.agentInfoService = Objects.requireNonNull(agentInfoService, "agentInfoService");
    }

    @Override
    public void removeApplicationName(String applicationName) {
        applicationIndexDao.deleteApplicationName(applicationName);
    }

    @Override
    public void removeAgentId(String applicationName, String agentId) {
        applicationIndexDao.deleteAgentId(applicationName, agentId);
    }

    @Override
    @Deprecated
    public void removeInactiveAgents(int durationDays) {
        if (durationDays < MIN_DURATION_DAYS_FOR_INACTIVITY) {
            throw new IllegalArgumentException("duration may not be less than " + MIN_DURATION_DAYS_FOR_INACTIVITY + " days");
        }

        List<String> applicationNames = this.applicationIndexDao.selectAllApplicationNames()
                .stream()
                .map(Application::getName)
                .distinct()
                .collect(Collectors.toList());
        Collections.shuffle(applicationNames);

        int index = 1;
        for (String applicationName: applicationNames) {
            logger.info("Cleaning {} ({}/{})", applicationName, index++, applicationNames.size());
            removeInactiveAgentInApplication(applicationName, durationDays);
        }
    }

    @Override
    public int removeInactiveAgentInApplication(String applicationName, int durationDays) {
        try {
            return removeInactiveAgentInApplication0(applicationName, durationDays);
        } catch (Exception e) {
            logger.error("Backoff to remove inactive agents in application {}", applicationName, e);
        }
        return 0;
    }

    private int removeInactiveAgentInApplication0(String applicationName, int durationDays) {
        final List<String> agentsToDelete = new ArrayList<>(100);
        int deleteCount = 0;

        final List<String> agentIds = this.applicationIndexDao.selectAgentIds(applicationName);
        for (String agentId: agentIds) {
            if (!isInactiveAgent(agentId, durationDays)) {
                continue;
            }

            agentsToDelete.add(agentId);
            deleteCount++;

            if (agentsToDelete.size() >= 100) {
                logger.info("Delete {} of {}", agentsToDelete, applicationName);
                applicationIndexDao.deleteAgentIds(Map.of(applicationName, agentsToDelete));
                agentsToDelete.clear();
            }
        }

        if (!agentsToDelete.isEmpty()) {
            logger.info("Delete {} of {}", agentsToDelete, applicationName);
            applicationIndexDao.deleteAgentIds(Map.of(applicationName, agentsToDelete));
        }

        logger.info("({}/{}) agents of {} had been cleaned up", deleteCount, agentIds.size(), applicationName);
        return deleteCount;
    }

    @Override
    public Map<String, List<Application>> getAgentIdMap() {
        Map<String, List<Application>> agentIdMap = new TreeMap<>();
        List<Application> applications = this.applicationIndexDao.selectAllApplicationNames();
        for (Application application : applications) {
            List<String> agentIds = this.applicationIndexDao.selectAgentIds(application.getName());
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
    public Map<String, List<Application>> getInactiveAgents(String applicationName, int durationDays) {
        Objects.requireNonNull(applicationName, "applicationName");

        if (durationDays < MIN_DURATION_DAYS_FOR_INACTIVITY) {
            throw new IllegalArgumentException("duration may not be less than " + MIN_DURATION_DAYS_FOR_INACTIVITY + " days");
        }
        List<String> agentIds = this.applicationIndexDao.selectAgentIds(applicationName);
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
                .filter(agentId -> isInactiveAgent(agentId, durationDays))
                .collect(Collectors.toList());
    }

    private boolean isInactiveAgent(String agentId, int durationDays) {
        long now = System.currentTimeMillis();
        Range range = Range.between(now - TimeUnit.DAYS.toMillis(durationDays), now);

        return !this.agentInfoService.isActiveAgent(agentId, range);
    }

}
