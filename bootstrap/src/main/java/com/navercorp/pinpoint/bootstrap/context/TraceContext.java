package com.nhn.pinpoint.bootstrap.context;

import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.common.util.ParsingResult;
import com.nhn.pinpoint.bootstrap.config.ProfilerConfig;
import com.nhn.pinpoint.bootstrap.interceptor.MethodDescriptor;

/**
 * @author emeroad
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

    long getAgentStartTime();

    short getServerTypeCode();

    String getServerType();

    int cacheApi(MethodDescriptor methodDescriptor);

    int cacheString(String value);

    ParsingResult parseSql(String sql);

    DatabaseInfo parseJdbcUrl(String sql);

    DatabaseInfo createDatabaseInfo(ServiceType type, ServiceType executeQueryType, String url, int port, String databaseId);

    TraceId createTraceId(String transactionId, long parentSpanID, long spanID, short flags);

    Trace disableSampling();

    ProfilerConfig getProfilerConfig();

    Metric getRpcMetric(ServiceType serviceType);

    void recordContextMetricIsError();

    void recordContextMetric(int elapsedTime);

    void recordAcceptResponseTime(String parentApplicationName, short parentApplicationType, int elapsedTime);

    void recordUserAcceptResponseTime(int elapsedTime);
}
