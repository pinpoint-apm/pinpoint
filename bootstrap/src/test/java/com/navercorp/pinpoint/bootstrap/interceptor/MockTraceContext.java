package com.nhn.pinpoint.bootstrap.interceptor;

import com.nhn.pinpoint.bootstrap.config.ProfilerConfig;
import com.nhn.pinpoint.bootstrap.context.*;
import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.common.util.ParsingResult;

/**
 * @author emeroad
 * @author hyungil.jeong
 */
public class MockTraceContext implements TraceContext {

    private Trace trace;

    public void setTrace(Trace trace) {
        this.trace = trace;
    }

    @Override
    public Trace currentTraceObject() {
        if (trace == null) {
            return null;
        }
        if (trace.canSampled()) {
            return null;
        }
        return trace;
    }

    @Override
    public Trace currentRawTraceObject() {
        return trace;
    }

    @Override
    public Trace continueTraceObject(TraceId traceID) {
        return trace;
    }

    @Override
    public Trace newTraceObject() {
        return trace;
    }

    @Override
    public void detachTraceObject() {
        trace = null;
    }

    @Override
    public String getAgentId() {
        return null;
    }

    @Override
    public String getApplicationName() {
        return null;
    }

    @Override
    public long getAgentStartTime() {
        return 0;
    }

    @Override
    public short getServerTypeCode() {
        return 0;
    }

    @Override
    public String getServerType() {
        return null;
    }

    @Override
    public int cacheApi(MethodDescriptor methodDescriptor) {
        return 0;
    }

    @Override
    public int cacheString(String value) {
        return 0;
    }

    @Override
    public ParsingResult parseSql(String sql) {
        return null;
    }

    @Override
    public DatabaseInfo parseJdbcUrl(String sql) {
        return null;
    }

    @Override
    public DatabaseInfo createDatabaseInfo(ServiceType type, ServiceType executeQueryType, String url, int port, String databaseId) {
        return null;
    }

    @Override
    public TraceId createTraceId(String transactionId, long parentSpanID, long spanID, short flags) {
        return null;
    }

    @Override
    public Trace disableSampling() {
        return null;
    }

    @Override
    public ProfilerConfig getProfilerConfig() {
        return null;
    }

    @Override
    public Metric getRpcMetric(ServiceType serviceType) {
        return null;
    }

    @Override
    public void recordContextMetricIsError() {

    }

    @Override
    public void recordContextMetric(int elapsedTime) {

    }

	@Override
	public void recordAcceptResponseTime(String parentApplicationName, short parentApplicationType, int elapsedTime) {
		
	}

	@Override
	public void recordUserAcceptResponseTime(int elapsedTime) {
		
	}

    @Override
    public ServerMetaDataHolder getServerMetaDataHolder() {
        return null;
    }
}
