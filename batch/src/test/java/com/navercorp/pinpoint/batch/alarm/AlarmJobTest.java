/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.batch.alarm;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.context.support.GenericXmlApplicationContext;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class AlarmJobTest {

    public static void main(String[] args) throws Exception {
        GenericXmlApplicationContext applicationContext = new GenericXmlApplicationContext("/applicationContext-test.xml");
        JobLauncherTestUtils testLauncher = applicationContext.getBean(JobLauncherTestUtils.class);

        JobExecution jobExecution = testLauncher.launchJob(getParameters());
        BatchStatus status = jobExecution.getStatus();
        assertEquals(BatchStatus.COMPLETED, status);

        applicationContext.close();
    }

    private static JobParameters getParameters() {
        JobParametersBuilder builder = new JobParametersBuilder();
        builder.addDate("schedule.scheduledFireTime", new Date());
        return builder.toJobParameters();
    }
}
