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

import com.navercorp.pinpoint.alarm.dao.AlarmHistoryV2Dao;
import com.navercorp.pinpoint.alarm.dao.AlarmNotificationOutboxDao;
import com.navercorp.pinpoint.batch.common.BatchProperties;
import com.navercorp.pinpoint.batch.job.AlarmHistoryCleanupTasklet;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.builder.TaskletStepBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Optional;

@Configuration(proxyBeanMethods = false)
public class AlarmHistoryCleanupJobConfig {
    public static final String JOB_NAME = "alarmHistoryCleanupJob";

    @Bean
    public Job alarmHistoryCleanupJob(JobRepository jobRepository,
                                      Step alarmHistoryCleanupStep,
                                      @Qualifier("jobFailListener") JobExecutionListener jobFailListener) {
        return new JobBuilder(JOB_NAME, jobRepository)
                .start(alarmHistoryCleanupStep)
                .listener(jobFailListener)
                .build();
    }

    @Bean
    public Step alarmHistoryCleanupStep(JobRepository jobRepository,
                                        PlatformTransactionManager transactionManager,
                                        AlarmHistoryCleanupTasklet alarmHistoryCleanupTasklet,
                                        @Qualifier("cleanupStepCustomListener") Optional<StepExecutionListener> stepExecutionListeners) {
        TaskletStepBuilder builder = new StepBuilder("alarmHistoryCleanupStep", jobRepository)
                .tasklet(alarmHistoryCleanupTasklet, transactionManager);
        stepExecutionListeners.ifPresent(builder::listener);

        return builder.build();
    }

    @Bean
    public AlarmHistoryCleanupTasklet alarmHistoryCleanupTasklet(AlarmNotificationOutboxDao alarmNotificationOutboxDao,
                                                                 AlarmHistoryV2Dao alarmHistoryV2Dao,
                                                                 BatchProperties batchProperties) {
        return new AlarmHistoryCleanupTasklet(alarmNotificationOutboxDao, alarmHistoryV2Dao,
                batchProperties.getAlarmHistoryCleanupExpirationDays(),
                batchProperties.getAlarmHistoryCleanupChunkSize());
    }
}
