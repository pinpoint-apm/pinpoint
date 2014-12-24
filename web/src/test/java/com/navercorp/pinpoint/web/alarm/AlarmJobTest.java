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

package com.navercorp.pinpoint.web.alarm;

import static org.junit.Assert.assertEquals;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.context.support.GenericXmlApplicationContext;


public class AlarmJobTest {
     
     public static void main(String[] args) throws Exception{
          GenericXmlApplicationContext applicationContext = new GenericXmlApplicationContext("/applicationContext-test.xml");
          JobLauncherTestUtils testLauncher = applicationContext.getBean(JobLauncherTestUtils.class);
          
          JobExecution jobExecution = testLauncher.launchJob(getParameters());
          BatchStatus status = jobExecution.getStatus();
          assertEquals(BatchStatus.COMPLETED, status);
          
          applicationContext.close();
     }

     private static JobParameters getParameters() {
          Map<String, JobParameter> parameters = new HashMap<String, JobParameter>();
          parameters.put("schedule.scheduledFireTime", new JobParameter(new Date()));
          return new JobParameters(parameters);
     }
}
