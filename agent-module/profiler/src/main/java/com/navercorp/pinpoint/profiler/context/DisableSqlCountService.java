package com.navercorp.pinpoint.profiler.context;

import com.navercorp.pinpoint.profiler.context.id.Shared;

public class DisableSqlCountService implements SqlCountService {
    @Override
    public void recordSqlCount(Shared shared) {
    }
}
