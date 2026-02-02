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

package com.navercorp.pinpoint.batch.config;

import com.navercorp.pinpoint.batch.job.AgentRemover;
import com.navercorp.pinpoint.batch.job.ApplicationCleaningProcessor;
import com.navercorp.pinpoint.batch.job.ApplicationReader;
import com.navercorp.pinpoint.batch.job.ApplicationRemover;
import com.navercorp.pinpoint.batch.job.CleanTargetWriter;
import com.navercorp.pinpoint.batch.job.EmptyItemWriter;
import com.navercorp.pinpoint.batch.job.ItemListWriter;
import com.navercorp.pinpoint.batch.service.BatchAgentServiceImpl;
import com.navercorp.pinpoint.batch.service.BatchApplicationIndexService;
import com.navercorp.pinpoint.batch.service.BatchApplicationIndexServiceImpl;
import com.navercorp.pinpoint.batch.vo.CleanTarget;
import com.navercorp.pinpoint.web.service.component.ActiveAgentValidator;
import com.navercorp.pinpoint.web.vo.Application;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.SimpleStepBuilder;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * @author youngjin.kim2
 */
@Configuration(proxyBeanMethods = false)
@ComponentScan(
        basePackages = "com.navercorp.pinpoint.batch.service",
        useDefaultFilters = false,
        includeFilters = {
                @ComponentScan.Filter(
                        type = FilterType.ASSIGNABLE_TYPE,
                        classes = {
                                BatchApplicationIndexServiceImpl.class,
                                BatchAgentServiceImpl.class
                        }
                )
        }
)
public class CleanupInactiveApplicationsJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    public CleanupInactiveApplicationsJobConfig(JobRepository jobRepository,
                                                PlatformTransactionManager transactionManager) {
        this.jobRepository = Objects.requireNonNull(jobRepository, "jobRepository");
        this.transactionManager = Objects.requireNonNull(transactionManager, "transactionManager");
    }

    @Bean
    public Job cleanupInactiveApplicationsJob(
            Step cleanupInactiveApplicationsStep,
            JobExecutionListener jobFailListener
    ) {
        return new JobBuilder("cleanupInactiveApplicationsJob", jobRepository)
                .listener(jobFailListener)
                .start(cleanupInactiveApplicationsStep)
                .build();
    }

    @Bean
    public Step cleanupInactiveApplicationsStep(
            ApplicationReader itemStreamReader,
            ApplicationCleaningProcessor itemProcessor,
            ItemListWriter<CleanTarget> itemWriter,
            @Qualifier("cleanupExecutor") TaskExecutor cleanExecutor,
            @Qualifier("cleanupStepCustomListener") Optional<StepExecutionListener> stepExecutionListeners
    ) {
        SimpleStepBuilder<Application, List<CleanTarget>> builder = new StepBuilder("cleanupInactiveApplicationsStep", jobRepository)
                .<Application, List<CleanTarget>>chunk(100, transactionManager)
                .reader(itemStreamReader)
                .processor(itemProcessor)
                .writer(itemWriter)
                .taskExecutor(cleanExecutor);
        stepExecutionListeners.ifPresent(builder::listener);

        return builder
                .faultTolerant()
                .retry(Exception.class)
                .retryLimit(10)
                .skip(IllegalArgumentException.class)
                .skipLimit(10)
                .build();
    }

    @Bean
    public TaskExecutor cleanupExecutor(
            @Value("${job.cleanup.inactive.applications.worker.queueSize:1024}") int queueSize,
            @Value("${job.cleanup.inactive.applications.worker.coreSize:2}") int coreSize,
            @Value("${job.cleanup.inactive.applications.worker.maxSize:2}") int maxSize
    ) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setQueueCapacity(queueSize);
        executor.setCorePoolSize(coreSize);
        executor.setMaxPoolSize(maxSize);
        executor.setWaitForTasksToCompleteOnShutdown(false);
        executor.initialize();
        return executor;
    }

    @Bean
    public ApplicationReader applicationReader(BatchApplicationIndexService batchApplicationIndexService) {
        return new ApplicationReader(batchApplicationIndexService);
    }

    @Bean
    @StepScope
    public ApplicationCleaningProcessor applicationCleaningProcessor(
            ActiveAgentValidator activeAgentValidator,
            BatchApplicationIndexService batchApplicationIndexService,
            @Value("${job.cleanup.inactive.applications.emptydurationthreshold:P35D}") Duration emptyDurationThreshold
    ) {
        return new ApplicationCleaningProcessor(activeAgentValidator, batchApplicationIndexService, emptyDurationThreshold);
    }

    @Bean
    public ItemListWriter<CleanTarget> cleanTargetListWriter(
            @Qualifier("targetWriter") ItemWriter<CleanTarget> delegate
    ) {
        return new ItemListWriter<>(delegate);
    }

    @Bean("targetWriter")
    @ConditionalOnProperty(
            name = "job.cleanup.inactive.applications.writer",
            havingValue = "emptyItemWriter",
            matchIfMissing = true
    )
    public ItemWriter<CleanTarget> emptyTargetWriter() {
        return new EmptyItemWriter<>();
    }

    @Bean("targetWriter")
    @ConditionalOnProperty(
            name = "job.cleanup.inactive.applications.writer",
            havingValue = "cleanTargetWriter"
    )
    public ItemWriter<CleanTarget> cleanTargetWriter(
            ApplicationRemover applicationRemover,
            AgentRemover agentRemover
    ) {
        return new CleanTargetWriter(applicationRemover, agentRemover);
    }

    @Bean
    public ApplicationRemover applicationRemover(BatchApplicationIndexService batchApplicationIndexService) {
        return new ApplicationRemover(batchApplicationIndexService);
    }

    @Bean
    public AgentRemover agentRemover(BatchApplicationIndexService batchApplicationIndexService) {
        return new AgentRemover(batchApplicationIndexService);
    }
}
