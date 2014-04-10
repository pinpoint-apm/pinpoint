package com.nhn.pinpoint.thrift.io;

import junit.framework.Assert;

import org.junit.Test;

public class HeaderTBaseSerDesFactoryTest {

	@Test
	public void optionTest1() {
    	HeaderTBaseSerializer serializer = HeaderTBaseSerDesFactory.getSerializer(1);
    	Assert.assertTrue(serializer.isSafetyGurantee());
	}
	
	@Test
	public void optionTest2() {
    	HeaderTBaseSerializer serializer = HeaderTBaseSerDesFactory.getSerializer(true, 1);
    	Assert.assertTrue(serializer.isSafetyGurantee());
	}
	
	@Test
	public void optionTest() {
    	HeaderTBaseSerializer serializer = HeaderTBaseSerDesFactory.getSerializer(false, 1);
    	Assert.assertFalse(serializer.isSafetyGurantee());
	}
	
	
}
