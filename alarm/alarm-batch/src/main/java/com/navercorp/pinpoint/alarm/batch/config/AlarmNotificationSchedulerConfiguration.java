/*
 * Copyright 2026 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.navercorp.pinpoint.alarm.batch.config;

import com.navercorp.pinpoint.alarm.service.AlarmNotificationDispatcher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.util.Objects;

/**
 * Drains the notification outbox on its own schedule.
 *
 * <p>Separate from the evaluation job on purpose. Evaluation records the event and enqueues
 * the deliveries in one transaction; sending them is a network call that can fail, retry and
 * take as long as the far side takes. Running it here means a send failure cannot roll back
 * the event that caused it, and a slow provider cannot delay the next evaluation sweep.
 */
@Configuration(proxyBeanMethods = false)
@EnableScheduling
public class AlarmNotificationSchedulerConfiguration {

    private final AlarmNotificationDispatcher notificationDispatcher;

    public AlarmNotificationSchedulerConfiguration(AlarmNotificationDispatcher notificationDispatcher) {
        this.notificationDispatcher = Objects.requireNonNull(notificationDispatcher, "notificationDispatcher");
    }

    @Bean
    public ThreadPoolTaskScheduler alarmOutboxScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(1);
        scheduler.setThreadNamePrefix("alarm-outbox-scheduler-");
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.setAwaitTerminationSeconds(60);
        return scheduler;
    }

    @Scheduled(
            fixedDelayString = "${pinpoint.modules.batch.alarm.outbox.fixedDelayMs:5000}",
            scheduler = "alarmOutboxScheduler")
    public void dispatchAlarmNotifications() {
        notificationDispatcher.dispatch();
    }
}
