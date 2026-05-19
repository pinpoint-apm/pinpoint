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

import com.navercorp.pinpoint.common.server.uid.ServiceUid;
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
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class ApplicationCleanupTasklet implements Tasklet {
    private static final int DELETE_BATCH_SIZE = 2000;
    private static final int STATISTICS_CHECK_RANGE_SPLIT_COUNT = 5;
    private final Logger logger = LogManager.getLogger(this.getClass());

    private final ApplicationDao applicationDao;
    private final AgentIdDao agentIdDao;
    private final TraceIndexDao traceIndexDao;
    private final MapAgentResponseDao mapAgentResponseDao;

    private final boolean dryRun;
    private final long baseTimestamp;
    private final int inactiveDays;

    // Experimental: additional retention reduction for applications exceeding agentCountThreshold
    private final int agentCountThreshold;
    private final int inactiveGraceDays;
    private final Set<Integer> statisticsCheckServiceTypeCodes;

    private boolean traceIndexAvailable = true;

    public ApplicationCleanupTasklet(
            ApplicationDao applicationDao,
            AgentIdDao agentIdDao,
            TraceIndexDao traceIndexDao,
            MapAgentResponseDao mapAgentResponseDao,
            Boolean dryRun,
            long baseTimestamp,
            int inactiveDays,
            int agentCountThreshold,
            int inactiveGraceDays,
            Set<Integer> statisticsCheckServiceTypeCodes
    ) {
        this.applicationDao = Objects.requireNonNull(applicationDao, "applicationDao");
        this.agentIdDao = Objects.requireNonNull(agentIdDao, "agentIdDao");
        this.traceIndexDao = Objects.requireNonNull(traceIndexDao, "traceIndexDao");
        this.mapAgentResponseDao = Objects.requireNonNull(mapAgentResponseDao, "mapAgentResponseDao");
        this.dryRun = Objects.requireNonNullElse(dryRun, Boolean.TRUE);
        this.baseTimestamp = baseTimestamp;
        this.inactiveDays = inactiveDays;
        this.agentCountThreshold = agentCountThreshold;
        this.inactiveGraceDays = inactiveGraceDays;
        this.statisticsCheckServiceTypeCodes = Objects.requireNonNull(statisticsCheckServiceTypeCodes, "statisticsCheckServiceTypeCodes");
        if (this.agentCountThreshold != Integer.MAX_VALUE) {
            logger.info("ExperimentalAgentCountThreshold agentCountThreshold={}", this.agentCountThreshold);
        }
    }

    @Override
    public RepeatStatus execute(@NonNull StepContribution contribution, @NonNull ChunkContext chunkContext) {
        List<Application> applications = applicationDao.getApplications(ServiceUid.DEFAULT_SERVICE_UID_CODE);
        logger.info("processing service={}, applicationCount={}", ServiceUid.DEFAULT_SERVICE_UID_CODE, applications.size());
        for (Application application : applications) {
            processApplication(application, baseTimestamp);
        }
        return RepeatStatus.FINISHED;
    }

    private void processApplication(Application application, long baseTimestamp) {
        int serviceUid = application.getService().getUid();
        String applicationName = application.getApplicationName();
        int serviceTypeCode = application.getServiceTypeCode();
        int agentCount = agentIdDao.countAgentIdEntry(serviceUid, applicationName, serviceTypeCode);

        logger.info("application={}, agentCount={}", application, agentCount);
        if (agentCount == 0) {
            if (!hasActiveTraceData(application, baseTimestamp)) {
                deleteApplication(application, baseTimestamp);
            }
            return;
        }

        // delete agentIds
        if (agentCount > agentCountThreshold) {
            List<AgentIdEntry> agentIdEntries = agentIdDao.getAgentIdEntry(serviceUid, applicationName, serviceTypeCode);
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
        int excessCount = agentIdEntries.size() - agentCountThreshold;

        long alignedBaseTimestamp = baseTimestamp - (baseTimestamp % Duration.ofDays(1).toMillis());
        long alignedGraceTimestamp = alignedBaseTimestamp - Duration.ofDays(inactiveGraceDays).toMillis();

        // check grace range
        Set<String> keepSet = new HashSet<>(mapAgentResponseDao.selectAgentIds(application,
                new TimeWindow(Range.between(alignedGraceTimestamp, alignedBaseTimestamp))));
        int regularSliceDays = (inactiveDays - inactiveGraceDays) / STATISTICS_CHECK_RANGE_SPLIT_COUNT;
        List<AgentIdEntry> deleteTargets = collectCandidates(agentIdEntries, keepSet, alignedGraceTimestamp);
        long lastSliceStart = alignedGraceTimestamp;
        // check older range slice by slice (newest first); commit deleteTargets only when still above excessCount
        if (regularSliceDays > 0 && deleteTargets.size() > excessCount) {
            long regularSliceMillis = Duration.ofDays(regularSliceDays).toMillis();
            for (int i = 0; i < STATISTICS_CHECK_RANGE_SPLIT_COUNT; i++) {
                TimeWindow sliceWindow = buildSliceWindow(alignedGraceTimestamp, regularSliceMillis, i);
                keepSet.addAll(mapAgentResponseDao.selectAgentIds(application, sliceWindow));
                List<AgentIdEntry> newDeleteTargets = collectCandidates(agentIdEntries, keepSet, sliceWindow.getWindowRange().getFrom());
                if (newDeleteTargets.size() <= excessCount) {
                    break;
                }
                deleteTargets = newDeleteTargets;
                lastSliceStart = sliceWindow.getWindowRange().getFrom();
            }
        }
        logger.info("Cleaning up excessive agents. application={}, agentCount={}, targetCount={}, lastSliceStart={}",
                application, agentIdEntries.size(), deleteTargets.size(), new Date(lastSliceStart));
        return deleteTargets;
    }

    private TimeWindow buildSliceWindow(long alignedGraceTimestamp, long regularSliceMillis, int sliceIndex) {
        long sliceStart = alignedGraceTimestamp - regularSliceMillis * (sliceIndex + 1);
        long sliceEnd = alignedGraceTimestamp - regularSliceMillis * sliceIndex;
        return new TimeWindow(Range.between(sliceStart, sliceEnd));
    }

    private List<AgentIdEntry> collectCandidates(List<AgentIdEntry> agentIdEntries, Set<String> keepSet, long threshold) {
        List<AgentIdEntry> candidates = new ArrayList<>();
        String lastKeptAgentId = null;
        for (AgentIdEntry entry : agentIdEntries) {
            if (!isInactiveCandidate(entry, threshold)) {
                continue;
            }
            if (keepSet.contains(entry.getAgentId()) && !entry.getAgentId().equals(lastKeptAgentId)) {
                lastKeptAgentId = entry.getAgentId();
                continue;
            }
            candidates.add(entry);
        }
        return candidates;
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

    private boolean hasActiveTraceData(Application application, long baseTimestamp) {
        if (!traceIndexAvailable) {
            return false;
        }
        try {
            Dot dot = getTraceIndexData(application, baseTimestamp);
            if (dot != null) {
                logger.info("Skip deleting application with no agentId but has trace data. application={}, dot={}", application, dot);
                return true;
            }
            return false;
        } catch (Exception e) {
            if (ExceptionUtils.indexOfThrowable(e, TableNotFoundException.class) >= 0) {
                logger.info("TraceIndex table not found. Disabling trace check for remaining applications. message={}", e.getMessage());
                traceIndexAvailable = false;
                return false;
            }
            logger.warn("Failed to check trace data. Skip deleting application. application={}", application, e);
            return true;
        }
    }

    private Dot getTraceIndexData(Application application, long baseTimestamp) {
        Range range = Range.between(baseTimestamp - Duration.ofDays(inactiveDays).toMillis(), baseTimestamp);
        LimitedScanResult<List<Dot>> result = traceIndexDao.scanTraceScatterData(application.getService().getUid(), application.getApplicationName(), application.getServiceTypeCode(), range, 1);
        if (!result.scanData().isEmpty()) {
            return result.scanData().get(0);
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

