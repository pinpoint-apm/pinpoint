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

package com.navercorp.pinpoint.plugin.jdbc.informix.interceptor;

import com.navercorp.pinpoint.bootstrap.context.DatabaseInfo;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.ParsingResult;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.SpanEventSimpleAroundInterceptorForPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.DatabaseInfoAccessor;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.ParsingResultAccessor;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.DefaultDatabaseInfo;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.UnKnownDatabaseInfo;
import com.navercorp.pinpoint.bootstrap.util.InterceptorUtils;

import java.util.Arrays;
import java.util.Properties;
import com.informix.util.AdvancedUppercaseProperties;

import com.navercorp.pinpoint.plugin.jdbc.informix.InformixConstants;
import com.navercorp.pinpoint.plugin.jdbc.informix.interceptor.getter.InformixDatabaseNameGetter;
import com.navercorp.pinpoint.plugin.jdbc.informix.interceptor.getter.InformixConnectionInfoGetter;
import com.navercorp.pinpoint.plugin.jdbc.informix.interceptor.getter.Informix_4_50_ConnectionInfoGetter;

/**
 * @author Guillermo Adrian Molina
 */
// #1375 Workaround java level Deadlock
// https://oss.navercorp.com/pinpoint/pinpoint-naver/issues/1375
//@TargetMethods({
//        @TargetMethod(name="prepareStatement", paramTypes={ "java.lang.String" }),
//        @TargetMethod(name="prepareStatement", paramTypes={ "java.lang.String", "int" }),
//        @TargetMethod(name="prepareStatement", paramTypes={ "java.lang.String", "int[]" }),
//        @TargetMethod(name="prepareStatement", paramTypes={ "java.lang.String", "java.lang.String[]" }),
//        @TargetMethod(name="prepareStatement", paramTypes={ "java.lang.String", "int", "int" }),
//        @TargetMethod(name="prepareStatement", paramTypes={ "java.lang.String", "int", "int", "int" }),
//        @TargetMethod(name="prepareCall", paramTypes={ "java.lang.String" }),
//        @TargetMethod(name="prepareCall", paramTypes={ "java.lang.String", "int", "int" }),
//        @TargetMethod(name="prepareCall", paramTypes={ "java.lang.String", "int", "int", "int" })
//})
public class InformixPreparedStatementCreateInterceptor extends SpanEventSimpleAroundInterceptorForPlugin {

    public InformixPreparedStatementCreateInterceptor(TraceContext context, MethodDescriptor descriptor) {
        super(context, descriptor);
    }

    @Override
    public void doInBeforeTrace(SpanEventRecorder recorder, Object target, Object[] args)  {
        final DatabaseInfo databaseInfo = getDatabaseInfo(target);
        if (target instanceof DatabaseInfoAccessor) {
            ((DatabaseInfoAccessor) target)._$PINPOINT$_setDatabaseInfo(databaseInfo);
        }
        
        recorder.recordServiceType(databaseInfo.getType());
        recorder.recordEndPoint(databaseInfo.getMultipleHost());
        recorder.recordDestinationId(databaseInfo.getDatabaseId());
    }

    @Override
    protected void prepareAfterTrace(Object target, Object[] args, Object result, Throwable throwable) {
        final boolean success = InterceptorUtils.isSuccess(throwable);
        if (success) {
            if (target instanceof DatabaseInfoAccessor) {
                // set databaseInfo to PreparedStatement only when preparedStatement is generated successfully.
                DatabaseInfo databaseInfo = ((DatabaseInfoAccessor)target)._$PINPOINT$_getDatabaseInfo();
                if (databaseInfo != null) {
                    if (result instanceof DatabaseInfoAccessor) {
                        ((DatabaseInfoAccessor)result)._$PINPOINT$_setDatabaseInfo(databaseInfo);
                    }
                }
            }
            if (result instanceof ParsingResultAccessor) {
                // 1. Don't check traceContext. preparedStatement can be created in other thread.
                // 2. While sampling is active, the thread which creates preparedStatement could not be a sampling target. So record sql anyway. 
                String sql = (String) args[0];
                ParsingResult parsingResult = traceContext.parseSql(sql);
                if (parsingResult != null) {
                    ((ParsingResultAccessor)result)._$PINPOINT$_setParsingResult(parsingResult);
                } else {
                    if (logger.isErrorEnabled()) {
                        logger.error("sqlParsing fail. parsingResult is null sql:{}", sql);
                    }
                }
            }
        }
    }

    @Override
    public void doInAfterTrace(SpanEventRecorder recorder, Object target, Object[] args, Object result, Throwable throwable) {
        if (result instanceof ParsingResultAccessor) {
            ParsingResult parsingResult = ((ParsingResultAccessor)result)._$PINPOINT$_getParsingResult();
            recorder.recordSqlParsingResult(parsingResult);
        }
        recorder.recordException(throwable);
        recorder.recordApi(methodDescriptor);
    }

    private DatabaseInfo getDatabaseInfo(Object target) {
        final String url = getURL(target);
        final String dbName = getDatabaseName(target);

        DatabaseInfo databaseInfo = null;
        if (url != null && dbName != null) {
            databaseInfo = new DefaultDatabaseInfo(InformixConstants.INFORMIX, InformixConstants.INFORMIX_EXECUTE_QUERY, url, url, Arrays.asList(url), dbName);
        }

        if (databaseInfo == null) {
            databaseInfo = UnKnownDatabaseInfo.INSTANCE;
        }
        return databaseInfo;
    }

    private String getDatabaseName(Object target) {
        if (target instanceof InformixDatabaseNameGetter) {
            return ((InformixDatabaseNameGetter) target)._$PINPOINT$_getDatabaseName();
        }
        return null;
    }

    private String getURL(Object target) {
        if (target instanceof InformixConnectionInfoGetter) {
            Properties connInfo = ((InformixConnectionInfoGetter) target)._$PINPOINT$_getConnectionInfo();
            String url = connInfo.getProperty("IFXHOST") + ":" + connInfo.getProperty("PORTNO");
            return url;
        }
        if (target instanceof Informix_4_50_ConnectionInfoGetter) {
            AdvancedUppercaseProperties connInfo = ((Informix_4_50_ConnectionInfoGetter) target)._$PINPOINT$_getConnectionInfo();
            String url = connInfo.getProperty("IFXHOST") + ":" + connInfo.getProperty("PORTNO");
            return url;
        }
        return null;
    }
}
