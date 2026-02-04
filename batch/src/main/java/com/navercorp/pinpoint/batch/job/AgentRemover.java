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
import com.navercorp.pinpoint.web.vo.Application;
import jakarta.annotation.Nonnull;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;

import java.util.Objects;

/**
 * @author youngjin.kim2
 */
public class AgentRemover implements ItemWriter<CleanTarget.TypeAgents> {

    private final BatchApplicationIndexService batchApplicationIndexService;

    public AgentRemover(BatchApplicationIndexService batchApplicationIndexService) {
        this.batchApplicationIndexService = Objects.requireNonNull(batchApplicationIndexService, "batchApplicationIndexService");
    }

    @Override
    public void write(@Nonnull Chunk<? extends CleanTarget.TypeAgents> targets) throws Exception {
        for (CleanTarget.TypeAgents target : targets) {
            Application application = target.application();
            batchApplicationIndexService.deleteAgentIds(application.getApplicationName(), application.getServiceTypeCode(), target.agentIds());
        }
    }
}
