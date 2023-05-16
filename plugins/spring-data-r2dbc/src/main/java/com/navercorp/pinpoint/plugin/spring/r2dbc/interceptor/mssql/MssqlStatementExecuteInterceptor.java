/*
 * Copyright 2022 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.spring.r2dbc.interceptor.mssql;

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessor;
import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessorUtils;
import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.context.DatabaseInfo;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.ParsingResult;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.SpanEventSimpleAroundInterceptorForPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.BindValueAccessor;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.DatabaseInfoAccessor;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.ParsingResultAccessor;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.UnKnownDatabaseInfo;
import com.navercorp.pinpoint.plugin.spring.r2dbc.BindNameValueAccessor;

import java.util.HashMap;
import java.util.Map;

public class MssqlStatementExecuteInterceptor extends SpanEventSimpleAroundInterceptorForPlugin {
    private final int maxSqlBindValueSize;

    public MssqlStatementExecuteInterceptor(TraceContext context, MethodDescriptor descriptor, int maxSqlBindValueSize) {
        super(context, descriptor);
        this.maxSqlBindValueSize = maxSqlBindValueSize;
    }

    @Override
    public void doInBeforeTrace(SpanEventRecorder recorder, Object target, Object[] args) {
        DatabaseInfo databaseInfo = (target instanceof DatabaseInfoAccessor) ? ((DatabaseInfoAccessor) target)._$PINPOINT$_getDatabaseInfo() : null;
        if (databaseInfo == null) {
            databaseInfo = UnKnownDatabaseInfo.INSTANCE;
        }

        recorder.recordServiceType(databaseInfo.getExecuteQueryType());
        recorder.recordEndPoint(databaseInfo.getMultipleHost());
        recorder.recordDestinationId(databaseInfo.getDatabaseId());

        ParsingResult parsingResult = null;
        if (target instanceof ParsingResultAccessor) {
            parsingResult = ((ParsingResultAccessor) target)._$PINPOINT$_getParsingResult();
        }
        Map<String, String> bindValue = null;
        if (target instanceof BindNameValueAccessor) {
            bindValue = ((BindNameValueAccessor) target)._$PINPOINT$_getBindValue();
        }
        if (bindValue != null) {
            String bindString = toBindVariable(bindValue);
            recorder.recordSqlParsingResult(parsingResult, bindString);
        } else {
            recorder.recordSqlParsingResult(parsingResult);
        }
        clean(target);
    }

    private String toBindVariable(Map<String, String> bindValue) {
        return traceContext.getJdbcContext().getBindVariableService().bindNameVariableToString(bindValue, maxSqlBindValueSize);
    }

    private void clean(Object target) {
        if (target instanceof BindValueAccessor) {
            ((BindValueAccessor) target)._$PINPOINT$_setBindValue(new HashMap<>());
        }
    }

    @Override
    public void doInAfterTrace(SpanEventRecorder recorder, Object target, Object[] args, Object result, Throwable throwable) {
        recorder.recordException(throwable);
        recorder.recordApi(methodDescriptor);

        if (Boolean.FALSE == result instanceof AsyncContextAccessor) {
            return;
        }

        final AsyncContext asyncContext = recorder.recordNextAsyncContext();
        AsyncContextAccessorUtils.setAsyncContext(asyncContext, result);
        if (isDebug) {
            logger.debug("Set asyncContext to result. asyncContext={}", asyncContext);
        }
    }
}
