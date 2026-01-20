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
import com.navercorp.pinpoint.batch.common.Divider;
import com.navercorp.pinpoint.batch.job.AgentCountPartitioner;
import com.navercorp.pinpoint.batch.job.AgentCountProcessor;
import com.navercorp.pinpoint.batch.job.AgentCountReader;
import com.navercorp.pinpoint.batch.job.AgentCountWriter;
import com.navercorp.pinpoint.batch.service.BatchAgentService;
import com.navercorp.pinpoint.batch.service.BatchApplicationIndexService;
import com.navercorp.pinpoint.web.dao.AgentStatisticsDao;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.core.partition.support.TaskExecutorPartitionHandler;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.TaskletStep;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Optional;

@Configuration
@ConditionalOnProperty(
    name = "batch.use-java-config",
    havingValue = "true"
)
@ComponentScan(basePackages = "com.navercorp.pinpoint.batch.job")
public class AgentCountJobConfig {

    @Bean
    public Job agentCountJob(
            JobRepository jobRepository,
            @Qualifier("agentCountPartitionStep") Step agentCountPartitionStep,
            @Qualifier("jobFailListener") JobExecutionListener jobFailListener) {

        return new JobBuilder("agentCountJob", jobRepository)
                .start(agentCountPartitionStep)
                .listener(jobFailListener)
                .build();
    }

    @Bean
    public Step agentCountPartitionStep(
            JobRepository jobRepository,
            @Qualifier("agentCountStep") Step agentCountStep,
            @Qualifier("agentCountPartitioner") Partitioner agentCountPartitioner,
            @Qualifier("agentCountPoolTaskExecutorForPartition") TaskExecutor agentCountPoolTaskExecutorForPartition) {

        TaskExecutorPartitionHandler partitionHandler = new TaskExecutorPartitionHandler();
        partitionHandler.setStep(agentCountStep);
        partitionHandler.setTaskExecutor(agentCountPoolTaskExecutorForPartition);

        return new StepBuilder("agentCountPartitionStep", jobRepository)
                .partitioner("agentCountStep", agentCountPartitioner)
                .partitionHandler(partitionHandler)
                .build();
    }

    @Bean
    public TaskletStep agentCountStep(
            JobRepository jobRepository,
            @Qualifier("transactionManager") PlatformTransactionManager transactionManager,
            @Qualifier("agentCountReader") AgentCountReader agentCountReader,
            @Qualifier("agentCountProcessor") AgentCountProcessor agentCountProcessor,
            @Qualifier("agentCountWriter") AgentCountWriter agentCountWriter) {

        return new StepBuilder("agentCountStep", jobRepository)
                .<String, Integer>chunk(1, transactionManager)
                .reader(agentCountReader)
                .processor(agentCountProcessor)
                .writer(agentCountWriter)
                .faultTolerant()
                .retryLimit(10)
                .retry(Exception.class)
                .build();
    }

    @Bean
    public Partitioner agentCountPartitioner(@Qualifier("divider") Optional<Divider> divider) {
        return new AgentCountPartitioner(divider);
    }

    @Bean
    @StepScope
    public AgentCountReader agentCountReader(BatchApplicationIndexService batchApplicationIndexService) {
        return new AgentCountReader(batchApplicationIndexService);
    }

    @Bean
    @StepScope
    public AgentCountProcessor agentCountProcessor(
            BatchAgentService batchAgentService,
            BatchProperties batchProperties) {
        return new AgentCountProcessor(batchAgentService, batchProperties);
    }

    @Bean
    @StepScope
    public AgentCountWriter agentCountWriter(AgentStatisticsDao agentStatisticsDao) {
        return new AgentCountWriter(agentStatisticsDao);
    }

    @Bean
    public TaskExecutor agentCountPoolTaskExecutorForPartition() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(1);
        executor.setQueueCapacity(0);
        executor.setThreadNamePrefix("agentCount-partition-");
        executor.setWaitForTasksToCompleteOnShutdown(false);
        return executor;
    }
}

