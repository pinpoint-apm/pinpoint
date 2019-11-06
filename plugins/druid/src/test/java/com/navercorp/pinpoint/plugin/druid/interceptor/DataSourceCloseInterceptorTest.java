package com.navercorp.pinpoint.plugin.druid.interceptor;

import com.navercorp.pinpoint.bootstrap.plugin.monitor.DataSourceMonitor;
import com.navercorp.pinpoint.bootstrap.plugin.monitor.DataSourceMonitorRegistry;
import com.navercorp.pinpoint.plugin.druid.DruidDataSourceTest;
import org.junit.Test;

public class DataSourceCloseInterceptorTest {

    private DataSourceCloseInterceptor interceptor = new DataSourceCloseInterceptor(new DataSourceMonitorRegistry() {
        @Override
        public boolean register(DataSourceMonitor dataSourceMonitor) {
            return false;
        }

        @Override
        public boolean unregister(DataSourceMonitor dataSourceMonitor) {
            return false;
        }
    });

    @Test
    public void before() {
        interceptor.before(new DruidDataSourceTest(), null);
    }

    @Test
    public void after() {
        interceptor.after(null, null, null, null);
    }
}