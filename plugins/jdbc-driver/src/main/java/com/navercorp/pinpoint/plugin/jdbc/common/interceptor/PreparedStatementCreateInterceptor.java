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
import com.navercorp.pinpoint.bootstrap.plugin.annotation.Targets;
import com.navercorp.pinpoint.bootstrap.util.InterceptorUtils;
import com.navercorp.pinpoint.common.util.ParsingResult;
import com.navercorp.pinpoint.plugin.jdbc.common.JdbcDriverConstants;
import com.navercorp.pinpoint.plugin.jdbc.common.UnKnownDatabaseInfo;

/**
 * @author emeroad
 */
@Targets(methods={
        @TargetMethod(name="prepareStatement", paramTypes={ "java.lang.String" }),
        @TargetMethod(name="prepareStatement", paramTypes={ "java.lang.String", "int" }), 
        @TargetMethod(name="prepareStatement", paramTypes={ "java.lang.String", "int[]" }),
        @TargetMethod(name="prepareStatement", paramTypes={ "java.lang.String", "int", "int" }),
        @TargetMethod(name="prepareStatement", paramTypes={ "java.lang.String", "int", "int", "int" })
})
public class PreparedStatementCreateInterceptor extends SpanEventSimpleAroundInterceptorForPlugin implements JdbcDriverConstants {

    private final MetadataAccessor databaseInfoAccessor;
    private final MetadataAccessor parsingResultAccessor;

    public PreparedStatementCreateInterceptor(TraceContext context, MethodDescriptor descriptor, @Name(DATABASE_INFO) MetadataAccessor databaseInfoAccessor, @Name(PARSING_RESULT) MetadataAccessor parsingResultAccessor) {
        super(context, descriptor);
        this.databaseInfoAccessor = databaseInfoAccessor;
        this.parsingResultAccessor = parsingResultAccessor;
    }

    @Override
    public void doInBeforeTrace(SpanEventRecorder recorder, Object target, Object[] args)  {
        final DatabaseInfo databaseInfo = databaseInfoAccessor.get(target, UnKnownDatabaseInfo.INSTANCE);
        
        recorder.recordServiceType(databaseInfo.getType());
        recorder.recordEndPoint(databaseInfo.getMultipleHost());
        recorder.recordDestinationId(databaseInfo.getDatabaseId());
    }

    @Override
    protected void prepareAfterTrace(Object target, Object[] args, Object result, Throwable throwable) {
        final boolean success = InterceptorUtils.isSuccess(throwable);
        if (success) {
            if (databaseInfoAccessor.isApplicable(target)) {
                // set databaseInfo to PreparedStatement only when preparedStatement is generated successfully.
                DatabaseInfo databaseInfo = databaseInfoAccessor.get(target);
                if (databaseInfo != null) {
                    if (databaseInfoAccessor.isApplicable(result)) {
                        databaseInfoAccessor.set(result, databaseInfo);
                    }
                }
            }
            if (parsingResultAccessor.isApplicable(result)) {
                // 1. Don't check traceContext. preparedStatement can be created in other thread.
                // 2. While sampling is active, the thread which creates preparedStatement could not be a sampling target. So record sql anyway. 
                String sql = (String) args[0];
                ParsingResult parsingResult = traceContext.parseSql(sql);
                if (parsingResult != null) {
                    parsingResultAccessor.set(result, parsingResult);
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
        if (parsingResultAccessor.isApplicable(result)) {
            ParsingResult parsingResult = parsingResultAccessor.get(result);
            recorder.recordSqlParsingResult(parsingResult);
        }
        recorder.recordException(throwable);
        recorder.recordApi(methodDescriptor);
    }
}
