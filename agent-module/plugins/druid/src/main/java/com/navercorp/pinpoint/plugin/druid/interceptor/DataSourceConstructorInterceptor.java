/*
 *  Copyright 2018 NAVER Corp.
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.navercorp.pinpoint.plugin.druid.interceptor;

import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.plugin.monitor.DataSourceMonitorRegistry;
import com.navercorp.pinpoint.bootstrap.util.InterceptorUtils;
import com.navercorp.pinpoint.plugin.druid.DataSourceMonitorAccessor;
import com.navercorp.pinpoint.plugin.druid.DruidDataSourceMonitor;

/**
 * The type Data source constructor interceptor.
 *
 * @author Victor.Zxy
 * @version 1.8.1
 * @since 2017/07/21
 */
public class DataSourceConstructorInterceptor implements AroundInterceptor {

    private final DataSourceMonitorRegistry dataSourceMonitorRegistry;

    /**
     * Instantiates a new Data source constructor interceptor.
     *
     * @param dataSourceMonitorRegistry the data source monitor registry
     */
    public DataSourceConstructorInterceptor(DataSourceMonitorRegistry dataSourceMonitorRegistry) {
        this.dataSourceMonitorRegistry = dataSourceMonitorRegistry;
    }

    @Override
    public void before(Object target, Object[] args) {
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {

        if (!InterceptorUtils.isSuccess(throwable)) {
            return;
        }

        if (target instanceof DataSourceMonitorAccessor) {
            final DruidDataSourceMonitor dbcpDataSourceMonitor = new DruidDataSourceMonitor(target);
            this.dataSourceMonitorRegistry.register(dbcpDataSourceMonitor);
            ((DataSourceMonitorAccessor) target)._$PINPOINT$_setDataSourceMonitor(dbcpDataSourceMonitor);
        }
    }
}