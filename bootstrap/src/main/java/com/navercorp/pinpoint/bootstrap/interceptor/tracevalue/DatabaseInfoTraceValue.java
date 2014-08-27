package com.nhn.pinpoint.bootstrap.interceptor.tracevalue;

import com.nhn.pinpoint.bootstrap.context.DatabaseInfo;

/**
 * @author emeroad
 */
public interface DatabaseInfoTraceValue extends TraceValue {
    void __setTraceDatabaseInfo(DatabaseInfo value);

    DatabaseInfo __getTraceDatabaseInfo();

}
