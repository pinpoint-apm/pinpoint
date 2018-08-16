package com.navercorp.pinpoint.plugin.druid;

import org.junit.Assert;
import org.junit.Test;

public class DruidConstantsTest {

    @Test
    public void test() {
        Assert.assertEquals(DruidConstants.SCOPE, "DRUID_SCOPE");
        Assert.assertEquals(DruidConstants.SERVICE_TYPE.getName(), "DRUID");

        Assert.assertEquals(DruidConstants.ACCESSOR_DATASOURCE_MONITOR, "com.navercorp.pinpoint.plugin.druid.DataSourceMonitorAccessor");
        Assert.assertEquals(DruidConstants.INTERCEPTOR_CONSTRUCTOR, "com.navercorp.pinpoint.plugin.druid.interceptor.DataSourceConstructorInterceptor");
        Assert.assertEquals(DruidConstants.INTERCEPTOR_CLOSE, "com.navercorp.pinpoint.plugin.druid.interceptor.DataSourceCloseInterceptor");
        Assert.assertEquals(DruidConstants.INTERCEPTOR_GET_CONNECTION, "com.navercorp.pinpoint.plugin.druid.interceptor.DataSourceGetConnectionInterceptor");
        Assert.assertEquals(DruidConstants.INTERCEPTOR_CLOSE_CONNECTION, "com.navercorp.pinpoint.plugin.druid.interceptor.DataSourceCloseConnectionInterceptor");

        Assert.assertEquals(DruidConstants.PLUGIN_ENABLE, "profiler.jdbc.druid");
        Assert.assertEquals(DruidConstants.PROFILE_CONNECTIONCLOSE_ENABLE, "profiler.jdbc.druid.connectionclose");
    }
}