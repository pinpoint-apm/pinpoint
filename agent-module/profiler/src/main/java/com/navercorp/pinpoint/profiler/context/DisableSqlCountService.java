package com.navercorp.pinpoint.profiler.context;

import com.navercorp.pinpoint.profiler.context.id.TraceRoot;

public class DisableSqlCountService implements SqlCountService {
    @Override
    public void recordSqlCount(TraceRoot traceRoot) {
    }
}
