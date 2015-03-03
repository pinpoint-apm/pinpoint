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

package com.navercorp.pinpoint.profiler.modifier.db.interceptor;

import com.navercorp.pinpoint.bootstrap.context.DatabaseInfo;
import com.navercorp.pinpoint.bootstrap.context.RecordableTrace;
import com.navercorp.pinpoint.bootstrap.interceptor.*;
import com.navercorp.pinpoint.bootstrap.interceptor.tracevalue.DatabaseInfoTraceValue;
import com.navercorp.pinpoint.bootstrap.interceptor.tracevalue.DatabaseInfoTraceValueUtils;
import com.navercorp.pinpoint.bootstrap.interceptor.tracevalue.ParsingResultTraceValue;
import com.navercorp.pinpoint.bootstrap.util.InterceptorUtils;
import com.navercorp.pinpoint.common.util.ParsingResult;

/**
 * @author emeroad
 */
public class PreparedStatementCreateInterceptor extends SpanEventSimpleAroundInterceptor {


    public PreparedStatementCreateInterceptor() {
        super(PreparedStatementCreateInterceptor.class);
    }

    @Override
    public void doInBeforeTrace(RecordableTrace trace, Object target, Object[] args)  {
        trace.markBeforeTime();

        final DatabaseInfo databaseInfo = DatabaseInfoTraceValueUtils.__getTraceDatabaseInfo(target, UnKnownDatabaseInfo.INSTANCE);
        trace.recordServiceType(databaseInfo.getType());
        trace.recordEndPoint(databaseInfo.getMultipleHost());
        trace.recordDestinationId(databaseInfo.getDatabaseId());
    }

    @Override
    protected void prepareAfterTrace(Object target, Object[] args, Object result, Throwable throwable) {
        final boolean success = InterceptorUtils.isSuccess(throwable);
        if (success) {
            if (target instanceof DatabaseInfoTraceValue) {
                // set databaeInfo to PreparedStatement only when preparedStatment is generated successfully. 
                DatabaseInfo databaseInfo = ((DatabaseInfoTraceValue) target).__getTraceDatabaseInfo();
                if (databaseInfo != null) {
                    if (result instanceof DatabaseInfoTraceValue) {
                        ((DatabaseInfoTraceValue) result).__setTraceDatabaseInfo(databaseInfo);
                    }
                }
            }
            if (result instanceof ParsingResultTraceValue) {
                // 1. Don't check traceContext. preparedStatement can be created in other thread.
                // 2. While sampling is active, the thread which creates preparedStatement could not be a sampling target. So record sql anyway. 
                String sql = (String) args[0];
                ParsingResult parsingResult = getTraceContext().parseSql(sql);
                if (parsingResult != null) {
                    ((ParsingResultTraceValue)result).__setTraceParsingResult(parsingResult);
                } else {
                    if (logger.isErrorEnabled()) {
                        logger.error("sqlParsing fail. parsingResult is null sql:{}", sql);
                    }
                }
            }
        }
    }

    @Override
    public void doInAfterTrace(RecordableTrace trace, Object target, Object[] args, Object result, Throwable throwable) {
        if (result instanceof ParsingResultTraceValue) {
            ParsingResult parsingResult = ((ParsingResultTraceValue) result).__getTraceParsingResult();
            trace.recordSqlParsingResult(parsingResult);
        }
        trace.recordException(throwable);
        trace.recordApi(getMethodDescriptor());

        trace.markAfterTime();
    }


}
