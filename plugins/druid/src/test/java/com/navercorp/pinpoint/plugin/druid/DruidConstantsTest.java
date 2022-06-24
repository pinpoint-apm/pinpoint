package com.navercorp.pinpoint.plugin.druid;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DruidConstantsTest {

    @Test
    public void test() {
        Assertions.assertEquals(DruidConstants.SCOPE, "DRUID_SCOPE");
        Assertions.assertEquals(DruidConstants.SERVICE_TYPE.getName(), "DRUID");

        Assertions.assertEquals("com.navercorp.pinpoint.plugin.druid.DataSourceMonitorAccessor", "com.navercorp.pinpoint.plugin.druid.DataSourceMonitorAccessor");
        Assertions.assertEquals("com.navercorp.pinpoint.plugin.druid.interceptor.DataSourceConstructorInterceptor", "com.navercorp.pinpoint.plugin.druid.interceptor.DataSourceConstructorInterceptor");
        Assertions.assertEquals("com.navercorp.pinpoint.plugin.druid.interceptor.DataSourceCloseInterceptor", "com.navercorp.pinpoint.plugin.druid.interceptor.DataSourceCloseInterceptor");
        Assertions.assertEquals("com.navercorp.pinpoint.plugin.druid.interceptor.DataSourceGetConnectionInterceptor", "com.navercorp.pinpoint.plugin.druid.interceptor.DataSourceGetConnectionInterceptor");
        Assertions.assertEquals("com.navercorp.pinpoint.plugin.druid.interceptor.DataSourceCloseConnectionInterceptor", "com.navercorp.pinpoint.plugin.druid.interceptor.DataSourceCloseConnectionInterceptor");

        Assertions.assertEquals(DruidConstants.PLUGIN_ENABLE, "profiler.jdbc.druid");
        Assertions.assertEquals(DruidConstants.PROFILE_CONNECTIONCLOSE_ENABLE, "profiler.jdbc.druid.connectionclose");
    }
}