package com.profiler.interceptor;

import com.profiler.context.TraceContext;

/**
 *
 */
public interface TraceContextSupport {
    void setTraceContext(TraceContext traceContext);
}
