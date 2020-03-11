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

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.SpanEventSimpleAroundInterceptorForPlugin;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.monitor.DataSourceMonitorRegistry;
import com.navercorp.pinpoint.bootstrap.util.InterceptorUtils;
import com.navercorp.pinpoint.plugin.hikaricp.DataSourceMonitorAccessor;
import com.navercorp.pinpoint.plugin.hikaricp.HikariCpConstants;
import com.navercorp.pinpoint.plugin.hikaricp.HikariCpDataSourceMonitor;

import java.lang.reflect.Method;
import java.util.Properties;

/**
 * @author Taejin Koo
 */
public class DataSourceConstructorInterceptor extends SpanEventSimpleAroundInterceptorForPlugin {

    private final PLogger logger = PLoggerFactory.getLogger(getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final DataSourceMonitorRegistry dataSourceMonitorRegistry;

    public DataSourceConstructorInterceptor(TraceContext traceContext, MethodDescriptor descriptor, DataSourceMonitorRegistry dataSourceMonitorRegistry) {
        super(traceContext, descriptor);
        this.dataSourceMonitorRegistry = dataSourceMonitorRegistry;
    }

    @Override
    protected void doInBeforeTrace(SpanEventRecorder recorder, Object target, Object[] args) {
    }

    @Override
    protected void prepareAfterTrace(Object target, Object[] args, Object result, Throwable throwable) {
        // create DataSourceMonitor object even if it is not being traced
        if (!InterceptorUtils.isSuccess(throwable)) {
            return;
        }

        if (args.length >= 1) {
            try {
                String jdbcUrl = getJdbcUrl(args[0]);
                if (jdbcUrl == null) {
                    jdbcUrl = findJdbcUrl(args[0]);
                }

                if (jdbcUrl != null) {
                    HikariCpDataSourceMonitor dataSourceMonitor = new HikariCpDataSourceMonitor(target, jdbcUrl);
                    dataSourceMonitorRegistry.register(dataSourceMonitor);

                    if (target instanceof DataSourceMonitorAccessor) {
                        ((DataSourceMonitorAccessor) target)._$PINPOINT$_setDataSourceMonitor(dataSourceMonitor);
                    }

                    if (isDebug) {
                        logger.debug("create HikariCpDataSourceMonitor success. jdbcUrl:{}", jdbcUrl);
                    }
                } else {
                    logger.info("failed while creating HikariCpDataSourceMonitor. can't find jdbclUrl");
                }
            } catch (Exception e) {
                logger.info("failed while creating HikariCpDataSourceMonitor. message:{}", e.getMessage(), e);
            }
        }
    }

    private String getJdbcUrl(Object object) {
        // get JdbcUrl using getJdbcUrl method
        if (object == null) {
            return null;
        }

        try {
            Method getJdbcUrl = object.getClass().getMethod("getJdbcUrl");
            if (getJdbcUrl != null) {
                Object result = getJdbcUrl.invoke(object);
                if (result instanceof String) {
                    return (String) result;
                }
                return null;
            }
        } catch (Exception e) {
            logger.info("failed while executing getJdbcUrl(). message:{}", e.getMessage(), e);
        }
        return null;
    }

    private String findJdbcUrl(Object object) {
        // find jdbcUrl in dataSourceProperties
        if (object == null) {
            return null;
        }

        try {
            Method getDataSourceProperties = object.getClass().getMethod("getDataSourceProperties");
            if (getDataSourceProperties != null) {
                Object result = getDataSourceProperties.invoke(object);
                if (result instanceof Properties) {
                    return ((Properties) result).getProperty("url");
                }
                return null;
            }
        } catch (Exception e) {
            logger.info("failed while executing getJdbcUrl(). message:{}", e.getMessage(), e);
        }
        return null;
    }

    @Override
    protected void doInAfterTrace(SpanEventRecorder recorder, Object target, Object[] args, Object result, Throwable throwable) {
        recorder.recordServiceType(HikariCpConstants.SERVICE_TYPE);
        recorder.recordApi(getMethodDescriptor());
        recorder.recordException(throwable);
    }

}
