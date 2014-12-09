package com.navercorp.pinpoint.bootstrap.context;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.interceptor.MethodDescriptor;
import com.navercorp.pinpoint.common.ServiceType;
import com.navercorp.pinpoint.common.util.ParsingResult;

/**
 * @author emeroad
 * @author hyungil.jeong
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
    
    ServerMetaDataHolder getServerMetaDataHolder();
    
}
