package com.nhn.pinpoint.profiler.interceptor;

import com.nhn.pinpoint.profiler.context.TraceContext;

/**
 *
 */
public interface TraceContextSupport {
    void setTraceContext(TraceContext traceContext);
}
