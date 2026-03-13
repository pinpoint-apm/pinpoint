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

package com.navercorp.pinpoint.batch.config;

import com.navercorp.pinpoint.batch.common.BatchProperties;
import com.navercorp.pinpoint.batch.job.AgentIdCleanupTasklet;
import com.navercorp.pinpoint.batch.job.ApplicationCleanupTasklet;
import com.navercorp.pinpoint.web.applicationmap.dao.MapAgentResponseDao;
import com.navercorp.pinpoint.web.dao.AgentIdDao;
import com.navercorp.pinpoint.web.dao.ApplicationDao;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.builder.TaskletStepBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Configuration(proxyBeanMethods = false)
public class CleanupAgentAndApplicationJobConfig {
    public static final String JOB_NAME = "cleanupAgentAndApplicationJob";

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    private final BatchProperties batchProperties;

    public CleanupAgentAndApplicationJobConfig(JobRepository jobRepository, PlatformTransactionManager transactionManager,
                                               BatchProperties batchProperties) {
        this.jobRepository = Objects.requireNonNull(jobRepository, "jobRepository");
        this.transactionManager = Objects.requireNonNull(transactionManager, "transactionManager");
        this.batchProperties = Objects.requireNonNull(batchProperties, "batchProperties");
    }

    @Bean
    public Job agentIdCleanupJob(
            Step agentIdCleanupStep,
            Step applicationCleanupStep
    ) {
        return new JobBuilder(JOB_NAME, jobRepository)
                .start(agentIdCleanupStep)
                .next(applicationCleanupStep)
                .build();
    }

    @Bean
    public Step agentIdCleanupStep(
            AgentIdCleanupTasklet agentIdCleanupTasklet,
            @Qualifier("cleanupStepCustomListener") Optional<StepExecutionListener> stepExecutionListeners
    ) {
        TaskletStepBuilder builder = new StepBuilder("agentIdCleanupStep", jobRepository)
                .tasklet(agentIdCleanupTasklet, transactionManager);
        stepExecutionListeners.ifPresent(builder::listener);

        return builder.build();
    }

    @Bean
    public Step applicationCleanupStep(
            ApplicationCleanupTasklet applicationCleanupTasklet,
            @Qualifier("cleanupStepCustomListener") Optional<StepExecutionListener> stepExecutionListeners
    ) {
        TaskletStepBuilder builder = new StepBuilder("applicationCleanupStep", jobRepository)
                .tasklet(applicationCleanupTasklet, transactionManager);
        stepExecutionListeners.ifPresent(builder::listener);

        return builder.build();
    }

    @Bean
    @StepScope
    public AgentIdCleanupTasklet agentIdCleanupTasklet(
            @Value("#{jobParameters['dryRun']}") String dryRunParameter,
            @Value("${job.cleanup.inactive.agent.fetch-size:1000}") int fetchSize,
            @Value("${job.cleanup.inactive.agent.max-iteration:100000}") int maxIteration,
            @Value("${job.cleanup.inactive.agent.trace-based-check-service-type-codes:1220,1400,1700}") Set<Integer> statisticsCheckServiceTypeCodes,
            AgentIdDao agentIdDao,
            MapAgentResponseDao mapAgentResponseDao
    ) {
        boolean dryRun = dryRunParameter == null || Boolean.parseBoolean(dryRunParameter);
        return new AgentIdCleanupTasklet(
                agentIdDao,
                mapAgentResponseDao,
                dryRun,
                Math.max(batchProperties.getCleanupAgentInactiveThresholdDays(), batchProperties.getCleanupAgentAndApplicationGraceDays()),
                fetchSize,
                maxIteration,
                statisticsCheckServiceTypeCodes
        );
    }

    @Bean
    @StepScope
    public ApplicationCleanupTasklet applicationCleanupTasklet(
            @Value("#{jobParameters['dryRun']}") String dryRunParameter,
            @Value("${job.cleanup.inactive.application.serviceUidList:0}") List<Integer> serviceUidList,
            @Value("${job.cleanup.inactive.application.agent-count-threshold:2147483647}") int agentCountThreshold,
            @Value("${job.cleanup.inactive.agent.trace-based-check-service-type-codes:1220,1400,1700}") Set<Integer> statisticsCheckServiceTypeCodes,
            ApplicationDao applicationDao,
            AgentIdDao agentIdDao,
            MapAgentResponseDao mapAgentResponseDao
    ) {
        boolean dryRun = dryRunParameter == null || Boolean.parseBoolean(dryRunParameter);
        return new ApplicationCleanupTasklet(
                applicationDao,
                agentIdDao,
                mapAgentResponseDao,
                dryRun,
                serviceUidList,
                agentCountThreshold,
                batchProperties.getCleanupAgentInactiveThresholdDays(),
                batchProperties.getCleanupAgentAndApplicationGraceDays(),
                statisticsCheckServiceTypeCodes
        );
    }
}


