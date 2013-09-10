package com.nhn.pinpoint.profiler.context;

import com.nhn.pinpoint.common.util.ParsingResult;
import com.nhn.pinpoint.profiler.interceptor.MethodDescriptor;

/**
 *
 */
public interface TraceContext {

    Trace currentTraceObject();

    Trace currentRawTraceObject();

    Trace continueTraceObject(TraceId traceID);

    Trace newTraceObject();

    void detachTraceObject();

//    ActiveThreadCounter getActiveThreadCounter();

    void setAgentId(String agentId);

    String getAgentId();

    void setApplicationId(String applicationId);

    String getApplicationId();

    int cacheApi(MethodDescriptor methodDescriptor);

    ParsingResult parseSql(String sql);

    DatabaseInfo parseJdbcUrl(String sql);

    TraceId createTraceId(String traceId, int parentSpanID, int spanID, short flags);

    void disableSampling();
}
