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
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.builder.TaskletStepBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Configuration(proxyBeanMethods = false)
public class CleanupAgentAndApplicationJobConfig {
    public static final String JOB_NAME = "cleanupAgentAndApplicationJob";

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final JobExplorer jobExplorer;

    private final BatchProperties batchProperties;
    private final AgentProperties agentProperties;

    private static final String BASE_TIMESTAMP_KEY = "baseTimestamp";
    private static final String DRY_RUN_KEY = "dryRun";
    private static final String LAST_COMPLETED_JOB_TIME_KEY = "lastCompletedJobTime";

    public CleanupAgentAndApplicationJobConfig(JobRepository jobRepository,
                                               PlatformTransactionManager transactionManager,
                                               JobExplorer jobExplorer,
                                               BatchProperties batchProperties,
                                               AgentProperties agentProperties) {
        this.jobRepository = Objects.requireNonNull(jobRepository, "jobRepository");
        this.transactionManager = Objects.requireNonNull(transactionManager, "transactionManager");
        this.jobExplorer = Objects.requireNonNull(jobExplorer, "jobExplorer");
        this.batchProperties = Objects.requireNonNull(batchProperties, "batchProperties");
        this.agentProperties = Objects.requireNonNull(agentProperties, "agentProperties");
    }

    @Bean
    public Job agentIdCleanupJob(
            Step agentIdCleanupStep,
            Step applicationCleanupStep,
            @Value("${job.cleanup.inactive.application.job-history-search-limit:30}") int jobHistorySearchLimit
    ) {
        return new JobBuilder(JOB_NAME, jobRepository)
                .listener(beforeJobListener(jobHistorySearchLimit))
                .start(agentIdCleanupStep)
                .next(applicationCleanupStep)
                .build();
    }

    private JobExecutionListener beforeJobListener(int jobHistorySearchLimit) {
        return new JobExecutionListener() {
            @Override
            public void beforeJob(@NonNull JobExecution jobExecution) {
                String dryRunParameter = jobExecution.getJobParameters().getString("dryRun");
                boolean dryRun = dryRunParameter == null || Boolean.parseBoolean(dryRunParameter);

                Date scheduleDate = jobExecution.getJobParameters().getDate(JobParametersUtils.SCHEDULE_DATE_KEY);
                long baseTimestamp = scheduleDate != null ? scheduleDate.getTime() : System.currentTimeMillis();

                Long lastCompletedJobTime = getLastCompletedJobTime(jobHistorySearchLimit);

                jobExecution.getExecutionContext().putLong(BASE_TIMESTAMP_KEY, baseTimestamp);
                jobExecution.getExecutionContext().put(DRY_RUN_KEY, dryRun);
                if (lastCompletedJobTime != null) {
                    jobExecution.getExecutionContext().putLong(LAST_COMPLETED_JOB_TIME_KEY, lastCompletedJobTime);
                }
                logger.info("beforeJob: dryRun={}, baseTimestamp={}, lastCompletedJobTime={}", dryRun, baseTimestamp, lastCompletedJobTime);
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
            @Value("${job.cleanup.inactive.agent.fetch-size:2000}") int fetchSize,
            @Value("${job.cleanup.inactive.agent.max-iteration:100000}") int maxIteration,
            AgentIdDao agentIdDao,
            MapAgentResponseDao mapAgentResponseDao
    ) {
        int inactiveDays = Math.max(batchProperties.getCleanupAgentInactiveThresholdDays(), batchProperties.getCleanupAgentAndApplicationGraceDays());
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
            @Value("#{jobExecutionContext['" + LAST_COMPLETED_JOB_TIME_KEY + "']}") Long lastCompletedJobTime,
            @Value("${job.cleanup.inactive.application.service-uid:0}") List<Integer> serviceUidList,
            @Value("${job.cleanup.inactive.application.agent-count-threshold:2147483647}") int agentCountThreshold,
            ApplicationDao applicationDao,
            AgentIdDao agentIdDao,
            TraceIndexDao traceIndexDao,
            MapAgentResponseDao mapAgentResponseDao
    ) {
        long cleanupWindowMillis = calculateCleanupWindowMillis(baseTimestamp, lastCompletedJobTime);
        logger.info("applicationCleanupTasklet: cleanupWindowMillis={}", cleanupWindowMillis);
        return new ApplicationCleanupTasklet(
                applicationDao,
                agentIdDao,
                traceIndexDao,
                mapAgentResponseDao,
                dryRun,
                baseTimestamp,
                serviceUidList,
                agentCountThreshold,
                batchProperties.getCleanupAgentInactiveThresholdDays(),
                batchProperties.getCleanupAgentAndApplicationGraceDays(),
                agentProperties.getStatisticsCheckServiceTypeCodes(),
                cleanupWindowMillis
        );
    }

    private long calculateCleanupWindowMillis(long baseTimestamp, Long lastCompletedJobTime) {
        long maxWindowMillis = Duration.ofDays(batchProperties.getCleanupAgentInactiveThresholdDays()).toMillis();
        if (lastCompletedJobTime == null) {
            return maxWindowMillis;
        }
        long elapsed = baseTimestamp - lastCompletedJobTime;
        long graceDaysMillis = Duration.ofDays(batchProperties.getCleanupAgentAndApplicationGraceDays()).toMillis();
        return Math.min(elapsed + graceDaysMillis, maxWindowMillis);
    }

    private Long getLastCompletedJobTime(int searchLimit) {
        if (searchLimit == 0) {
            return null;
        }
        List<JobInstance> instances = jobExplorer.getJobInstances(JOB_NAME, 0, searchLimit);
        for (JobInstance instance : instances) {
            try {
                List<JobExecution> executions = jobExplorer.getJobExecutions(instance);
                for (JobExecution execution : executions) {
                    if (!ExitStatus.COMPLETED.equals(execution.getExitStatus())) {
                        continue;
                    }
                    logger.info("Found last completed job. execution={}", execution);
                    return JobParametersUtils.getScheduleTime(execution);
                }
            } catch (Exception e) {
                logger.warn("Failed to read job execution for instance={}. Skipping.", instance, e);
            }
        }
        logger.info("No previous completed job found for job={}, searchLimit={}", JOB_NAME, searchLimit);
        return null;
    }
}
