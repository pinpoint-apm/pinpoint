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
import com.navercorp.pinpoint.common.timeseries.window.TimeWindow;
import com.navercorp.pinpoint.web.applicationmap.dao.MapAgentResponseDao;
import com.navercorp.pinpoint.web.dao.AgentIdDao;
import com.navercorp.pinpoint.web.dao.ApplicationDao;
import com.navercorp.pinpoint.web.scatter.dao.TraceIndexDao;
import com.navercorp.pinpoint.web.scatter.vo.Dot;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.LimitedScanResult;
import com.navercorp.pinpoint.web.vo.agent.AgentIdEntry;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.hadoop.hbase.TableNotFoundException;
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
    private final Logger logger = LogManager.getLogger(this.getClass());

    private final ApplicationDao applicationDao;
    private final AgentIdDao agentIdDao;
    private final TraceIndexDao traceIndexDao;
    private final MapAgentResponseDao mapAgentResponseDao;

    private final boolean dryRun;
    private final List<Integer> serviceUidList;

    private final int agentCountThreshold;
    private final int inactiveDays;
    private final int inactiveGraceDays;
    private final long baseTimestamp;
    private final long cleanupWindowMillis;
    private final Set<Integer> statisticsCheckServiceTypeCodes;

    public ApplicationCleanupTasklet(
            ApplicationDao applicationDao,
            AgentIdDao agentIdDao,
            TraceIndexDao traceIndexDao,
            MapAgentResponseDao mapAgentResponseDao,
            Boolean dryRun,
            long baseTimestamp,
            List<Integer> serviceUidList,
            int agentCountThreshold,
            int inactiveDays,
            int inactiveGraceDays,
            Set<Integer> statisticsCheckServiceTypeCodes,
            long cleanupWindowMillis
    ) {
        this.applicationDao = Objects.requireNonNull(applicationDao, "applicationDao");
        this.agentIdDao = Objects.requireNonNull(agentIdDao, "agentIdDao");
        this.traceIndexDao = Objects.requireNonNull(traceIndexDao, "traceIndexDao");
        this.mapAgentResponseDao = Objects.requireNonNull(mapAgentResponseDao, "mapAgentResponseDao");
        this.dryRun = Objects.requireNonNullElse(dryRun, Boolean.TRUE);
        this.baseTimestamp = baseTimestamp;
        this.serviceUidList = Objects.requireNonNull(serviceUidList, "serviceUidList");
        this.agentCountThreshold = agentCountThreshold;
        this.inactiveDays = inactiveDays;
        this.inactiveGraceDays = inactiveGraceDays;
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

        List<AgentIdEntry> agentIdEntries = agentIdDao.getAgentIdEntry(serviceUid, applicationName, serviceTypeCode);
        if (agentIdEntries.isEmpty()) {
            Dot dot = getTraceIndexData(application, baseTimestamp);
            if (dot == null) {
                deleteApplication(application, baseTimestamp);
            } else {
                logger.info("Skip deleting application with no agentId but has trace data. application={}, dot={}", application, dot);
            }
            return;
        }

        // delete agentIds
        if (agentIdEntries.size() > agentCountThreshold) {
            if (statisticsCheckServiceTypeCodes.contains(application.getServiceTypeCode())) {
                List<AgentIdEntry> deleteTargets = getDeleteTargetsUsingStatisticsCheck(application, baseTimestamp, agentIdEntries);
                deleteAgentEntries(application, deleteTargets);
            } else {
                List<AgentIdEntry> deleteTargets = getDeleteTargets(application, baseTimestamp, agentIdEntries);
                deleteAgentEntries(application, deleteTargets);
            }
        }
    }

    private List<AgentIdEntry> getDeleteTargetsUsingStatisticsCheck(Application application, long baseTimestamp, List<AgentIdEntry> agentIdEntries) {
        int newCleanDurationDays = calculateNewCleanDurationDays(agentIdEntries.size());
        if (newCleanDurationDays >= inactiveDays) {
            return List.of();
        }
        long newThresholdTimestamp = baseTimestamp - Duration.ofDays(newCleanDurationDays).toMillis();
        Set<String> activeAgentIds = mapAgentResponseDao.selectAgentIds(application, new TimeWindow(Range.between(newThresholdTimestamp, baseTimestamp)));
        String lastKeptAgentId = null;
        List<AgentIdEntry> deleteTargets = new ArrayList<>();
        for (AgentIdEntry entry : agentIdEntries) {
            if (!isInactiveCandidate(entry, newThresholdTimestamp)) {
                continue;
            }
            if (activeAgentIds.contains(entry.getAgentId()) && !entry.getAgentId().equals(lastKeptAgentId)) {
                lastKeptAgentId = entry.getAgentId();
                continue;
            }
            deleteTargets.add(entry);
        }
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
                .filter(entry -> isInactiveCandidate(entry, cleanGraceTimestamp))
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

    private boolean isInactiveCandidate(AgentIdEntry entry, long threshold) {
        return entry.getAgentStartTime() < threshold
                && entry.getCurrentStateTimestamp() < threshold;
    }

    private int calculateNewCleanDurationDays(int agentCount) {
        if (agentCount <= agentCountThreshold) {
            return inactiveDays;
        }
        double cleanDurationMillis = cleanupWindowMillis * (agentCountThreshold / (double) agentCount);
        int calculatedDays = (int) (cleanDurationMillis / Duration.ofDays(1).toMillis());
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

    private Dot getTraceIndexData(Application application, long baseTimestamp) {
        Range range = Range.between(baseTimestamp - Duration.ofDays(inactiveDays).toMillis(), baseTimestamp);
        try {
            LimitedScanResult<List<Dot>> result = traceIndexDao.scanTraceScatterData(application.getService().getUid(), application.getApplicationName(), application.getServiceTypeCode(), range, 1);
            if (!result.scanData().isEmpty()) {
                return result.scanData().get(0);
            }
        } catch (Exception e) {
            if (ExceptionUtils.indexOfThrowable(e, TableNotFoundException.class) >= 0) {
                logger.info("Table not found. application={} message={}", application, e.getMessage());
            }
            logger.warn("Failed to find dot data. application={}", application, e);
        }
        return null;
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

