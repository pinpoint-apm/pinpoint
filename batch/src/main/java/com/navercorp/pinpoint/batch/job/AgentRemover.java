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
import jakarta.annotation.Nonnull;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;

import java.util.List;
import java.util.Objects;

/**
 * @author youngjin.kim2
 */
public class AgentRemover implements ItemWriter<CleanTarget.TypeAgents> {

    private static final Logger logger = LogManager.getLogger(AgentRemover.class);
    private static final int BATCH_SIZE = 200;

    private final BatchApplicationIndexService batchApplicationIndexService;

    public AgentRemover(BatchApplicationIndexService batchApplicationIndexService) {
        this.batchApplicationIndexService = Objects.requireNonNull(batchApplicationIndexService, "batchApplicationIndexService");
    }

    @Override
    public void write(@Nonnull Chunk<? extends CleanTarget.TypeAgents> targets) throws Exception {
        for (CleanTarget.TypeAgents target : targets) {
            for (int i = 0; i < target.agentIds().size(); i += BATCH_SIZE) {
                int end = Math.min(i + BATCH_SIZE, target.agentIds().size());
                List<String> agentIdBatch = target.agentIds().subList(i, end);
                logger.info("Removing agents. applicationName: {}, agents: {}", target.applicationName(), agentIdBatch);
                try {
                    batchApplicationIndexService.deleteAgentIds(target.applicationName(), agentIdBatch);
                } catch (Exception e) {
                    logger.error("Failed to remove agents. applicationName: {}, agents: {}", target.applicationName(), agentIdBatch, e);
                }
            }
        }
    }
}
