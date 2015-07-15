/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.jdbc.common.interceptor;

import com.navercorp.pinpoint.bootstrap.MetadataAccessor;
import com.navercorp.pinpoint.bootstrap.context.DatabaseInfo;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.interceptor.SpanEventSimpleAroundInterceptorForPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.annotation.Name;
import com.navercorp.pinpoint.bootstrap.plugin.annotation.TargetMethod;
import com.navercorp.pinpoint.bootstrap.util.InterceptorUtils;
import com.navercorp.pinpoint.plugin.jdbc.common.JdbcDriverConstants;
import com.navercorp.pinpoint.plugin.jdbc.common.JdbcUrlParser;
import com.navercorp.pinpoint.plugin.jdbc.common.UnKnownDatabaseInfo;


/**
 * must be used with ExecutionPolicy.ALWAYS
 * 
 * @author emeroad
 */
@TargetMethod(name="connect", paramTypes={ "java.lang.String", "java.util.Properties" })
public class DriverConnectInterceptor extends SpanEventSimpleAroundInterceptorForPlugin implements JdbcDriverConstants {
    private final MetadataAccessor databaseInfoAccessor;
    
    private final JdbcUrlParser jdbcUrlParser;
    private final boolean recordConnection;


    public DriverConnectInterceptor(TraceContext context, MethodDescriptor descriptor, @Name(DATABASE_INFO) MetadataAccessor databaseInfoAccessor, JdbcUrlParser jdbcUrlParser) {
        this(context, descriptor, databaseInfoAccessor, jdbcUrlParser, true);
    }

    public DriverConnectInterceptor(TraceContext context, MethodDescriptor descriptor, @Name(DATABASE_INFO) MetadataAccessor databaseInfoAccessor, JdbcUrlParser jdbcUrlParser, boolean recordConnection) {
        super(context, descriptor);

        this.databaseInfoAccessor = databaseInfoAccessor;
        this.jdbcUrlParser = jdbcUrlParser;
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
        DatabaseInfo databaseInfo = createDatabaseInfo(driverUrl);
        if (success) {
            if (recordConnection) {
                databaseInfoAccessor.set(result, databaseInfo);
            }
        }
    }

    @Override
    protected void doInAfterTrace(SpanEventRecorder recorder, Object target, Object[] args, Object result, Throwable throwable) {

        if (recordConnection) {
            final DatabaseInfo databaseInfo = databaseInfoAccessor.get(result, UnKnownDatabaseInfo.INSTANCE);
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

    private DatabaseInfo createDatabaseInfo(String url) {
        if (url == null) {
            return UnKnownDatabaseInfo.INSTANCE;
        }
        
        final DatabaseInfo databaseInfo = jdbcUrlParser.parse(url);
        
        if (isDebug) {
            logger.debug("parse DatabaseInfo:{}", databaseInfo);
        }
        
        return databaseInfo;
    }

}
