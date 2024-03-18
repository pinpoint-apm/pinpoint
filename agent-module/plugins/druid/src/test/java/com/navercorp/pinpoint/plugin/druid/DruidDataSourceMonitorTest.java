package com.navercorp.pinpoint.plugin.druid;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DruidDataSourceMonitorTest extends DataSourceMonitorAccessorTest {

    @Test
    public void test() {

        DruidDataSourceMonitor monitor = new DruidDataSourceMonitor(new DruidDataSourceTest());

        Assertions.assertFalse(monitor.isDisabled());

        monitor.close();

        Assertions.assertTrue(monitor.isDisabled());

        Assertions.assertEquals(monitor.getServiceType(), DruidConstants.SERVICE_TYPE);

        Assertions.assertEquals(monitor.getUrl(), null);
        Assertions.assertEquals(monitor.getActiveConnectionSize(), -1);
        Assertions.assertEquals(monitor.getMaxConnectionSize(), -1);
    }

}