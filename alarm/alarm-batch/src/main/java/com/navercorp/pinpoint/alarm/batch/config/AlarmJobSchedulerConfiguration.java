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

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.util.Objects;

/**
 * Launches the evaluation job on a fixed delay.
 *
 * <p>Fixed delay rather than a fixed rate, on a single thread: a sweep that runs long must
 * not have the next one start on top of it, since both would read the same due rules.
 *
 * <p>One process per set of data sources, too. A sweep asks only for the data sources this
 * process has services for, so two processes evaluating different sets never read the same
 * rule. Two that share a set do: the due-rule query takes no claim, so the second instance
 * reads the same rules and evaluates them again. What that cannot do is notify twice: every
 * event is recorded under a row lock on the rule, and whether to enqueue is decided from the
 * state read inside that lock, so the second writer sees the first one's enqueue and
 * suppresses its own. What it does cost is one backend query per rule per instance, and an
 * ordering the lock does not fix -- an evaluation that started earlier can take the lock later
 * and store its older result over a newer one. Running more than one instance over the same
 * data sources therefore means claiming due rules, and fencing the state write against a
 * superseded evaluation; raising the replica count alone is not enough.
 */
@Configuration(proxyBeanMethods = false)
@EnableScheduling
public class AlarmJobSchedulerConfiguration {

    private final JobLauncher jobLauncher;
    private final Job alarmJob;

    public AlarmJobSchedulerConfiguration(JobLauncher jobLauncher, @Qualifier("alarmJob") Job alarmJob) {
        this.jobLauncher = Objects.requireNonNull(jobLauncher, "jobLauncher");
        this.alarmJob = Objects.requireNonNull(alarmJob, "alarmJob");
    }

    @Bean
    public ThreadPoolTaskScheduler alarmScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(1);
        scheduler.setThreadNamePrefix("alarm-scheduler-");
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.setAwaitTerminationSeconds(60);
        return scheduler;
    }

    @Scheduled(
            fixedDelayString = "${pinpoint.modules.batch.alarm.fixedDelayMs:10000}",
            scheduler = "alarmScheduler")
    public void runAlarmJob() throws Exception {
        // A new run needs distinct parameters; Batch refuses to re-run a completed instance.
        jobLauncher.run(alarmJob, new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis())
                .toJobParameters());
    }
}
