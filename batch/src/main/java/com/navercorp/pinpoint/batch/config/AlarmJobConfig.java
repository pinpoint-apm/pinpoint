/*
 * Copyright 2026 NAVER Corp.
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

import java.util.Optional;

import com.navercorp.pinpoint.batch.alarm.AlarmMessageSender;
import com.navercorp.pinpoint.batch.alarm.AlarmPartitioner;
import com.navercorp.pinpoint.batch.alarm.AlarmProcessor;
import com.navercorp.pinpoint.batch.alarm.AlarmReader;
import com.navercorp.pinpoint.batch.alarm.AlarmWriter;
import com.navercorp.pinpoint.batch.alarm.AlarmWriterInterceptor;
import com.navercorp.pinpoint.batch.alarm.CheckerRegistry;
import com.navercorp.pinpoint.batch.alarm.DataCollectorFactory;
import com.navercorp.pinpoint.batch.alarm.dao.pinot.PinotAlarmDao;
import com.navercorp.pinpoint.batch.alarm.vo.AppAlarmChecker;
import com.navercorp.pinpoint.batch.common.BatchProperties;
import com.navercorp.pinpoint.batch.common.Divider;
import com.navercorp.pinpoint.batch.alarm.dao.AlarmDao; // PinotAlarmDao용 인터페이스 import
import com.navercorp.pinpoint.batch.dao.mysql.MysqlAlarmDao;
import com.navercorp.pinpoint.batch.service.AlarmService;
import com.navercorp.pinpoint.batch.service.AlarmServiceImpl;
import com.navercorp.pinpoint.batch.service.BatchAgentService;
import com.navercorp.pinpoint.batch.service.BatchApplicationIndexService;
import com.navercorp.pinpoint.pinot.mybatis.PinotAsyncTemplate;
import com.navercorp.pinpoint.web.vo.Application;
import org.aopalliance.aop.Advice;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.aop.Advisor;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.aop.support.JdkRegexpMethodPointcut;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.core.partition.support.TaskExecutorPartitionHandler;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.TaskletStep;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.task.TaskExecutor;
import org.springframework.retry.interceptor.RetryInterceptorBuilder;
import org.springframework.retry.interceptor.RetryOperationsInterceptor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * XML(job/applicationContext-alarmJob.xml) 정의를 Java Config로 옮긴 설정.
 *
 * BatchJavaConfigModule에 의해 조건부 로드됨
 *
 * 주의: Job/Step/bean 이름이 BatchJobLauncher에서 사용하는 문자열과 동일해야 합니다.
 *
 * 참고: AlarmCheckerConfiguration, BatchPinotDaoConfiguration은
 *      AlarmJobModule에서 이미 Import하므로 여기서는 제외
 */
@Configuration(proxyBeanMethods = false)
public class AlarmJobConfig {

    // ---- job / steps

    @Bean
    public Job alarmJob(JobRepository jobRepository,
                        @Qualifier("alarmPartitionStep") Step alarmPartitionStep,
                        @Qualifier("jobFailListener") Object jobFailListener) { // jobFailListener 타입은 BatchInfrastructureConfig를 따름
        // XML에서는 jobFailListener가 참조되지만, 현재 컨텍스트(XML 인프라)에서 해당 빈을 제공합니다.
        // XML 정의: <batch:listener ref="jobFailListener"/>
        return new JobBuilder("alarmJob", jobRepository)
                .start(alarmPartitionStep)
                .listener((org.springframework.batch.core.JobExecutionListener) jobFailListener)
                .build();
    }

    @Bean(name = "alarmPartitionStep")
    public Step alarmPartitionStep(JobRepository jobRepository,
                                   @Qualifier("alarmStep") Step alarmStep,
                                   @Qualifier("alarmPartitioner") Partitioner alarmPartitioner,
                                   @Qualifier("alarmPoolTaskExecutorForPartition") TaskExecutor partitionTaskExecutor) {

        TaskExecutorPartitionHandler handler = new TaskExecutorPartitionHandler();
        handler.setTaskExecutor(partitionTaskExecutor);
        handler.setStep(alarmStep);
        handler.setGridSize(1);

        return new StepBuilder("alarmPartitionStep", jobRepository)
                .partitioner("alarmStep", alarmPartitioner)
                .partitionHandler(handler)
                .build();
    }

    @Bean(name = "alarmStep")
    public TaskletStep alarmStep(JobRepository jobRepository,
                                 @Qualifier("transactionManager") PlatformTransactionManager transactionManager, // 기본 TM 사용
                                 @Qualifier("alarmExecutor") TaskExecutor alarmExecutor,
                                 @Qualifier("reader") ItemReader<Application> reader,
                                 @Qualifier("processor") ItemProcessor<Application, AppAlarmChecker> processor,
                                 @Qualifier("writer") ItemWriter<AppAlarmChecker> writer,
                                 @Value("${alarm.worker.maxSize:2}") int throttleLimit) {

        // XML: commit-interval=1, task-executor=alarmExecutor, throttle-limit=${alarm.worker.maxSize:2}
        return new StepBuilder("alarmStep", jobRepository)
                .<Application, AppAlarmChecker>chunk(1, transactionManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .taskExecutor(alarmExecutor)
                .throttleLimit(throttleLimit)
                .build();
    }

    // ---- infrastructure beans from XML

    @Bean(name = "alarmExecutor")
    public ThreadPoolTaskExecutor alarmExecutor(
            @Value("${alarm.worker.maxSize:2}") int maxPoolSize,
            @Value("${alarm.worker.coreSize:2}") int corePoolSize) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setQueueCapacity(1024);
        executor.setWaitForTasksToCompleteOnShutdown(false);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setCorePoolSize(corePoolSize);
        executor.setThreadNamePrefix("alarm-");
        return executor;
    }

    @Bean(name = "alarmPoolTaskExecutorForPartition")
    public TaskExecutor alarmPoolTaskExecutorForPartition() {
        // XML: <task:executor ... pool-size="1"/>
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(1);
        executor.setQueueCapacity(0);
        executor.setWaitForTasksToCompleteOnShutdown(false);
        executor.setThreadNamePrefix("alarm-partition-");
        return executor;
    }

    // ---- batch beans (step scope)

    @Bean(name = "alarmPartitioner")
    public Partitioner alarmPartitioner(Optional<Divider> divider) {
        return new AlarmPartitioner(divider);
    }

    @Bean(name = "reader")
    @StepScope
    public AlarmReader alarmReader(BatchApplicationIndexService batchApplicationIndexService,
                                   com.navercorp.pinpoint.web.service.AlarmService alarmService) {
        return new AlarmReader(batchApplicationIndexService, alarmService);
    }

    @Bean(name = "processor")
    @StepScope
    public ItemProcessor<Application, AppAlarmChecker> alarmProcessor(DataCollectorFactory dataCollectorFactory,
                                                        com.navercorp.pinpoint.web.service.AlarmService alarmService,
                                                        CheckerRegistry checkerRegistry,
                                                        BatchAgentService batchAgentService) {
        return new AlarmProcessor(dataCollectorFactory, alarmService, checkerRegistry, batchAgentService);
    }

    @Bean(name = "writer")
    @StepScope
    public ItemWriter<AppAlarmChecker> alarmWriter(AlarmMessageSender alarmMessageSender,
                                     @Qualifier("batchAlarmService") AlarmService batchAlarmService,
                                     ObjectProvider<AlarmWriterInterceptor> alarmWriterInterceptor) {
        return new AlarmWriter(alarmMessageSender, batchAlarmService, alarmWriterInterceptor.getIfAvailable());
    }

    // ---- Alarm Service & DAO (from XML) ----

    @Bean
    public com.navercorp.pinpoint.batch.dao.AlarmDao batchAlarmDao(
            @Qualifier("sqlSessionTemplate") SqlSessionTemplate sqlSessionTemplate
    ) {
        return new MysqlAlarmDao(sqlSessionTemplate);
    }

    @Bean
    public AlarmService batchAlarmService(com.navercorp.pinpoint.batch.dao.AlarmDao batchAlarmDao) {
        return new AlarmServiceImpl(batchAlarmDao);
    }

    @Bean
    @Qualifier("alarmDao")
    public AlarmDao alarmDao(
            PinotAsyncTemplate pinotAsyncTemplate,
            @Qualifier("batchPinotTemplate") SqlSessionTemplate batchPinotTemplate,
            BatchProperties batchProperties
    ) {
        return new PinotAlarmDao(pinotAsyncTemplate, batchPinotTemplate, batchProperties);
    }

    @Bean(name = "retryableAdvisor")
    public RetryOperationsInterceptor retryableAdvisor() {
        return RetryInterceptorBuilder.stateless().build();
    }

    @Bean
    public Advisor pinotAlarmDaoRetryAdvisor(@Qualifier("retryableAdvisor") Advice retryAdvice) {
        JdkRegexpMethodPointcut pointcut = new JdkRegexpMethodPointcut();
        pointcut.setPatterns("com.navercorp.pinpoint.batch.alarm.dao.pinot.PinotAlarmDao.*");
        return new DefaultPointcutAdvisor(pointcut, retryAdvice);
    }
}
