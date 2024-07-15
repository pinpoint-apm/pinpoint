package com.navercorp.pinpoint.profiler.context;

import com.navercorp.pinpoint.profiler.context.id.Shared;

public class DefaultSqlCountService implements SqlCountService {
    private final int sqlErrorLimit;

    public DefaultSqlCountService(int sqlErrorLimit) {
        this.sqlErrorLimit = sqlErrorLimit;
    }

    @Override
    public void recordSqlCount(Shared shared) {
        boolean isError = shared.getErrorCode() != 0;
        if (isError) {
            return;
        }

        int sqlExecutionCount = shared.incrementAndGetSqlCount();
        if (sqlExecutionCount >= sqlErrorLimit) {
            recordError(shared);
        }
    }

    private void recordError(Shared shared) {
        shared.maskErrorCode(1);
    }
}
