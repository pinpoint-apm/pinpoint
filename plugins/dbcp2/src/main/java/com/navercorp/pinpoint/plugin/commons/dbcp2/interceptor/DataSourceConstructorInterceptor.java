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

package com.navercorp.pinpoint.plugin.commons.dbcp2.interceptor;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.annotation.Scope;
import com.navercorp.pinpoint.bootstrap.interceptor.annotation.TargetConstructor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.monitor.DataSourceMonitor;
import com.navercorp.pinpoint.bootstrap.plugin.monitor.PluginMonitorContext;
import com.navercorp.pinpoint.bootstrap.plugin.monitor.PluginMonitorRegistry;
import com.navercorp.pinpoint.bootstrap.util.InterceptorUtils;
import com.navercorp.pinpoint.plugin.commons.dbcp2.CommonsDbcp2Constants;
import com.navercorp.pinpoint.plugin.commons.dbcp2.DataSourceMonitorAccessor;
import com.navercorp.pinpoint.plugin.commons.dbcp2.Dbcp2DataSourceMonitor;
import org.apache.commons.dbcp2.BasicDataSource;

/**
 * @author Taejin Koo
 */
@Scope(CommonsDbcp2Constants.SCOPE)
@TargetConstructor
public class DataSourceConstructorInterceptor implements AroundInterceptor {

    private static final PLogger logger = PLoggerFactory.getLogger(DataSourceConstructorInterceptor.class);

    private final TraceContext traceContext;
    private final MethodDescriptor methodDescriptor;


    public DataSourceConstructorInterceptor(TraceContext traceContext, MethodDescriptor methodDescriptor) {
        this.traceContext = traceContext;
        this.methodDescriptor = methodDescriptor;
    }

    @Override
    public void before(Object target, Object[] args) {
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (!InterceptorUtils.isSuccess(throwable)) {
            return;
        }

        PluginMonitorContext pluginMonitorContext = traceContext.getPluginMonitorContext();
        PluginMonitorRegistry<DataSourceMonitor> dataSourceMonitorRegistry = pluginMonitorContext.getDataSourceMonitorRegistry();
        if (dataSourceMonitorRegistry == null) {
            return;
        }

        if ((target instanceof DataSourceMonitorAccessor)) {
            Dbcp2DataSourceMonitor dbcpDataSourceMonitor = new Dbcp2DataSourceMonitor((BasicDataSource)target);
            dataSourceMonitorRegistry.register(dbcpDataSourceMonitor);

            ((DataSourceMonitorAccessor) target)._$PINPOINT$_setDataSourceMonitor(dbcpDataSourceMonitor);
        }
    }

}
