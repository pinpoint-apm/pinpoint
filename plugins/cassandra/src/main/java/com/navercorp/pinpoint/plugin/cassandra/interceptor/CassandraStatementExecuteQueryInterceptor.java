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

package com.navercorp.pinpoint.plugin.cassandra.interceptor;

import com.datastax.driver.core.BatchStatement;
import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.RegularStatement;
import com.datastax.driver.core.Statement;
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
import com.navercorp.pinpoint.common.util.MapUtils;
import com.navercorp.pinpoint.plugin.cassandra.field.WrappedStatementGetter;

import java.util.HashMap;
import java.util.Map;

/**
 * @author dawidmalina
 */
public class CassandraStatementExecuteQueryInterceptor extends SpanEventSimpleAroundInterceptorForPlugin {

    private static final int DEFAULT_BIND_VALUE_LENGTH = 1024;

    private final int maxSqlBindValueLength;

    public CassandraStatementExecuteQueryInterceptor(TraceContext traceContext, MethodDescriptor descriptor) {
        this(traceContext, descriptor, DEFAULT_BIND_VALUE_LENGTH);
    }

    public CassandraStatementExecuteQueryInterceptor(TraceContext traceContext, MethodDescriptor descriptor,
            int maxSqlBindValueLength) {
        super(traceContext, descriptor);
        this.maxSqlBindValueLength = maxSqlBindValueLength;
    }


    @Override
    protected void doInBeforeTrace(SpanEventRecorder recorder, Object target, Object[] args) throws Exception {
        DatabaseInfo databaseInfo = (target instanceof DatabaseInfoAccessor)
                ? ((DatabaseInfoAccessor) target)._$PINPOINT$_getDatabaseInfo() : null;

        if (databaseInfo == null) {
            databaseInfo = UnKnownDatabaseInfo.INSTANCE;
        }

        recorder.recordServiceType(databaseInfo.getExecuteQueryType());
        recorder.recordEndPoint(databaseInfo.getMultipleHost());
        recorder.recordDestinationId(databaseInfo.getDatabaseId());

        String sql = retrieveSql(args[0]);

        if (sql != null) {
            ParsingResult parsingResult = traceContext.parseSql(sql);
            if (parsingResult != null) {
                ((ParsingResultAccessor) target)._$PINPOINT$_setParsingResult(parsingResult);
            } else {
                if (logger.isErrorEnabled()) {
                    logger.error("sqlParsing fail. parsingResult is null sql:{}", sql);
                }
            }

            Map<Integer, String> bindValue = ((BindValueAccessor) target)._$PINPOINT$_getBindValue();
            // TODO Add bind variable interceptors to BoundStatement's setter methods and bind method and pass it down
            // Extracting bind variables from already-serialized is too risky
            if (MapUtils.hasLength(bindValue)) {
                String bindString = toBindVariable(bindValue);
                recorder.recordSqlParsingResult(parsingResult, bindString);
            } else {
                recorder.recordSqlParsingResult(parsingResult);
            }
        }

        recorder.recordApi(methodDescriptor);
        clean(target);
    }


    private String retrieveSql(Object args0) {
        if (args0 instanceof BoundStatement) {
            return ((BoundStatement) args0).preparedStatement().getQueryString();
        } else if (args0 instanceof RegularStatement) {
            return ((RegularStatement) args0).getQueryString();
        } else if (args0 instanceof WrappedStatementGetter) {
            return retrieveWrappedStatement((WrappedStatementGetter) args0);
        } else if (args0 instanceof BatchStatement) {
            // we could unroll all the batched statements and append ; between them if need be but it could be too long.
            return null;
        } else if (args0 instanceof String) {
            return (String) args0;
        }
        return null;
    }

    private String retrieveWrappedStatement(WrappedStatementGetter wrappedStatementGetter) {
        Statement wrappedStatement = wrappedStatementGetter._$PINPOINT$_getStatement();
        return retrieveSql(wrappedStatement);
    }

    private void clean(Object target) {
        if (target instanceof BindValueAccessor) {
            ((BindValueAccessor) target)._$PINPOINT$_setBindValue(new HashMap<Integer, String>());
        }
    }

    private String toBindVariable(Map<Integer, String> bindValue) {
        return traceContext.getJdbcContext().getBindVariableService().bindVariableToString(bindValue, maxSqlBindValueLength);
    }

    @Override
    protected void doInAfterTrace(SpanEventRecorder recorder, Object target, Object[] args, Object result, Throwable throwable) throws Exception {
        // TODO Test if it's success. if failed terminate. else calculate
        // resultset fetch too. we'd better make resultset fetch optional.
        recorder.recordException(throwable);
    }

}
