package com.nhn.pinpoint.web.alarm;

import junit.framework.Assert;

import org.junit.Test;

import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.web.alarm.filter.AlarmFilter;
import com.nhn.pinpoint.web.alarm.vo.AlarmRuleResource;
import com.nhn.pinpoint.web.vo.Application;

public class SubCategoryTest {

	@Test
	public void getValueTest() {
		SubCategory category = SubCategory.getValue(1);
		Assert.assertEquals(SubCategory.FAIL_RATE, category);
		
		category = SubCategory.getValue(2);
		Assert.assertEquals(SubCategory.FAIL_COUNT, category);
		
		category = SubCategory.getValue(3);
		Assert.assertEquals(SubCategory.SLOW_RATE, category);

		category = SubCategory.getValue(4);
		Assert.assertEquals(SubCategory.SLOW_COUNT, category);
	}

	
	@Test
	public void checkTest() throws Exception {
		Application application = new Application("TEST", ServiceType.UNDEFINED);
		AlarmRuleResource rule = new AlarmRuleResource();
		
		AlarmFilter filter = SubCategory.FAIL_RATE.createAlarmFilter(application, MainCategory.REQUEST_SENDED, rule);
		Assert.assertNotNull(filter);
		
		filter = SubCategory.FAIL_RATE.createAlarmFilter(application, MainCategory.REQUEST_RECEIVED, rule);
		Assert.assertNull(filter);
	}
	
	
}
