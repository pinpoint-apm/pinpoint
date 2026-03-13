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

import com.navercorp.pinpoint.batch.util.JobParametersUtils;
import com.navercorp.pinpoint.common.timeseries.time.Range;
import com.navercorp.pinpoint.common.timeseries.window.TimeWindow;
import com.navercorp.pinpoint.web.applicationmap.dao.MapAgentResponseDao;
import com.navercorp.pinpoint.web.dao.AgentIdDao;
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
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class AgentIdCleanupTasklet implements Tasklet {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final AgentIdDao agentIdDao;
    private final MapAgentResponseDao mapAgentResponseDao;

    private final boolean dryRun;
    private final int inactiveDays;
    private final int fetchSize;
    private final int maxIteration;
    private final Set<Integer> statisticsCheckServiceTypeCodes;

    public AgentIdCleanupTasklet(
            AgentIdDao agentIdDao,
            MapAgentResponseDao mapAgentResponseDao,
            boolean dryRun,
            int inactiveDays,
            int fetchSize,
            int maxIteration,
            Set<Integer> statisticsCheckServiceTypeCodes
    ) {
        this.agentIdDao = Objects.requireNonNull(agentIdDao, "agentIdDao");
        this.mapAgentResponseDao = Objects.requireNonNull(mapAgentResponseDao, "mapAgentResponseDao");
        this.dryRun = dryRun;
        this.inactiveDays = inactiveDays;
        this.fetchSize = fetchSize;
        this.maxIteration = maxIteration;
        this.statisticsCheckServiceTypeCodes = Set.copyOf(statisticsCheckServiceTypeCodes);
    }

    @Override
    public RepeatStatus execute(@NonNull StepContribution stepContribution, @NonNull ChunkContext chunkContext) {
        final long baseTimestamp = getBaseTimestamp(chunkContext);
        final long cleanThreshold = baseTimestamp - Duration.ofDays(inactiveDays).toMillis();
        final TimeWindow activeTimeWindow = new TimeWindow(Range.between(cleanThreshold, baseTimestamp));

        AgentIdEntry lastScanValue = null;
        StatisticsValueCache statisticsCache = new StatisticsValueCache();
        for (int iteration = 0; iteration < maxIteration; iteration++) {
            List<AgentIdEntry> entryList = agentIdDao.getInactiveAgentIdEntry(cleanThreshold, fetchSize, lastScanValue);
            if (entryList.isEmpty()) {
                return RepeatStatus.FINISHED;
            }

            lastScanValue = entryList.get(entryList.size() - 1);
            logger.info("scan agentIds. count={}, last={}", entryList.size(), lastScanValue);

            List<AgentIdEntry> deleteTargets = entryList.stream()
                    .filter(entry -> isDeleteTarget(entry, statisticsCache, cleanThreshold, activeTimeWindow))
                    .toList();
            deleteAgentEntries(deleteTargets);

            if (entryList.size() < fetchSize) {
                return RepeatStatus.FINISHED;
            }
        }

        logger.warn("reached maxIteration. maxIteration={}, lastScanValue={}", maxIteration, lastScanValue);
        return RepeatStatus.FINISHED;
    }

    private long getBaseTimestamp(@NonNull ChunkContext chunkContext) {
        Date scheduleDate = JobParametersUtils.getScheduleDate(chunkContext);
        if (scheduleDate == null) {
            return System.currentTimeMillis();
        } else {
            return scheduleDate.getTime();
        }
    }

    private boolean isDeleteTarget(AgentIdEntry entry, StatisticsValueCache statisticsCache, long cleanThreshold, TimeWindow activeTimeWindow) {
        if (entry.getAgentStartTime() >= cleanThreshold) {
            return false;
        }
        if (statisticsCheckServiceTypeCodes.contains(entry.getServiceTypeCode())) {
            Application application = entry.getApplication();
            if (statisticsCache.key == null || !statisticsCache.key.equals(application)) {
                statisticsCache.key = application;
                statisticsCache.agentIds = mapAgentResponseDao.selectAgentIds(application, activeTimeWindow);
            }
            if (statisticsCache.agentIds.contains(entry.getAgentId())) {
                return false;
            }
            logger.debug("not found in statistics. delete target, {}", entry);
            return true;
        }

        long stateTimestamp = entry.getCurrentStateTimestamp();
        if (stateTimestamp == 0 || stateTimestamp > cleanThreshold) {
            logger.warn("Entry with invalid Timestamp, {}", entry);
            return false;
        }
        return true;
    }

    private void deleteAgentEntries(List<AgentIdEntry> deleteTargets) {
        if (dryRun) {
            logger.info("dryRun=true, skip delete agentIds. count={}", deleteTargets.size());
            return;
        }
        if (deleteTargets.isEmpty()) {
            return;
        }
        logger.info("delete agentIds. count={}", deleteTargets.size());
        agentIdDao.delete(deleteTargets);
    }

    private static class StatisticsValueCache {
        private Application key = null;
        private Set<String> agentIds = Set.of();
    }
}



