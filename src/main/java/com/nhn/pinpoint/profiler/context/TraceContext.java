package com.nhn.pinpoint.profiler.context;

import com.nhn.pinpoint.common.util.ParsingResult;
import com.nhn.pinpoint.profiler.interceptor.MethodDescriptor;

/**
 *
 */
public interface TraceContext {

    Trace currentTraceObject();

    /**
     * sampling rate를 추가적으로 확인해야 되는 trace를 리턴한다.
     * @return
     */
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

    int cacheString(String value);

    ParsingResult parseSql(String sql);

    DatabaseInfo parseJdbcUrl(String sql);

    TraceId createTraceId(String transactionId, int parentSpanID, int spanID, short flags);

    void disableSampling();
}
