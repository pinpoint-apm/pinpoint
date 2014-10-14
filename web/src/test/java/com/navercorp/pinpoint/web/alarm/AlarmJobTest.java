package com.nhn.pinpoint.web.alarm;

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
