package com.navercorp.pinpoint.bootstrap.interceptor.tracevalue;

import com.navercorp.pinpoint.bootstrap.context.DatabaseInfo;

/**
 * @author emeroad
 */
public interface DatabaseInfoTraceValue extends TraceValue {
    void __setTraceDatabaseInfo(DatabaseInfo value);

    DatabaseInfo __getTraceDatabaseInfo();

}
