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

import com.navercorp.pinpoint.batch.service.BatchAgentService;
import com.navercorp.pinpoint.batch.service.BatchApplicationService;
import com.navercorp.pinpoint.batch.vo.CleanTarget;
import com.navercorp.pinpoint.common.server.cluster.ClusterKey;
import com.navercorp.pinpoint.common.server.util.time.Range;
import jakarta.annotation.Nonnull;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.batch.item.ItemProcessor;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * @author youngjin.kim2
 */
public class ApplicationCleaningProcessor implements ItemProcessor<UUID, List<CleanTarget>> {

    private static final Logger logger = LogManager.getLogger(ApplicationCleaningProcessor.class);

    private final BatchAgentService agentService;
    private final BatchApplicationService batchApplicationService;
    private final Duration emptyDurationThreshold;

    public ApplicationCleaningProcessor(
            BatchAgentService agentService,
            BatchApplicationService batchApplicationService,
            Duration emptyDurationThreshold
    ) {
        this.agentService = Objects.requireNonNull(agentService, "agentService");
        this.batchApplicationService = Objects.requireNonNull(batchApplicationService, "batchApplicationService");
        this.emptyDurationThreshold = Objects.requireNonNull(emptyDurationThreshold, "emptyDurationThreshold");
    }

    @Override
    public List<CleanTarget> process(@Nonnull UUID applicationId) throws Exception {
        logger.info("Processing application: {}", applicationId);

        Range range = getRange();
        List<String> agentIds = getAgents(applicationId);
        List<CleanTarget> targets = new ArrayList<>(agentIds.size() + 1);

        for (String agentId: agentIds) {
            if (isAgentTarget(agentId, range)) {
                String agentKey = ClusterKey.compose(applicationId.toString(), agentId, -1);
                targets.add(new CleanTarget(CleanTarget.TYPE_AGENT, agentKey));
            }
        }

        if (targets.size() == agentIds.size() && isApplicationTarget(applicationId)) {
            targets.add(new CleanTarget(CleanTarget.TYPE_APPLICATION, applicationId.toString()));
        }

        if (targets.isEmpty()) {
            return null;
        }

        logger.info("Cleaning application {}: {}", applicationId, targets);
        return targets;
    }

    private boolean isApplicationTarget(UUID applicationId) {
        return !this.batchApplicationService.isActive(applicationId, this.emptyDurationThreshold);
    }

    private boolean isAgentTarget(String agentId, Range range) {
        return !this.agentService.isActive(agentId, range);
    }

    private List<String> getAgents(UUID applicationId) {
        return this.agentService.getIds(applicationId);
    }

    private Range getRange() {
        long now = System.currentTimeMillis();
        return Range.between(now - this.emptyDurationThreshold.toMillis(), now);
    }
}
