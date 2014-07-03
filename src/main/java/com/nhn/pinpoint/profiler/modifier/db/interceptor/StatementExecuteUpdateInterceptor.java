package com.nhn.pinpoint.profiler.modifier.db.interceptor;

import com.nhn.pinpoint.bootstrap.context.RecordableTrace;

import com.nhn.pinpoint.bootstrap.interceptor.*;
import com.nhn.pinpoint.bootstrap.interceptor.tracevalue.DatabaseInfoTraceValueUtils;
import com.nhn.pinpoint.bootstrap.logging.PLoggerFactory;
import com.nhn.pinpoint.bootstrap.context.DatabaseInfo;

/**
 * protected int executeUpdate(String sql, boolean isBatch, boolean returnGeneratedKeys)
 *
 * @author netspider
 * @author emeroad
 */
public class StatementExecuteUpdateInterceptor extends SpanEventSimpleAroundInterceptor {

    public StatementExecuteUpdateInterceptor() {
        super(PLoggerFactory.getLogger(StatementExecuteUpdateInterceptor.class));
    }

    @Override
    public void doInBeforeTrace(RecordableTrace trace, Object target, Object[] args) {

        trace.markBeforeTime();

        DatabaseInfo databaseInfo = DatabaseInfoTraceValueUtils.__getTraceDatabaseInfo(target, UnKnownDatabaseInfo.INSTANCE);

        trace.recordServiceType(databaseInfo.getExecuteQueryType());
        trace.recordEndPoint(databaseInfo.getMultipleHost());
        trace.recordDestinationId(databaseInfo.getDatabaseId());

        trace.recordApi(getMethodDescriptor());
        if (args != null && args.length > 0) {
            Object arg = args[0];
            if (arg instanceof String) {
                trace.recordSqlInfo((String) arg);
            }
        }
    }


    @Override
    public void doInAfterTrace(RecordableTrace trace, Object target, Object[] args, Object result, Throwable throwable) {
        trace.recordException(throwable);

        // TODO 결과, 수행시간을.알수 있어야 될듯.
        trace.markAfterTime();
    }

}
