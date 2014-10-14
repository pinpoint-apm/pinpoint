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

//     private final int threshold = 100;
//     private final int ratesThreshold = 50;
//
//     private AlarmRuleResource checkCountRule;
//     private AlarmRuleResource checkRateRule;
//
//     @Before
//     public void setup() {
//          this.checkCountRule = new AlarmRuleResource();
//          checkCountRule.setThresholdRule(threshold);
//
//          this.checkRateRule = new AlarmRuleResource();
//          checkRateRule.setThresholdRule(ratesThreshold);
//     }
//
//     @Test
//     public void simpleTest1() {
//          int count = 50;
//          int totalCount = 100;
//
//          boolean isSuccess = execute(count, totalCount);
//          
//          Assert.assertFalse(isSuccess);
//     }
//
//     @Test
//     public void simpleTest2() {
//          int count = 150;
//          int totalCount = 400;
//
//          boolean isSuccess = execute(count, totalCount);
//          
//          Assert.assertFalse(isSuccess);
//     }
//
//     @Test
//     public void simpleTest3() {
//          int count = 150;
//          int totalCount = 200;
//
//          boolean isSuccess = execute(count, totalCount);
//          
//          Assert.assertTrue(isSuccess);
//     }
//     
//     private boolean execute(int count, int totalCount) {
//          Application application = new Application("TEST", ServiceType.UNDEFINED);
//          DefaultAlarmJob job = new DefaultAlarmJob(application);
//
//          SimpleCheckCountFilter checkCountFilter = new SimpleCheckCountFilter(count);
//          checkCountFilter.initialize(checkCountRule);
//
//          SimpleCheckRateFilter checkRateFilter = new SimpleCheckRateFilter(count, totalCount);
//          checkRateFilter.initialize(checkRateRule);
//
//          job.addFilter(checkCountFilter);
//          job.addFilter(checkRateFilter);
//
//          return job.execute(null);
//     }
//
//     class SimpleCheckCountFilter extends AlarmCheckCountFilter {
//          private final int count;
//
//          public SimpleCheckCountFilter(int count) {
//               super(0);
//               this.count = count;
//          }
//
//          @Override
//          public void check() {
//               decideResult(count);
//          }
//     }
//
//     class SimpleCheckRateFilter extends AlarmCheckRatesFilter {
//          private final int count;
//          private final int totalCount;
//
//          public SimpleCheckRateFilter(int count, int totalCount) {
//               this.count = count;
//               this.totalCount = totalCount;
//          }
//
//          @Override
//          public void check() {
//               check(count, totalCount);
//          }
//     }

}
