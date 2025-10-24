package com.navercorp.pinpoint.profiler.context;

import com.navercorp.pinpoint.bootstrap.context.ErrorRecorder;
import com.navercorp.pinpoint.profiler.context.id.Shared;

public interface SqlCountService {
    void recordSqlCount(Shared shared, ErrorRecorder errorRecorder);
}
