/*
 * Copyright 2022 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.plugin.cassandra4.interceptor;

import com.datastax.dse.driver.api.core.graph.BatchGraphStatement;
import com.datastax.dse.driver.api.core.graph.FluentGraphStatement;
import com.datastax.dse.driver.api.core.graph.GraphStatement;
import com.datastax.oss.driver.api.core.cql.BatchStatement;
import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.datastax.oss.driver.api.core.cql.PrepareRequest;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import com.datastax.oss.driver.api.core.session.Request;
import com.navercorp.pinpoint.bootstrap.context.DatabaseInfo;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.ParsingResult;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.SpanEventSimpleAroundInterceptorForPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.BindValueAccessor;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.DatabaseInfoAccessorUtils;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.UnKnownDatabaseInfo;
import com.navercorp.pinpoint.common.util.ArrayArgumentUtils;
import com.navercorp.pinpoint.common.util.MapUtils;

import java.util.Map;

public class DefaultSessionExecuteInterceptor extends SpanEventSimpleAroundInterceptorForPlugin {

    private final int maxSqlBindValueLength;

    public DefaultSessionExecuteInterceptor(TraceContext traceContext, MethodDescriptor descriptor, int maxSqlBindValueLength) {
        super(traceContext, descriptor);
        this.maxSqlBindValueLength = maxSqlBindValueLength;
    }

    @Override
    public void doInBeforeTrace(SpanEventRecorder recorder, Object target, Object[] args) throws Exception {
        DatabaseInfo databaseInfo = DatabaseInfoAccessorUtils.getDatabaseInfo(target);
        if (databaseInfo == null) {
            databaseInfo = UnKnownDatabaseInfo.INSTANCE;
        }

        recorder.recordServiceType(databaseInfo.getExecuteQueryType());
        recorder.recordEndPoint(databaseInfo.getMultipleHost());
        recorder.recordDestinationId(databaseInfo.getDatabaseId());

        final Request request = ArrayArgumentUtils.getArgument(args, 0, Request.class);
        final String sql = retrieveSql(request);
        if (sql != null) {
            final ParsingResult parsingResult = traceContext.parseSql(sql);
            if (parsingResult != null) {
                final Map<Integer, String> bindValue = getBindValue(request);
                if (MapUtils.hasLength(bindValue)) {
                    final String bindString = toBindVariable(bindValue);
                    recorder.recordSqlParsingResult(parsingResult, bindString);
                } else {
                    recorder.recordSqlParsingResult(parsingResult);
                }
            }
        }
    }

    private String retrieveSql(Request request) {
        if (request == null) {
            return "UNKNOWN";
        }

        if (request instanceof BoundStatement) {
            return ((BoundStatement) request).getPreparedStatement().getQuery();
        } else if (request instanceof PrepareRequest) {
            return ((PrepareRequest) request).getQuery();
        } else if (request instanceof SimpleStatement) {
            return ((SimpleStatement) request).getQuery();
        } else if (request instanceof BatchStatement) {
            return "Batch statement. size=" + ((BatchStatement) request).size();
        } else if (request instanceof BatchGraphStatement) {
            return "Batch graph statement. size=" + ((BatchGraphStatement) request).size();
        } else if (request instanceof FluentGraphStatement) {
            return "Fluent graph statement.";
        } else if (request instanceof GraphStatement) {
            return "Graph statement.";
        }
        return null;
    }

    private Map<Integer, String> getBindValue(Request request) {
        if (request instanceof BindValueAccessor) {
            return ((BindValueAccessor) request)._$PINPOINT$_getBindValue();
        }
        return null;
    }

    private String toBindVariable(Map<Integer, String> bindValue) {
        return traceContext.getJdbcContext().getBindVariableService().bindVariableToString(bindValue, maxSqlBindValueLength);
    }

    @Override
    public void doInAfterTrace(SpanEventRecorder recorder, Object target, Object[] args, Object result, Throwable throwable) throws Exception {
        recorder.recordException(throwable);
        recorder.recordApi(methodDescriptor);
    }
}
