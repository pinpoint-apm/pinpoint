package com.nhn.pinpoint.web.alarm;

import junit.framework.Assert;

import org.junit.Test;

public class MainCategoryTest {

	@Test
	public void getValueTest() {
		MainCategory category = MainCategory.getValue(1);
		Assert.assertEquals(MainCategory.REQUEST_SENDED, category);
		
		category = MainCategory.getValue(2);
		Assert.assertEquals(MainCategory.REQUEST_RECEIVED, category);
	}
	
}
