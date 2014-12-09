package com.navercorp.pinpoint.web.vo;

import com.navercorp.pinpoint.common.ServiceType;
import com.navercorp.pinpoint.web.vo.Application;

import junit.framework.Assert;

import org.junit.Test;

/**
 * @author emeroad
 */
public class ApplicationTest {
    @Test
    public void testEquals() throws Exception {
        Application one = new Application("test", ServiceType.TOMCAT);
        Application two = new Application("test", ServiceType.TOMCAT);

        Assert.assertTrue(one.equals(two));

        Assert.assertTrue(one.equals(two.getName(), two.getServiceType()));

        Assert.assertFalse(one.equals("test2", two.getServiceType()));
        Assert.assertFalse(one.equals("test", ServiceType.BLOC));
        Assert.assertFalse(one.equals("test2", ServiceType.BLOC));

    }
}
