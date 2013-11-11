package com.nhn.pinpoint.common;

import junit.framework.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServiceTypeTest {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());


    @Test
	public void testIndexable() {
		System.out.println(ServiceType.TOMCAT.isIndexable());
		System.out.println(ServiceType.BLOC.isIndexable());
		System.out.println(ServiceType.ARCUS.isIndexable());
	}

    @Test
    public void child() {
        ServiceType oracle = ServiceType.ORACLE;


    }

    @Test
    public void test() {
        ServiceType[] values = ServiceType.values();
        for (ServiceType value : values) {
            logger.debug(value.toString() + " " + value.getCode());
        }

    }

}
