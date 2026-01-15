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

import com.navercorp.pinpoint.batch.common.BatchJobLauncher;
import com.navercorp.pinpoint.batch.common.BatchProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.CronTrigger;

/**
 * 기존 applicationContext-batch-schedule.xml 대체
 *
 * Java Configuration 기반 배치 스케줄링 설정
 * BatchJavaConfigModule에 의해 조건부 로드됨
 */
@Configuration
@EnableScheduling
public class BatchScheduleConfig implements SchedulingConfigurer {

    private final BatchJobLauncher batchJobLauncher;
    private final BatchProperties batchProperties;

    public BatchScheduleConfig(BatchJobLauncher batchJobLauncher, BatchProperties batchProperties) {
        this.batchJobLauncher = batchJobLauncher;
        this.batchProperties = batchProperties;
    }


    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.setScheduler(taskScheduler());

        // 1. Alarm Job
        taskRegistrar.addTriggerTask(
                batchJobLauncher::alarmJob,
                triggerContext -> {
                    String cron = batchProperties.getAlarmJobCron();
                    return new CronTrigger(cron).nextExecution(triggerContext);
                }
        );

        taskRegistrar.addTriggerTask(
                batchJobLauncher::agentCountJob,
                triggerContext -> {
                    String cron = batchProperties.getAgentCountJobCron();
                    return new CronTrigger(cron).nextExecution(triggerContext);
                }
        );

        taskRegistrar.addTriggerTask(
                batchJobLauncher::cleanupInactiveApplicationsJob,
                triggerContext -> {
                    String cron = batchProperties.getCleanupInactiveApplicationsJobCron();
                    return new CronTrigger(cron).nextExecution(triggerContext);
                }
        );

        taskRegistrar.addTriggerTask(
                batchJobLauncher::uriStatAlarmJob,
                triggerContext -> {
                    String cron = batchProperties.getUriStatAlarmJobCron();
                    return new CronTrigger(cron).nextExecution(triggerContext);
                }
        );
    }

    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(4);
        scheduler.setThreadNamePrefix("batch-scheduler-");
        scheduler.initialize();
        return scheduler;
    }
}
