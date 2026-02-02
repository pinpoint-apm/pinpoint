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

package com.navercorp.pinpoint.batch.job;

import com.navercorp.pinpoint.batch.service.BatchApplicationIndexService;
import com.navercorp.pinpoint.batch.vo.CleanTarget;
import com.navercorp.pinpoint.common.timeseries.time.Range;
import com.navercorp.pinpoint.web.service.component.ActiveAgentValidator;
import com.navercorp.pinpoint.web.vo.Application;
import jakarta.annotation.Nonnull;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Value;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author youngjin.kim2
 */
public class ApplicationCleaningProcessor implements ItemProcessor<Application, List<CleanTarget>> {

    private static final Logger logger = LogManager.getLogger(ApplicationCleaningProcessor.class);

    private final ActiveAgentValidator activeAgentValidator;
    private final BatchApplicationIndexService batchApplicationIndexService;
    private final Duration emptyDurationThreshold;

    public ApplicationCleaningProcessor(
            ActiveAgentValidator activeAgentValidator,
            BatchApplicationIndexService batchApplicationIndexService,
            @Value("${job.cleanup.inactive.applications.emptydurationthreshold:P35D}") Duration emptyDurationThreshold
    ) {
        this.activeAgentValidator = Objects.requireNonNull(activeAgentValidator, "activeAgentValidator");
        this.batchApplicationIndexService = Objects.requireNonNull(batchApplicationIndexService, "applicationService");
        this.emptyDurationThreshold = Objects.requireNonNull(emptyDurationThreshold, "emptyDurationThreshold");
    }

    @Override
    public List<CleanTarget> process(@Nonnull Application application) throws Exception {
        logger.info("Processing application: {}", application);
        boolean removeApplication = false;
        Range range = getRange();
        long timeBoundary = range.getFrom();
        List<CleanTarget> targets = new ArrayList<>(2);

        // Find inactive agent from old agents
        List<String> oldAgentIds = getOldAgents(application, timeBoundary);
        List<String> targetAgentIds = new ArrayList<>(oldAgentIds.size());
        for (String agentId : oldAgentIds) {
            if (isAgentTarget(agentId, range)) {
                targetAgentIds.add(agentId);
            }
        }
        if (!targetAgentIds.isEmpty()) {
            targets.add(new CleanTarget.TypeAgents(application, targetAgentIds));
        }

        // Find empty application
        if (oldAgentIds.size() == targetAgentIds.size()) {
            if (getAllAgents(application).size() == targetAgentIds.size()) {
                targets.add(new CleanTarget.TypeApplication(application));
                removeApplication = true;
            }
        }

        if (targets.isEmpty()) {
            return null;
        }
        logger.info("Cleaning application {}, remove application: {}, agents: {}", application, removeApplication, targetAgentIds.size());
        return targets;
    }

    private boolean isAgentTarget(String agentId, Range range) {
        return !this.activeAgentValidator.isActiveAgent(agentId, range);
    }

    private List<String> getOldAgents(Application application, long maxTimestamp) {
        return this.batchApplicationIndexService.selectAgentIds(application.getName(), application.getServiceTypeCode(), maxTimestamp);
    }

    private List<String> getAllAgents(Application application) {
        return this.batchApplicationIndexService.selectAgentIds(application.getName(), application.getServiceTypeCode());
    }

    private Range getRange() {
        long now = System.currentTimeMillis();
        return Range.between(now - this.emptyDurationThreshold.toMillis(), now);
    }
}
