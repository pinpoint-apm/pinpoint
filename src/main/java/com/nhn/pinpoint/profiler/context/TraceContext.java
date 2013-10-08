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

    String getAgentId();

    String getApplicationName();

    short getServerTypeCode();

    String getServerType();

    int cacheApi(MethodDescriptor methodDescriptor);

    ParsingResult parseSql(String sql);

    DatabaseInfo parseJdbcUrl(String sql);

    TraceId createTraceId(String transactionId, int parentSpanID, int spanID, short flags);

    void disableSampling();
}
