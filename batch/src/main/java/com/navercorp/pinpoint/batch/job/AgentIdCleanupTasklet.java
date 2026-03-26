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
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class AgentIdCleanupTasklet implements Tasklet {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private static final int UNDEFINED_SERVICE_TYPE_CODE = -1;

    private final AgentIdDao agentIdDao;
    private final ApplicationDao applicationDao;
    private final MapAgentResponseDao mapAgentResponseDao;

    private final boolean dryRun;
    private final int inactiveDays;
    private final int fetchSize;
    private final int maxIteration;
    private final Set<Integer> statisticsCheckServiceTypeCodes;

    private final long baseTimestamp;

    public AgentIdCleanupTasklet(
            AgentIdDao agentIdDao,
            ApplicationDao applicationDao,
            MapAgentResponseDao mapAgentResponseDao,
            boolean dryRun,
            long baseTimestamp,
            int inactiveDays,
            int fetchSize,
            int maxIteration,
            Set<Integer> statisticsCheckServiceTypeCodes
    ) {
        this.agentIdDao = Objects.requireNonNull(agentIdDao, "agentIdDao");
        this.applicationDao = Objects.requireNonNull(applicationDao, "applicationDao");
        this.mapAgentResponseDao = Objects.requireNonNull(mapAgentResponseDao, "mapAgentResponseDao");
        this.dryRun = dryRun;
        this.baseTimestamp = baseTimestamp;
        this.inactiveDays = inactiveDays;
        this.fetchSize = fetchSize;
        this.maxIteration = maxIteration;
        this.statisticsCheckServiceTypeCodes = Set.copyOf(statisticsCheckServiceTypeCodes);
    }

    @Override
    public RepeatStatus execute(@NonNull StepContribution stepContribution, @NonNull ChunkContext chunkContext) {
        final long cleanThreshold = baseTimestamp - Duration.ofDays(inactiveDays).toMillis();
        final TimeWindow activeTimeWindow = new TimeWindow(Range.between(cleanThreshold, baseTimestamp));

        AgentIdEntry lastScanValue = null;
        StatisticsContext statisticsContext = new StatisticsContext();
        for (int iteration = 0; iteration < maxIteration; iteration++) {
            List<AgentIdEntry> entryList = agentIdDao.getInactiveAgentIdEntry(cleanThreshold, fetchSize, lastScanValue);
            if (entryList.isEmpty()) {
                return RepeatStatus.FINISHED;
            }

            lastScanValue = entryList.get(entryList.size() - 1);
            logger.info("scan agentIds. count={}, last={}", entryList.size(), lastScanValue);

            List<AgentIdEntry> deleteTargets = entryList.stream()
                    .filter(entry -> isDeleteTarget(entry, statisticsContext, cleanThreshold, activeTimeWindow))
                    .toList();
            deleteAgentEntries(deleteTargets);

            if (entryList.size() < fetchSize) {
                return RepeatStatus.FINISHED;
            }
        }

        logger.warn("reached maxIteration. maxIteration={}, lastScanValue={}", maxIteration, lastScanValue);
        return RepeatStatus.FINISHED;
    }


    private boolean isDeleteTarget(AgentIdEntry entry, StatisticsContext statisticsContext, long cleanThreshold, TimeWindow activeTimeWindow) {
        if (entry.getAgentStartTime() >= cleanThreshold) {
            return false;
        }

        if (entry.getServiceTypeCode() == UNDEFINED_SERVICE_TYPE_CODE || statisticsCheckServiceTypeCodes.contains(entry.getServiceTypeCode())) {
            Set<String> activeAgentIds = loadStatisticsAgentIds(entry, statisticsContext, activeTimeWindow);
            if (activeAgentIds != null) {
                if (activeAgentIds.contains(entry.getAgentId())) {
                    if (statisticsContext.isDuplicateAgentId(entry.getAgentId())) {
                        logger.debug("older instance. delete, {}", entry);
                        return true;
                    }
                    logger.debug("found in statistics. keep, {}", entry);
                    return false;
                }
                logger.debug("not in statistics. delete, {}", entry);
                return true;
            }
        }

        long stateTimestamp = entry.getCurrentStateTimestamp();
        if (stateTimestamp == 0 || stateTimestamp > cleanThreshold) {
            logger.warn("Entry with invalid Timestamp, {}", entry);
            return false;
        }
        return true;
    }

    /**
     * Returns cached statistics agentIds for the entry's application.
     * Returns null if the entry's serviceType is not a statistics-check type.
     */
    private Set<String> loadStatisticsAgentIds(AgentIdEntry entry, StatisticsContext statisticsContext, TimeWindow activeTimeWindow) {
        Application application = entry.getApplication();
        if (statisticsContext.isSameApplication(application)) {
            return statisticsContext.activeAgentIds;
        }
        Set<String> agentIds = selectStatisticsAgentIds(entry, activeTimeWindow);
        if (agentIds != null) {
            logger.debug("statistics update. application={}, activeAgentIds={}", application, agentIds.size());
        } else {
            logger.debug("no statistics check for application={}", application);
        }
        statisticsContext.update(application, agentIds);
        return statisticsContext.activeAgentIds;
    }

    private Set<String> selectStatisticsAgentIds(AgentIdEntry entry, TimeWindow activeTimeWindow) {
        int serviceTypeCode = entry.getServiceTypeCode();
        if (statisticsCheckServiceTypeCodes.contains(serviceTypeCode)) {
            return mapAgentResponseDao.selectAgentIds(entry.getApplication(), activeTimeWindow);
        }
        if (serviceTypeCode != UNDEFINED_SERVICE_TYPE_CODE) {
            return null;
        }
        List<Application> statisticsApps = applicationDao.getApplications(entry.getService().getUid(), entry.getApplicationName()).stream()
                .filter(app -> statisticsCheckServiceTypeCodes.contains(app.getServiceTypeCode()))
                .toList();
        if (statisticsApps.isEmpty()) {
            return null;
        }
        Set<String> merged = new HashSet<>();
        for (Application app : statisticsApps) {
            merged.addAll(mapAgentResponseDao.selectAgentIds(app, activeTimeWindow));
        }
        return merged;
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


    // Caches span-based statistics check results per application, uses scan order for duplicate detection
    private static class StatisticsContext {
        private Application application = null;
        private Set<String> activeAgentIds = null; // AgentIds found in statistics — null if not a statistics-check type
        private String lastAgentId = null;         // Tracks consecutive duplicate agentIds (ordered scan)

        boolean isSameApplication(Application application) {
            return application.equals(this.application);
        }

        void update(Application application, Set<String> activeAgentIds) {
            this.application = application;
            this.activeAgentIds = activeAgentIds;
            this.lastAgentId = null;
        }

        boolean isDuplicateAgentId(String agentId) {
            if (agentId.equals(lastAgentId)) {
                return true;
            }
            lastAgentId = agentId;
            return false;
        }
    }
}



