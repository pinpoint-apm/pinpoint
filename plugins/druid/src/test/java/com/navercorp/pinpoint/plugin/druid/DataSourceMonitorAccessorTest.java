package com.navercorp.pinpoint.plugin.druid;

import org.junit.Test;

public class DataSourceMonitorAccessorTest implements DataSourceMonitorAccessor {

    @Override
    public void _$PINPOINT$_setDataSourceMonitor(DruidDataSourceMonitor dataSourceMonitor) {

    }

    @Override
    public DruidDataSourceMonitor _$PINPOINT$_getDataSourceMonitor() {
        return new DruidDataSourceMonitor(new DruidDataSourceTest());
    }

    @Test
    public void test() {
        DataSourceMonitorAccessorTest test = new DataSourceMonitorAccessorTest();
        test._$PINPOINT$_setDataSourceMonitor(test._$PINPOINT$_getDataSourceMonitor());
    }
}