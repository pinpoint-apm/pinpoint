package com.profiler.context;

import com.profiler.common.util.ParsingResult;
import com.profiler.interceptor.MethodDescriptor;

import java.util.UUID;

/**
 *
 */
public interface TraceContext {

    Trace currentTraceObject();

    Trace continueTraceObject(TraceID traceID);

    Trace newTraceObject();

    void detachTraceObject();

//    ActiveThreadCounter getActiveThreadCounter();

    void setAgentId(String agentId);

    String getAgentId();

    void setApplicationId(String applicationId);

    String getApplicationId();

    int cacheApi(MethodDescriptor methodDescriptor);

    ParsingResult parseSql(String sql);

    TraceID createTraceId(UUID uuid, int parentSpanID, int spanID, boolean sampled, short flags);
}
