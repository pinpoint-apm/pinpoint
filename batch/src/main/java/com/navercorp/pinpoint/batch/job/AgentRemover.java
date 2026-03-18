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

import com.navercorp.pinpoint.batch.vo.CleanTarget;
import com.navercorp.pinpoint.web.dao.ApplicationIndexDao;
import com.navercorp.pinpoint.web.vo.Application;
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
    private final Logger logger = LogManager.getLogger(this.getClass());

    private final ApplicationIndexDao applicationIndexDao;

    public AgentRemover(ApplicationIndexDao applicationIndexDao) {
        this.applicationIndexDao = Objects.requireNonNull(applicationIndexDao, "applicationIndexDao");
    }

    @Override
    public void write(@Nonnull Chunk<? extends CleanTarget.TypeAgents> targets) throws Exception {
        for (CleanTarget.TypeAgents target : targets) {
            logger.info("Removing agents: {}", target);
            Application application = target.application();
            try {
                batchDeleteAgentIdsV1(application.getApplicationName(), application.getServiceTypeCode(), target.agentIds());
            } catch (Exception e) {
                throw new IllegalStateException("Failed to remove agents: " + target, e);
            }
        }
    }

    private void batchDeleteAgentIdsV1(String applicationName, int serviceTypeCode, List<String> agentIds) {
        int batchSize = 200;
        for (int i = 0; i < agentIds.size(); i += batchSize) {
            int end = Math.min(i + batchSize, agentIds.size());
            List<String> agentIdBatch = agentIds.subList(i, end);
            logger.debug("Removing agents. application: {}@{}, agents: {}", applicationName, serviceTypeCode, agentIdBatch);
            applicationIndexDao.deleteAgentIds(applicationName, agentIdBatch);
        }
    }
}
