package com.profiler.context;

import com.profiler.common.util.ParsingResult;
import com.profiler.interceptor.MethodDescriptor;

/**
 *
 */
public interface TraceContext {

    Trace currentTraceObject();

    void attachTraceObject(DefaultTrace trace);

    void detachTraceObject();

    ActiveThreadCounter getActiveThreadCounter();

    void setAgentId(String agentId);

    String getAgentId();

    void setApplicationId(String applicationId);

    String getApplicationId();

    int cacheApi(MethodDescriptor methodDescriptor);

    ParsingResult parseSql(String sql);
}
