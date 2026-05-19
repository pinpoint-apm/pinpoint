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
import com.navercorp.pinpoint.batch.util.JobParametersUtils;
import com.navercorp.pinpoint.common.server.config.AgentProperties;
import com.navercorp.pinpoint.web.applicationmap.dao.MapAgentResponseDao;
import com.navercorp.pinpoint.web.dao.AgentIdDao;
import com.navercorp.pinpoint.web.dao.ApplicationDao;
import com.navercorp.pinpoint.web.scatter.dao.TraceIndexDao;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.NonNull;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
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

import java.util.Date;
import java.util.Objects;
import java.util.Optional;

@Configuration(proxyBeanMethods = false)
public class CleanupAgentAndApplicationJobConfig {
    public static final String JOB_NAME = "cleanupAgentAndApplicationJob";

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    private final BatchProperties batchProperties;
    private final AgentProperties agentProperties;

    private static final String BASE_TIMESTAMP_KEY = "baseTimestamp";
    private static final String DRY_RUN_KEY = "dryRun";

    public CleanupAgentAndApplicationJobConfig(JobRepository jobRepository,
                                               PlatformTransactionManager transactionManager,
                                               BatchProperties batchProperties,
                                               AgentProperties agentProperties) {
        this.jobRepository = Objects.requireNonNull(jobRepository, "jobRepository");
        this.transactionManager = Objects.requireNonNull(transactionManager, "transactionManager");
        this.batchProperties = Objects.requireNonNull(batchProperties, "batchProperties");
        this.agentProperties = Objects.requireNonNull(agentProperties, "agentProperties");
    }

    @Bean
    public Job agentIdCleanupJob(
            Step agentIdCleanupStep,
            Step applicationCleanupStep
    ) {
        return new JobBuilder(JOB_NAME, jobRepository)
                .listener(beforeJobListener())
                .start(agentIdCleanupStep)
                .next(applicationCleanupStep)
                .build();
    }

    private JobExecutionListener beforeJobListener() {
        return new JobExecutionListener() {
            @Override
            public void beforeJob(@NonNull JobExecution jobExecution) {
                String dryRunParameter = jobExecution.getJobParameters().getString("dryRun");
                boolean dryRun = dryRunParameter == null || Boolean.parseBoolean(dryRunParameter);

                Date scheduleDate = jobExecution.getJobParameters().getDate(JobParametersUtils.SCHEDULE_DATE_KEY);
                long baseTimestamp = scheduleDate != null ? scheduleDate.getTime() : System.currentTimeMillis();

                jobExecution.getExecutionContext().putLong(BASE_TIMESTAMP_KEY, baseTimestamp);
                jobExecution.getExecutionContext().put(DRY_RUN_KEY, dryRun);
                logger.info("beforeJob: dryRun={}, baseTimestamp={}", dryRun, baseTimestamp);
            }

            @Override
            public void afterJob(@NonNull JobExecution jobExecution) {
                Boolean isDryRun = (Boolean) jobExecution.getExecutionContext().get(DRY_RUN_KEY);
                if (Boolean.TRUE.equals(isDryRun)) {
                    jobExecution.setExitStatus(ExitStatus.NOOP);
                }
            }
        };
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
            @Value("#{jobExecutionContext['" + DRY_RUN_KEY + "']}") boolean dryRun,
            @Value("#{jobExecutionContext['" + BASE_TIMESTAMP_KEY + "']}") long baseTimestamp,
            @Value("${job.cleanup.inactive.agent-application.fetch-size:2000}") int fetchSize,
            @Value("${job.cleanup.inactive.agent-application.max-iteration:100000}") int maxIteration,
            AgentIdDao agentIdDao,
            MapAgentResponseDao mapAgentResponseDao
    ) {
        int inactiveDays = batchProperties.getCleanupAgentAndApplicationThresholdDays();
        return new AgentIdCleanupTasklet(
                agentIdDao,
                mapAgentResponseDao,
                dryRun,
                baseTimestamp,
                inactiveDays,
                fetchSize,
                maxIteration,
                agentProperties.getStatisticsCheckServiceTypeCodes()
        );
    }

    @Bean
    @StepScope
    public ApplicationCleanupTasklet applicationCleanupTasklet(
            @Value("#{jobExecutionContext['" + DRY_RUN_KEY + "']}") boolean dryRun,
            @Value("#{jobExecutionContext['" + BASE_TIMESTAMP_KEY + "']}") long baseTimestamp,
            ApplicationDao applicationDao,
            AgentIdDao agentIdDao,
            TraceIndexDao traceIndexDao,
            MapAgentResponseDao mapAgentResponseDao
    ) {
        return new ApplicationCleanupTasklet(
                applicationDao,
                agentIdDao,
                traceIndexDao,
                mapAgentResponseDao,
                dryRun,
                baseTimestamp,
                batchProperties.getCleanupAgentAndApplicationThresholdDays(),
                batchProperties.getCleanupAgentAndApplicationAgentCountThreshold(),
                batchProperties.getCleanupAgentAndApplicationGraceDays(),
                agentProperties.getStatisticsCheckServiceTypeCodes()
        );
    }
}
