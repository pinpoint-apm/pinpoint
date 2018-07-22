package com.navercorp.pinpoint.plugin.druid.interceptor;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.context.*;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.JdbcContext;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.trace.ServiceType;
import org.junit.Test;

public class DataSourceGetConnectionInterceptorTest {

    private DataSourceGetConnectionInterceptor interceptor = new DataSourceGetConnectionInterceptor(new TraceContext() {
        @Override
        public Trace currentTraceObject() {
            return null;
        }

        @Override
        public Trace currentRawTraceObject() {
            return null;
        }

        @Override
        public Trace continueTraceObject(TraceId traceId) {
            return null;
        }

        @Override
        public Trace continueTraceObject(Trace trace) {
            return null;
        }

        @Override
        public Trace newTraceObject() {
            return null;
        }

        @Override
        public Trace newAsyncTraceObject() {
            return null;
        }

        @Override
        public Trace continueAsyncTraceObject(TraceId traceId) {
            return null;
        }

        @Override
        public Trace continueAsyncTraceObject(AsyncTraceId traceId, int asyncId, long startTime) {
            return null;
        }

        @Override
        public Trace removeTraceObject() {
            return null;
        }

        @Override
        public Trace removeTraceObject(boolean closeDisableTrace) {
            return null;
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
        public boolean cacheSql(ParsingResult parsingResult) {
            return false;
        }

        @Override
        public TraceId createTraceId(String transactionId, long parentSpanId, long spanId, short flags) {
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
        public ServerMetaDataHolder getServerMetaDataHolder() {
            return null;
        }

        @Override
        public int getAsyncId() {
            return 0;
        }

        @Override
        public JdbcContext getJdbcContext() {
            return null;
        }
    }, new MethodDescriptor() {
        @Override
        public String getMethodName() {
            return null;
        }

        @Override
        public String getClassName() {
            return null;
        }

        @Override
        public String[] getParameterTypes() {
            return new String[0];
        }

        @Override
        public String[] getParameterVariableName() {
            return new String[0];
        }

        @Override
        public String getParameterDescriptor() {
            return null;
        }

        @Override
        public int getLineNumber() {
            return 0;
        }

        @Override
        public String getFullName() {
            return null;
        }

        @Override
        public void setApiId(int apiId) {

        }

        @Override
        public int getApiId() {
            return 0;
        }

        @Override
        public String getApiDescriptor() {
            return null;
        }

        @Override
        public int getType() {
            return 0;
        }
    });

    @Test
    public void doInBeforeTrace() {

        interceptor.doInBeforeTrace(null, null, null);
    }

    @Test
    public void doInAfterTrace1() {

        interceptor.doInAfterTrace(new SpanEventRecorder() {
            @Override
            public void recordTime(boolean time) {

            }

            @Override
            public void recordException(Throwable throwable) {

            }

            @Override
            public void recordException(boolean markError, Throwable throwable) {

            }

            @Override
            public void recordApiId(int apiId) {

            }

            @Override
            public void recordApi(MethodDescriptor methodDescriptor) {

            }

            @Override
            public void recordApi(MethodDescriptor methodDescriptor, Object[] args) {

            }

            @Override
            public void recordApi(MethodDescriptor methodDescriptor, Object args, int index) {

            }

            @Override
            public void recordApi(MethodDescriptor methodDescriptor, Object[] args, int start, int end) {

            }

            @Override
            public void recordApiCachedString(MethodDescriptor methodDescriptor, String args, int index) {

            }

            @Override
            public ParsingResult recordSqlInfo(String sql) {
                return null;
            }

            @Override
            public void recordSqlParsingResult(ParsingResult parsingResult) {

            }

            @Override
            public void recordSqlParsingResult(ParsingResult parsingResult, String bindValue) {

            }

            @Override
            public void recordAttribute(AnnotationKey key, String value) {

            }

            @Override
            public void recordAttribute(AnnotationKey key, int value) {

            }

            @Override
            public void recordAttribute(AnnotationKey key, Object value) {

            }

            @Override
            public void recordServiceType(ServiceType serviceType) {

            }

            @Override
            public void recordRpcName(String rpc) {

            }

            @Override
            public void recordDestinationId(String destinationId) {

            }

            @Override
            public void recordEndPoint(String endPoint) {

            }

            @Override
            public void recordNextSpanId(long spanId) {

            }

            @Override
            public AsyncContext recordNextAsyncContext() {
                return null;
            }

            @Override
            public AsyncContext recordNextAsyncContext(boolean stateful) {
                return null;
            }

            @Override
            public void recordAsyncId(int asyncId) {

            }

            @Override
            public void recordNextAsyncId(int asyncId) {

            }

            @Override
            public void recordAsyncSequence(short sequence) {

            }

            @Override
            public Object attachFrameObject(Object frameObject) {
                return null;
            }

            @Override
            public Object getFrameObject() {
                return null;
            }

            @Override
            public Object detachFrameObject() {
                return null;
            }
        }, null, null, null, null);
    }

    @Test
    public void doInAfterTrace2() {

        interceptor.doInAfterTrace(new SpanEventRecorder() {
            @Override
            public void recordTime(boolean time) {

            }

            @Override
            public void recordException(Throwable throwable) {

            }

            @Override
            public void recordException(boolean markError, Throwable throwable) {

            }

            @Override
            public void recordApiId(int apiId) {

            }

            @Override
            public void recordApi(MethodDescriptor methodDescriptor) {

            }

            @Override
            public void recordApi(MethodDescriptor methodDescriptor, Object[] args) {

            }

            @Override
            public void recordApi(MethodDescriptor methodDescriptor, Object args, int index) {

            }

            @Override
            public void recordApi(MethodDescriptor methodDescriptor, Object[] args, int start, int end) {

            }

            @Override
            public void recordApiCachedString(MethodDescriptor methodDescriptor, String args, int index) {

            }

            @Override
            public ParsingResult recordSqlInfo(String sql) {
                return null;
            }

            @Override
            public void recordSqlParsingResult(ParsingResult parsingResult) {

            }

            @Override
            public void recordSqlParsingResult(ParsingResult parsingResult, String bindValue) {

            }

            @Override
            public void recordAttribute(AnnotationKey key, String value) {

            }

            @Override
            public void recordAttribute(AnnotationKey key, int value) {

            }

            @Override
            public void recordAttribute(AnnotationKey key, Object value) {

            }

            @Override
            public void recordServiceType(ServiceType serviceType) {

            }

            @Override
            public void recordRpcName(String rpc) {

            }

            @Override
            public void recordDestinationId(String destinationId) {

            }

            @Override
            public void recordEndPoint(String endPoint) {

            }

            @Override
            public void recordNextSpanId(long spanId) {

            }

            @Override
            public AsyncContext recordNextAsyncContext() {
                return null;
            }

            @Override
            public AsyncContext recordNextAsyncContext(boolean stateful) {
                return null;
            }

            @Override
            public void recordAsyncId(int asyncId) {

            }

            @Override
            public void recordNextAsyncId(int asyncId) {

            }

            @Override
            public void recordAsyncSequence(short sequence) {

            }

            @Override
            public Object attachFrameObject(Object frameObject) {
                return null;
            }

            @Override
            public Object getFrameObject() {
                return null;
            }

            @Override
            public Object detachFrameObject() {
                return null;
            }
        }, null, new Object[]{"", ""}, null, null);
    }
}