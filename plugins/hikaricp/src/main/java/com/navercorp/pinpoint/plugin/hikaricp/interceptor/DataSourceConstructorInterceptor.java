/*
 * Copyright 2017 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.plugin.hikaricp.interceptor;

import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.monitor.DataSourceMonitorRegistry;
import com.navercorp.pinpoint.bootstrap.util.InterceptorUtils;
import com.navercorp.pinpoint.plugin.hikaricp.DataSourceMonitorAccessor;
import com.navercorp.pinpoint.plugin.hikaricp.HikariCpDataSourceMonitor;

import java.lang.reflect.Method;

/**
 * @author Taejin Koo
 */
public class DataSourceConstructorInterceptor implements AroundInterceptor {

    private final PLogger logger = PLoggerFactory.getLogger(getClass());

    private final DataSourceMonitorRegistry dataSourceMonitorRegistry;

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

        if (args.length >= 1) {
            try {
                String jdbcUrl = getJdbcUrl(args[0]);
                HikariCpDataSourceMonitor dataSourceMonitor = new HikariCpDataSourceMonitor(target, jdbcUrl);
                dataSourceMonitorRegistry.register(dataSourceMonitor);

                if (target instanceof DataSourceMonitorAccessor) {
                    ((DataSourceMonitorAccessor) target)._$PINPOINT$_setDataSourceMonitor(dataSourceMonitor);
                }
            } catch (Exception e) {
                logger.info("failed while creating HikariCpDataSourceMonitor. message:{}", e.getMessage(), e);
            }
        }
    }

    private String getJdbcUrl(Object object) {
        try {
            if (object == null) {
                return null;
            }

            Method getJdbcUrl = object.getClass().getMethod("getJdbcUrl");
            if (getJdbcUrl == null) {
                return null;
            }

            return String.valueOf(getJdbcUrl.invoke(object));
        } catch (Exception e) {
            logger.info("failed while executing getJdbcUrl(). message:{}", e.getMessage(), e);
        }
        return null;
    }

}
