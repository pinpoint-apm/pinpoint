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

package com.navercorp.pinpoint.web.batch.job;

import com.navercorp.pinpoint.web.batch.BatchConfiguration;
import com.navercorp.pinpoint.web.service.AdminService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;

/**
 * @author Taejin Koo
 */
@Deprecated
public class CleanupInactiveAgentsTasklet implements Tasklet {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private BatchConfiguration batchConfiguration;

    private int durationDays;

    @Autowired
    private AdminService adminService;


    @PostConstruct
    public void setup() {
        int cleanupInactiveAgentsDurationDays = batchConfiguration.getCleanupInactiveAgentsDurationDays();
        this.durationDays = cleanupInactiveAgentsDurationDays;
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
