package com.navercorp.pinpoint.profiler.context;

import com.navercorp.pinpoint.profiler.context.id.Shared;

public interface SqlCountService {
    void recordSqlCount(Shared shared);
}
