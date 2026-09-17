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

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class AlarmJobSchedulerConfigurationTest {

    /**
     * The distinct {@code time} parameter is what lets the sweep run again at all: Spring Batch
     * refuses to re-run a completed JobInstance, so a fixed or absent parameter would leave
     * evaluation dead after the first sweep with nothing but a log line to show for it.
     */
    @Test
    void runAlarmJobLaunchesTheJobWithParametersThatDifferEachRun() throws Exception {
        JobLauncher jobLauncher = mock(JobLauncher.class);
        Job alarmJob = mock(Job.class);
        AlarmJobSchedulerConfiguration configuration =
                new AlarmJobSchedulerConfiguration(jobLauncher, alarmJob);

        configuration.runAlarmJob();
        Thread.sleep(2);
        configuration.runAlarmJob();

        ArgumentCaptor<JobParameters> parametersCaptor = ArgumentCaptor.forClass(JobParameters.class);
        verify(jobLauncher, times(2)).run(eq(alarmJob), parametersCaptor.capture());

        Long first = parametersCaptor.getAllValues().get(0).getLong("time");
        Long second = parametersCaptor.getAllValues().get(1).getLong("time");
        assertNotNull(first);
        assertNotNull(second);
        assertNotEquals(first, second);
    }
}
