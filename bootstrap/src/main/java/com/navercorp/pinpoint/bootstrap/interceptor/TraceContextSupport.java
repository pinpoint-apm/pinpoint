package com.navercorp.pinpoint.bootstrap.interceptor;

import com.navercorp.pinpoint.bootstrap.context.TraceContext;

/**
 * @author emeroad
 */
public interface TraceContextSupport {
    void setTraceContext(TraceContext traceContext);
}
