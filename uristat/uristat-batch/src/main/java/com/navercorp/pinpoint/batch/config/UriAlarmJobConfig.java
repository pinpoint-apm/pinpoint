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

import com.navercorp.pinpoint.batch.alarm.AlarmMessageSender;
import com.navercorp.pinpoint.batch.alarm.UriStatAlarmProcessor;
import com.navercorp.pinpoint.batch.alarm.UriStatAlarmReader;
import com.navercorp.pinpoint.batch.alarm.UriStatAlarmWriter;
import com.navercorp.pinpoint.batch.alarm.checker.UriStatAlarmCheckerRegistry;
import com.navercorp.pinpoint.batch.alarm.collector.UriStatDataCollectorFactory;
import com.navercorp.pinpoint.batch.alarm.dao.UriStatDao;
import com.navercorp.pinpoint.pinot.alarm.PinotAlarmWriterInterceptor;
import com.navercorp.pinpoint.pinot.alarm.condition.AlarmConditionFactory;
import com.navercorp.pinpoint.pinot.alarm.dao.PinotAlarmDao;
import com.navercorp.pinpoint.pinot.alarm.service.PinotAlarmService;
import com.navercorp.pinpoint.pinot.alarm.vo.PinotAlarmKey;
import com.navercorp.pinpoint.pinot.tenant.TenantProvider;
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
import org.springframework.beans.factory.annotation.Value;
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
@ComponentScan(basePackages = {
    "com.navercorp.pinpoint.batch.alarm",
    "com.navercorp.pinpoint.batch.alarm.sender"
})
public class UriAlarmJobConfig {

    @Bean
    public Job uriAlarmJob(
            JobRepository jobRepository,
            @Qualifier("uriAlarmPartitionStep") Step uriAlarmPartitionStep,
            @Qualifier("jobFailListener") JobExecutionListener jobFailListener) {

        return new JobBuilder("uriAlarmJob", jobRepository)
                .start(uriAlarmPartitionStep)
                .listener(jobFailListener)
                .build();
    }

    @Bean
    public Step uriAlarmPartitionStep(
            JobRepository jobRepository,
            @Qualifier("uriAlarmStep") Step uriAlarmStep,
            @Qualifier("alarmPartitioner") Partitioner alarmPartitioner,
            @Qualifier("uriAlarmPoolTaskExecutorForPartition") TaskExecutor uriAlarmPoolTaskExecutorForPartition) {

        TaskExecutorPartitionHandler partitionHandler = new TaskExecutorPartitionHandler();
        partitionHandler.setStep(uriAlarmStep);
        partitionHandler.setTaskExecutor(uriAlarmPoolTaskExecutorForPartition);

        return new StepBuilder("uriAlarmPartitionStep", jobRepository)
                .partitioner("uriAlarmStep", alarmPartitioner)
                .partitionHandler(partitionHandler)
                .build();
    }

    @Bean
    public TaskletStep uriAlarmStep(
            JobRepository jobRepository,
            @Qualifier("transactionManager") PlatformTransactionManager transactionManager,
            @Qualifier("uriAlarmReader") UriStatAlarmReader uriAlarmReader,
            @Qualifier("uriAlarmProcessor") UriStatAlarmProcessor uriAlarmProcessor,
            @Qualifier("uriAlarmWriter") UriStatAlarmWriter uriAlarmWriter,
            @Qualifier("uriAlarmExecutor") TaskExecutor uriAlarmExecutor,
            @Value("${alarm.worker.maxSize:2}") int throttleLimit) {

        return new StepBuilder("uriAlarmStep", jobRepository)
                .<PinotAlarmKey, com.navercorp.pinpoint.pinot.alarm.checker.PinotAlarmCheckers>chunk(1, transactionManager)
                .reader(uriAlarmReader)
                .processor(uriAlarmProcessor)
                .writer(uriAlarmWriter)
                .taskExecutor(uriAlarmExecutor)
                .throttleLimit(throttleLimit)
                .build();
    }

    @Bean
    @StepScope
    public UriStatAlarmReader uriAlarmReader(
            TenantProvider tenantProvider,
            PinotAlarmDao alarmDao,
            UriStatDao uriStatDao) {
        return new UriStatAlarmReader(tenantProvider, alarmDao, uriStatDao);
    }

    @Bean
    @StepScope
    public UriStatAlarmProcessor uriAlarmProcessor(
            PinotAlarmService alarmService,
            UriStatDataCollectorFactory uriStatDataCollectorFactory,
            AlarmConditionFactory alarmConditionFactory) {
        return new UriStatAlarmProcessor(alarmService, uriStatDataCollectorFactory, alarmConditionFactory);
    }

    @Bean
    @StepScope
    public UriStatAlarmWriter uriAlarmWriter(
            AlarmMessageSender alarmMessageSender,
            PinotAlarmService alarmService,
            @Qualifier("pinotAlarmWriterInterceptor") Optional<PinotAlarmWriterInterceptor> pinotAlarmWriterInterceptor) {
        return new UriStatAlarmWriter(alarmMessageSender, alarmService, pinotAlarmWriterInterceptor.orElse(null));
    }

    @Bean
    public TaskExecutor uriAlarmExecutor(
            @Value("${alarm.worker.maxSize:2}") int maxPoolSize,
            @Value("${alarm.worker.coreSize:2}") int corePoolSize) {

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setQueueCapacity(1024);
        executor.setWaitForTasksToCompleteOnShutdown(false);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setCorePoolSize(corePoolSize);
        executor.setThreadNamePrefix("uriAlarm-");
        return executor;
    }

    @Bean
    public TaskExecutor uriAlarmPoolTaskExecutorForPartition() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(1);
        executor.setQueueCapacity(0);
        executor.setThreadNamePrefix("uriAlarm-partition-");
        executor.setWaitForTasksToCompleteOnShutdown(false);
        return executor;
    }
}

