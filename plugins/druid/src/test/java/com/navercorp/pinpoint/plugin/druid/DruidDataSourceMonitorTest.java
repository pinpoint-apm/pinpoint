package com.navercorp.pinpoint.plugin.druid;

import org.junit.Assert;
import org.junit.Test;

public class DruidDataSourceMonitorTest extends DataSourceMonitorAccessorTest {

    @Test
    public void test() {

        DruidDataSourceMonitor monitor = new DruidDataSourceMonitor(new DruidDataSourceTest());

        Assert.assertFalse(monitor.isDisabled());

        monitor.close();

        Assert.assertTrue(monitor.isDisabled());

        Assert.assertEquals(monitor.getServiceType(), DruidConstants.SERVICE_TYPE);

        Assert.assertEquals(monitor.getUrl(), null);
        Assert.assertEquals(monitor.getActiveConnectionSize(), -1);
        Assert.assertEquals(monitor.getMaxConnectionSize(), -1);
    }

}