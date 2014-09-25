package com.nhn.pinpoint.web.alarm;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import com.nhn.pinpoint.web.alarm.filter.AlarmCheckCountFilter;
import com.nhn.pinpoint.web.alarm.filter.AlarmCheckRatesFilter;
import com.nhn.pinpoint.web.alarm.vo.AlarmRuleResource;

public class CheckFilterTest {

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
	public void checkCountTest1() {
		SimpleCheckCountFilter simpleCheckFilter = new SimpleCheckCountFilter(30);
		simpleCheckFilter.initialize(checkCountRule);
		
		boolean isSuccess = simpleCheckFilter.check(null);
		Assert.assertFalse(isSuccess);
	}
	
	@Test
	public void checkCountTest2() {
		SimpleCheckCountFilter simpleCheckFilter = new SimpleCheckCountFilter(150);
		simpleCheckFilter.initialize(checkCountRule);
		
		boolean isSuccess = simpleCheckFilter.check(null);
		Assert.assertTrue(isSuccess);
	}

	@Test
	public void checkRateTest1() {
		SimpleCheckRateFilter simpleCheckFilter = new SimpleCheckRateFilter(30, 100);
		simpleCheckFilter.initialize(checkRateRule);
		
		boolean isSuccess = simpleCheckFilter.check(null);
		Assert.assertFalse(isSuccess);
	}

	@Test
	public void checkRateTest2() {
		SimpleCheckRateFilter simpleCheckFilter = new SimpleCheckRateFilter(70, 100);
		simpleCheckFilter.initialize(checkRateRule);
		
		boolean isSuccess = simpleCheckFilter.check(null);
		Assert.assertTrue(isSuccess);
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
