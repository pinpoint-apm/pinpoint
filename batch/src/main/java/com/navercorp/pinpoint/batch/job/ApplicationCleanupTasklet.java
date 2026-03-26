/*
 * Copyright 2026 NAVER Corp.
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

package com.navercorp.pinpoint.batch.job;

import com.navercorp.pinpoint.common.timeseries.time.Range;
import com.navercorp.pinpoint.web.dao.AgentIdDao;
import com.navercorp.pinpoint.web.dao.ApplicationDao;
import com.navercorp.pinpoint.web.service.AgentListV2Service;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.agent.AgentIdEntry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.NonNull;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class ApplicationCleanupTasklet implements Tasklet {
    private static final int DELETE_BATCH_SIZE = 2000;
    private static final int UNDEFINED_SERVICE_TYPE_CODE = -1;

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final ApplicationDao applicationDao;
    private final AgentIdDao agentIdDao;
    private final AgentListV2Service agentListV2Service;

    private final boolean dryRun;
    private final List<Integer> serviceUidList;

    private final int agentCountThreshold;
    private final int inactiveDays;
    private final int inactiveGraceDays;
    private final long baseTimestamp;
    private final long cleanupWindowMillis;
    private final Set<Integer> statisticsCheckServiceTypeCodes;
    private final Set<Integer> missingHeaderServiceTypeCodes;

    public ApplicationCleanupTasklet(
            ApplicationDao applicationDao,
            AgentIdDao agentIdDao,
            AgentListV2Service agentListV2Service,
            Boolean dryRun,
            long baseTimestamp,
            List<Integer> serviceUidList,
            int agentCountThreshold,
            int inactiveDays,
            int inactiveGraceDays,
            Set<Integer> missingHeaderServiceTypeCodes,
            Set<Integer> statisticsCheckServiceTypeCodes,
            long cleanupWindowMillis
    ) {
        this.applicationDao = Objects.requireNonNull(applicationDao, "applicationDao");
        this.agentIdDao = Objects.requireNonNull(agentIdDao, "agentIdDao");
        this.agentListV2Service = Objects.requireNonNull(agentListV2Service, "agentListV2Service");
        this.dryRun = Objects.requireNonNullElse(dryRun, Boolean.TRUE);
        this.baseTimestamp = baseTimestamp;
        this.serviceUidList = Objects.requireNonNull(serviceUidList, "serviceUidList");
        this.agentCountThreshold = agentCountThreshold;
        this.inactiveDays = inactiveDays;
        this.inactiveGraceDays = inactiveGraceDays;
        this.missingHeaderServiceTypeCodes = Objects.requireNonNull(missingHeaderServiceTypeCodes, "missingHeaderServiceTypeCodes");
        this.statisticsCheckServiceTypeCodes = Objects.requireNonNull(statisticsCheckServiceTypeCodes, "statisticsCheckServiceTypeCodes");
        this.cleanupWindowMillis = cleanupWindowMillis;
    }

    @Override
    public RepeatStatus execute(@NonNull StepContribution contribution, @NonNull ChunkContext chunkContext) {
        for (int serviceUid : serviceUidList) {
            List<Application> applications = applicationDao.getApplications(serviceUid);
            logger.info("processing service={}, applicationCount={}", serviceUid, applications.size());
            for (Application application : applications) {
                processApplication(application, baseTimestamp);
            }
        }
        return RepeatStatus.FINISHED;
    }

    private void processApplication(Application application, long baseTimestamp) {
        int serviceUid = application.getService().getUid();
        String applicationName = application.getApplicationName();
        int serviceTypeCode = application.getServiceTypeCode();

        List<AgentIdEntry> agentIdEntries = queryAgentIdEntries(serviceUid, applicationName, serviceTypeCode);

        if (agentIdEntries.isEmpty()) {
            deleteApplication(application, baseTimestamp);
            return;
        }

        if (agentIdEntries.size() > agentCountThreshold) {
            if (statisticsCheckServiceTypeCodes.contains(serviceTypeCode)) {
                List<AgentIdEntry> deleteTargets = getDeleteTargetsUsingStatisticsCheck(application, baseTimestamp, agentIdEntries);
                deleteAgentEntries(application, deleteTargets);
            } else {
                List<AgentIdEntry> deleteTargets = getDeleteTargets(application, baseTimestamp, agentIdEntries);
                deleteAgentEntries(application, deleteTargets);
            }
        }
    }

    private List<AgentIdEntry> queryAgentIdEntries(int serviceUid, String applicationName, int serviceTypeCode) {
        List<AgentIdEntry> entries = agentIdDao.getAgentIdEntry(serviceUid, applicationName, serviceTypeCode);
        if (!missingHeaderServiceTypeCodes.contains(serviceTypeCode)) {
            return entries;
        }
        List<AgentIdEntry> undefinedEntries = agentIdDao.getAgentIdEntry(serviceUid, applicationName, UNDEFINED_SERVICE_TYPE_CODE);
        List<AgentIdEntry> combined = new ArrayList<>(entries.size() + undefinedEntries.size());
        combined.addAll(entries);
        combined.addAll(undefinedEntries);
        return combined;
    }

    private List<AgentIdEntry> getDeleteTargetsUsingStatisticsCheck(Application application, long baseTimestamp, List<AgentIdEntry> agentIdEntries) {
        int newCleanDurationDays = calculateNewCleanDurationDays(agentIdEntries.size());
        if (newCleanDurationDays >= inactiveDays) {
            return List.of();
        }
        long newThresholdTimestamp = baseTimestamp - Duration.ofDays(newCleanDurationDays).toMillis();
        Range activeRange = Range.between(newThresholdTimestamp, baseTimestamp);

        List<AgentIdEntry> deleteTargets = agentListV2Service.getInactiveAgentList(application, agentIdEntries, activeRange);
        logger.info("Cleaning up excessive agents. application={}, agentCount={}, targetCount={}, newCleanDurationDays={}", application, agentIdEntries.size(), deleteTargets.size(), newCleanDurationDays);
        return deleteTargets;
    }

    private List<AgentIdEntry> getDeleteTargets(Application application, long baseTimestamp, List<AgentIdEntry> agentIdEntries) {
        long cleanGraceTimestamp = baseTimestamp - Duration.ofDays(inactiveGraceDays).toMillis();
        int totalAgentCount = agentIdEntries.size();
        int limit = totalAgentCount - agentCountThreshold;
        if (limit <= 0) {
            logger.warn("Invalid delete limit. application={}. limit={}", application, limit);
            return List.of();
        }
        List<AgentIdEntry> sortedDeleteTargets = agentIdEntries.stream()
                .filter(entry -> entry.getAgentStartTime() < cleanGraceTimestamp && entry.getCurrentStateTimestamp() < cleanGraceTimestamp)
                .sorted(Comparator.comparingLong(AgentIdEntry::getCurrentStateTimestamp))
                .limit(limit)
                .toList();
        if (sortedDeleteTargets.isEmpty()) {
            logger.info("no inactive agentId found. application={}, agentCount={}, cleanGraceTimestamp={}", application, totalAgentCount, new Date(cleanGraceTimestamp));
            return List.of();
        }
        AgentIdEntry latestDeleteTarget = sortedDeleteTargets.get(sortedDeleteTargets.size() - 1);
        logger.info("Cleaning up excessive agents. application={}, agentCount={}, targetCount={}, latestTargetTimestamp={}", application, totalAgentCount, sortedDeleteTargets.size(), new Date(latestDeleteTarget.getCurrentStateTimestamp()));
        return sortedDeleteTargets;
    }

    private int calculateNewCleanDurationDays(int agentCount) {
        if (agentCount <= agentCountThreshold) {
            return inactiveDays;
        }
        double cleanDuration = cleanupWindowMillis * (agentCountThreshold / (double) agentCount);
        int calculatedDays = (int) (cleanDuration / 86400000L);
        return Math.max(inactiveGraceDays, calculatedDays);
    }

    private void deleteAgentEntries(Application application, List<AgentIdEntry> deleteTargets) {
        if (deleteTargets.isEmpty()) {
            return;
        }
        if (dryRun) {
            logger.info("dryRun=true, skip delete agentIds. application={}, deleteAgentCount={}",
                    application, deleteTargets.size());
            return;
        }
        logger.info("delete agentIds. application={}, deleteAgentCount={}", application, deleteTargets.size());
        for (int i = 0; i < deleteTargets.size(); i += DELETE_BATCH_SIZE) {
            int end = Math.min(i + DELETE_BATCH_SIZE, deleteTargets.size());
            List<AgentIdEntry> deleteTargetSublist = deleteTargets.subList(i, end);
            agentIdDao.delete(deleteTargetSublist);
        }
    }

    private void deleteApplication(Application application, long baseTimestamp) {
        if (dryRun) {
            logger.info("dryRun=true, skip delete application. application={}", application);
            return;
        }
        logger.info("delete application. application={}", application);
        applicationDao.deleteApplication(
                application.getService().getUid(),
                application.getApplicationName(),
                application.getServiceTypeCode(),
                baseTimestamp - Duration.ofHours(1).toMillis()
        );
    }

}

