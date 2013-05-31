package com.nhn.pinpoint.interceptor;

import com.nhn.pinpoint.context.TraceContext;

/**
 *
 */
public interface TraceContextSupport {
    void setTraceContext(TraceContext traceContext);
}
