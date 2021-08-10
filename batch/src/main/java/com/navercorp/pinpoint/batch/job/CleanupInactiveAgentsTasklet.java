/*
 * Copyright 2020 NAVER Corp.
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

import com.navercorp.pinpoint.batch.common.BatchConfiguration;
import com.navercorp.pinpoint.web.service.AdminService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

import java.util.Objects;

/**
 * @author Taejin Koo
 */
public class CleanupInactiveAgentsTasklet implements Tasklet {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final int durationDays;

    private final AdminService adminService;

    public CleanupInactiveAgentsTasklet(BatchConfiguration batchConfiguration, AdminService adminService) {
        Objects.requireNonNull(batchConfiguration, "batchConfiguration");
        this.durationDays = batchConfiguration.getCleanupInactiveAgentsDurationDays();
        this.adminService = Objects.requireNonNull(adminService, "adminService");
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        try {
            adminService.removeInactiveAgents(durationDays);
            return RepeatStatus.FINISHED;
        } catch (Exception e) {
            logger.warn("Failed to execute. message:{}", e.getMessage(), e);
            throw e;
        }
    }

}
