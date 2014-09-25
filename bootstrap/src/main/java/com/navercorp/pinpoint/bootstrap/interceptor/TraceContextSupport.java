package com.nhn.pinpoint.bootstrap.interceptor;

import com.nhn.pinpoint.bootstrap.context.TraceContext;

/**
 * @author emeroad
 */
public interface TraceContextSupport {
    void setTraceContext(TraceContext traceContext);
}
