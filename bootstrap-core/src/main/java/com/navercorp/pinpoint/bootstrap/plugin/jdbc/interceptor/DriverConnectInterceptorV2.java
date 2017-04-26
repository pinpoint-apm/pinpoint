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

package com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor;

import com.navercorp.pinpoint.bootstrap.context.DatabaseInfo;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.SpanEventSimpleAroundInterceptorForPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.DatabaseInfoAccessor;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.UnKnownDatabaseInfo;
import com.navercorp.pinpoint.bootstrap.util.InterceptorUtils;
import com.navercorp.pinpoint.common.trace.ServiceType;


/**
 * must be used with ExecutionPolicy.ALWAYS
 * 
 * @author emeroad
 */
// #1375 Workaround java level Deadlock
// https://oss.navercorp.com/pinpoint/pinpoint-naver/issues/1375
//@TargetMethod(name="connect", paramTypes={ "java.lang.String", "java.util.Properties" })
public class DriverConnectInterceptorV2 extends SpanEventSimpleAroundInterceptorForPlugin {

    private final ServiceType serviceType;
    private final boolean recordConnection;

    public DriverConnectInterceptorV2(TraceContext context, MethodDescriptor descriptor, ServiceType serviceType) {
        this(context, descriptor, serviceType, true);
    }

    public DriverConnectInterceptorV2(TraceContext context, MethodDescriptor descriptor, ServiceType serviceType, boolean recordConnection) {
        super(context, descriptor);

        this.serviceType = serviceType;
        // option for mysql loadbalance only. Destination is recorded at lower implementations.
        this.recordConnection = recordConnection;
    }

    @Override
    protected void logBeforeInterceptor(Object target, Object[] args) {
        // Must not log args because it contains a password
        logger.beforeInterceptor(target, null);
    }

    @Override
    protected void doInBeforeTrace(SpanEventRecorder recorder, Object target, Object[] args) {
    }


    @Override
    protected void logAfterInterceptor(Object target, Object[] args, Object result, Throwable throwable) {
        logger.afterInterceptor(target, null, result, throwable);
    }

    @Override
    protected void prepareAfterTrace(Object target, Object[] args, Object result, Throwable throwable) {
        final boolean success = InterceptorUtils.isSuccess(throwable);
        // Must not check if current transaction is trace target or not. Connection can be made by other thread.
        final String driverUrl = (String) args[0];
        DatabaseInfo databaseInfo = traceContext.getJdbcContext().parseJdbcUrl(serviceType, driverUrl);
        if (success) {
            if (recordConnection) {
                if (result instanceof DatabaseInfoAccessor) {
                    ((DatabaseInfoAccessor) result)._$PINPOINT$_setDatabaseInfo(databaseInfo);
                }
            }
        }
    }

    @Override
    protected void doInAfterTrace(SpanEventRecorder recorder, Object target, Object[] args, Object result, Throwable throwable) {
        if (recordConnection) {
            DatabaseInfo databaseInfo;
            if (result instanceof DatabaseInfoAccessor) {
                databaseInfo = ((DatabaseInfoAccessor) result)._$PINPOINT$_getDatabaseInfo();
            } else {
                databaseInfo = null;
            }

            if (databaseInfo == null) {
                databaseInfo = UnKnownDatabaseInfo.INSTANCE;
            }
            
            // Count database connect too because it's very heavy operation
            recorder.recordServiceType(databaseInfo.getType());
            recorder.recordEndPoint(databaseInfo.getMultipleHost());
            recorder.recordDestinationId(databaseInfo.getDatabaseId());
        }
        final String driverUrl = (String) args[0];
        // Invoking databaseInfo.getRealUrl() here is dangerous. It doesn't return real URL if it's a loadbalance connection.  
        recorder.recordApiCachedString(methodDescriptor, driverUrl, 0);

        recorder.recordException(throwable);
    }

}
