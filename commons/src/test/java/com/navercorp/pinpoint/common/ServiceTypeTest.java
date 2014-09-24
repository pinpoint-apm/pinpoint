package com.nhn.pinpoint.common;

import junit.framework.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ServiceTypeTest {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());


    @Test
	public void testIndexable() {
		logger.debug("{}", ServiceType.TOMCAT.isIndexable());
        logger.debug("{}", ServiceType.BLOC.isIndexable());
        logger.debug("{}", ServiceType.ARCUS.isIndexable());
	}

    @Test
    public void findDesc() {
        String desc = "MYSQL";
        List<ServiceType> mysqlList = ServiceType.findDesc(desc);
        boolean find = false;
        for (ServiceType serviceType : mysqlList) {
            if(serviceType.getDesc().equals(desc)) {
                find = true;
            }
        }
        Assert.assertTrue(find);

        try {
            mysqlList.add(ServiceType.ARCUS);
            Assert.fail();
        } catch (Exception e) {
        }
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
