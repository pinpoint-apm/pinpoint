package com.nhn.pinpoint.web.alarm;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.web.alarm.filter.AlarmCheckCountFilter;
import com.nhn.pinpoint.web.alarm.filter.AlarmCheckRatesFilter;
import com.nhn.pinpoint.web.alarm.vo.AlarmRuleResource;
import com.nhn.pinpoint.web.vo.Application;

public class AlarmJobTest {

	private final int threshold = 100;
	private final int ratesThreshold = 50;

	private AlarmRuleResource checkCountRule;
	private AlarmRuleResource checkRateRule;

	@Before
	public void setup() {
		this.checkCountRule = new AlarmRuleResource();
		checkCountRule.setThresholdRule(threshold);

		this.checkRateRule = new AlarmRuleResource();
		checkRateRule.setThresholdRule(ratesThreshold);
	}

	@Test
	public void simpleTest1() {
		int count = 50;
		int totalCount = 100;

		boolean isSuccess = execute(count, totalCount);
		
		Assert.assertFalse(isSuccess);
	}

	@Test
	public void simpleTest2() {
		int count = 150;
		int totalCount = 400;

		boolean isSuccess = execute(count, totalCount);
		
		Assert.assertFalse(isSuccess);
	}

	@Test
	public void simpleTest3() {
		int count = 150;
		int totalCount = 200;

		boolean isSuccess = execute(count, totalCount);
		
		Assert.assertTrue(isSuccess);
	}
	
	private boolean execute(int count, int totalCount) {
		Application application = new Application("TEST", ServiceType.UNDEFINED);
		DefaultAlarmJob job = new DefaultAlarmJob(application);

		SimpleCheckCountFilter checkCountFilter = new SimpleCheckCountFilter(count);
		checkCountFilter.initialize(checkCountRule);

		SimpleCheckRateFilter checkRateFilter = new SimpleCheckRateFilter(count, totalCount);
		checkRateFilter.initialize(checkRateRule);

		job.addFilter(checkCountFilter);
		job.addFilter(checkRateFilter);

		return job.execute(null);
	}

	class SimpleCheckCountFilter extends AlarmCheckCountFilter {
		private final int count;

		public SimpleCheckCountFilter(int count) {
			this.count = count;
		}

		@Override
		public boolean check(AlarmEvent event) {
			return check(count);
		}
	}

	class SimpleCheckRateFilter extends AlarmCheckRatesFilter {
		private final int count;
		private final int totalCount;

		public SimpleCheckRateFilter(int count, int totalCount) {
			this.count = count;
			this.totalCount = totalCount;
		}

		@Override
		public boolean check(AlarmEvent event) {
			return check(count, totalCount);
		}
	}

}
