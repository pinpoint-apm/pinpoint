package com.nhn.pinpoint.profiler.modifier.db.interceptor;

import com.nhn.pinpoint.bootstrap.context.DatabaseInfo;
import com.nhn.pinpoint.bootstrap.context.RecordableTrace;
import com.nhn.pinpoint.bootstrap.interceptor.*;
import com.nhn.pinpoint.bootstrap.interceptor.tracevalue.DatabaseInfoTraceValueUtils;
import com.nhn.pinpoint.bootstrap.logging.PLoggerFactory;

/**
 * @author emeroad
 */
public class TransactionRollbackInterceptor extends SpanEventSimpleAroundInterceptor {


    public TransactionRollbackInterceptor() {
        super(PLoggerFactory.getLogger(TransactionRollbackInterceptor.class));
    }


    @Override
    public void doInBeforeTrace(RecordableTrace trace, Object target, Object[] args) {
        trace.markBeforeTime();
    }


    @Override
    public void doInAfterTrace(RecordableTrace trace, Object target, Object[] args, Object result, Throwable throwable) {

        DatabaseInfo databaseInfo = DatabaseInfoTraceValueUtils.__getTraceDatabaseInfo(target, UnKnownDatabaseInfo.INSTANCE);

        trace.recordServiceType(databaseInfo.getType());
        trace.recordEndPoint(databaseInfo.getMultipleHost());
        trace.recordDestinationId(databaseInfo.getDatabaseId());


        trace.recordApi(getMethodDescriptor());
//            boolean success = InterceptorUtils.isSuccess(result);
//            if (success) {
//                trace.recordAttribute("Transaction", "rollback");
//            } else {
//                trace.recordAttribute("Transaction", "rollback fail");
//            }
        trace.recordException(throwable);
        trace.markAfterTime();
    }

}
