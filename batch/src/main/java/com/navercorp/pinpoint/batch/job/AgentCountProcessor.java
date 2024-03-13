/*
 * Copyright 2022 NAVER Corp.
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

import com.navercorp.pinpoint.batch.common.BatchProperties;
import com.navercorp.pinpoint.common.server.util.time.Range;
import com.navercorp.pinpoint.web.service.AgentInfoService;
import com.navercorp.pinpoint.web.service.ApplicationService;
import jakarta.annotation.Nonnull;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.batch.item.ItemProcessor;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author youngjin.kim2
 */
public class AgentCountProcessor implements ItemProcessor<UUID, Integer> {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final ApplicationService applicationService;
    private final AgentInfoService agentInfoService;
    private final long duration;

    public AgentCountProcessor(
            ApplicationService applicationService,
            AgentInfoService agentInfoService,
            BatchProperties batchProperties
    ) {
        this.applicationService = Objects.requireNonNull(applicationService, "applicationService");
        this.agentInfoService = Objects.requireNonNull(agentInfoService, "agentInfoService");

        long durationDays = batchProperties.getCleanupInactiveAgentsDurationDays();
        this.duration = TimeUnit.DAYS.toMillis(durationDays);
    }

    @Override
    public Integer process(@Nonnull UUID applicationId) {
        long localCount = applicationService.getAgents(applicationId)
                .stream()
                .filter(this::isActive)
                .count();
        logger.info("Application {} has {} agents", applicationId, localCount);
        return Math.toIntExact(localCount);
    }

    private boolean isActive(String agentId) {
        long now = System.currentTimeMillis();
        Range range = Range.between(now - duration, now);
        return agentInfoService.isActiveAgent(agentId, range);
    }
}
