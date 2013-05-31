package com.nhn.pinpoint.common;

import org.junit.Test;

public class ServiceTypeTest {

	@Test
	public void testIndexable() {
		System.out.println(ServiceType.TOMCAT.isIndexable());
		System.out.println(ServiceType.BLOC.isIndexable());
		System.out.println(ServiceType.ARCUS.isIndexable());
	}

}
