package com.navercorp.pinpoint.profiler.context;

import com.navercorp.pinpoint.profiler.context.id.TraceRoot;

public class DefaultSqlCountService implements SqlCountService {
    private final int sqlErrorCount;

    public DefaultSqlCountService(int sqlErrorCount) {
        this.sqlErrorCount = sqlErrorCount;
    }

    @Override
    public void recordSqlCount(TraceRoot traceRoot) {
        boolean isError = traceRoot.getShared().getErrorCode() != 0;
        if (isError) {
            return;
        }

        int sqlCount = traceRoot.getShared().incrementAndGetSqlCount();
        if (sqlCount >= sqlErrorCount) {
            recordError(traceRoot);
        }
    }

    private void recordError(TraceRoot traceRoot) {
        traceRoot.getShared().maskErrorCode(1);
    }
}
