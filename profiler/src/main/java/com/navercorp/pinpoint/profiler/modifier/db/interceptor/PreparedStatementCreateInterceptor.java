package com.nhn.pinpoint.profiler.modifier.db.interceptor;

import com.nhn.pinpoint.bootstrap.context.RecordableTrace;
import com.nhn.pinpoint.bootstrap.interceptor.*;
import com.nhn.pinpoint.bootstrap.interceptor.tracevalue.DatabaseInfoTraceValue;
import com.nhn.pinpoint.bootstrap.interceptor.tracevalue.DatabaseInfoTraceValueUtils;
import com.nhn.pinpoint.bootstrap.interceptor.tracevalue.ParsingResultTraceValue;
import com.nhn.pinpoint.common.util.ParsingResult;

import com.nhn.pinpoint.bootstrap.context.DatabaseInfo;
import com.nhn.pinpoint.bootstrap.util.InterceptorUtils;

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
                // preparedStatement의 생성이 성공하였을 경우만 PreparedStatement에 databaseInfo를 세팅해야 한다.
                DatabaseInfo databaseInfo = ((DatabaseInfoTraceValue) target).__getTraceDatabaseInfo();
                if (databaseInfo != null) {
                    if (result instanceof DatabaseInfoTraceValue) {
                        ((DatabaseInfoTraceValue) result).__setTraceDatabaseInfo(databaseInfo);
                    }
                }
            }
            if (result instanceof ParsingResultTraceValue) {
                // 1. traceContext를 체크하면 안됨. traceContext에서 즉 같은 thread에서 prearedStatement에서 안만들수도 있음.
                // 2. sampling 동작이 동작할 경우 preparedStatement를 create하는 thread가 trace 대상이 아닐수 있음. 먼제 sql을 저장해야 한다.
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

        ParsingResult parsingResult = ((ParsingResultTraceValue) result).__getTraceParsingResult();
        trace.recordSqlParsingResult(parsingResult);
        trace.recordException(throwable);
        trace.recordApi(getMethodDescriptor());

        trace.markAfterTime();
    }


}
