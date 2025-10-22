package com.navercorp.pinpoint.profiler.context;

import com.navercorp.pinpoint.bootstrap.context.ErrorRecorder;
import com.navercorp.pinpoint.common.trace.ErrorCategory;
import com.navercorp.pinpoint.profiler.context.id.Shared;

public class DefaultSqlCountService implements SqlCountService {
    private final int sqlErrorLimit;

    public DefaultSqlCountService(int sqlErrorLimit) {
        this.sqlErrorLimit = sqlErrorLimit;
    }

    @Override
    public void recordSqlCount(Shared shared, ErrorRecorder errorRecorder) {
        boolean isError = shared.getErrorCode() != 0;
        if (isError) {
            return;
        }

        int sqlExecutionCount = shared.incrementAndGetSqlCount();
        if (sqlExecutionCount >= sqlErrorLimit) {
            errorRecorder.recordError(ErrorCategory.SQL);
        }
    }
}
