package com.navercorp.pinpoint.profiler.context;

import com.navercorp.pinpoint.profiler.context.id.TraceRoot;

public interface SqlCountService {
    void recordSqlCount(TraceRoot traceRoot);
}
