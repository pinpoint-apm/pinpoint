package com.navercorp.pinpoint.plugin.fastjson.interceptor;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.context.*;
import com.navercorp.pinpoint.bootstrap.context.scope.TraceScope;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.JdbcContext;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.trace.ServiceType;
import org.junit.Test;

public class ParseObjectInterceptorTest {

    private Trace trace = new Trace() {
        @Override
        public long getId() {
            return 0;
        }

        @Override
        public long getStartTime() {
            return 0;
        }

        @Override
        public Thread getBindThread() {
            return null;
        }

        @Override
        public long getThreadId() {
            return 0;
        }

        @Override
        public TraceId getTraceId() {
            return null;
        }

        @Override
        public AsyncTraceId getAsyncTraceId() {
            return null;
        }

        @Override
        public boolean canSampled() {
            return false;
        }

        @Override
        public boolean isRoot() {
            return false;
        }

        @Override
        public boolean isAsync() {
            return false;
        }

        @Override
        public SpanRecorder getSpanRecorder() {
            return null;
        }

        @Override
        public SpanEventRecorder currentSpanEventRecorder() {
            return new SpanEventRecorder() {
                @Override
                public void recordTime(boolean b) {

                }

                @Override
                public void recordException(Throwable throwable) {

                }

                @Override
                public void recordException(boolean b, Throwable throwable) {

                }

                @Override
                public void recordApiId(int i) {

                }

                @Override
                public void recordApi(MethodDescriptor methodDescriptor) {

                }

                @Override
                public void recordApi(MethodDescriptor methodDescriptor, Object[] objects) {

                }

                @Override
                public void recordApi(MethodDescriptor methodDescriptor, Object o, int i) {

                }

                @Override
                public void recordApi(MethodDescriptor methodDescriptor, Object[] objects, int i, int i1) {

                }

                @Override
                public void recordApiCachedString(MethodDescriptor methodDescriptor, String s, int i) {

                }

                @Override
                public ParsingResult recordSqlInfo(String s) {
                    return null;
                }

                @Override
                public void recordSqlParsingResult(ParsingResult parsingResult) {

                }

                @Override
                public void recordSqlParsingResult(ParsingResult parsingResult, String s) {

                }

                @Override
                public void recordAttribute(AnnotationKey annotationKey, String s) {

                }

                @Override
                public void recordAttribute(AnnotationKey annotationKey, int i) {

                }

                @Override
                public void recordAttribute(AnnotationKey annotationKey, Object o) {

                }

                @Override
                public void recordServiceType(ServiceType serviceType) {

                }

                @Override
                public void recordRpcName(String s) {

                }

                @Override
                public void recordDestinationId(String s) {

                }

                @Override
                public void recordEndPoint(String s) {

                }

                @Override
                public void recordNextSpanId(long l) {

                }

                @Override
                public AsyncContext recordNextAsyncContext() {
                    return null;
                }

                @Override
                public AsyncContext recordNextAsyncContext(boolean b) {
                    return null;
                }

                @Override
                public void recordAsyncId(int i) {

                }

                @Override
                public void recordNextAsyncId(int i) {

                }

                @Override
                public void recordAsyncSequence(short i) {

                }

                @Override
                public Object attachFrameObject(Object o) {
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
            };
        }

        @Override
        public boolean isClosed() {
            return false;
        }

        @Override
        public void close() {

        }

        @Override
        public TraceScope getScope(String s) {
            return null;
        }

        @Override
        public TraceScope addScope(String s) {
            return null;
        }

        @Override
        public SpanEventRecorder traceBlockBegin() {
            return null;
        }

        @Override
        public SpanEventRecorder traceBlockBegin(int i) {
            return null;
        }

        @Override
        public void traceBlockEnd() {

        }

        @Override
        public void traceBlockEnd(int i) {

        }

        @Override
        public boolean isRootStack() {
            return false;
        }

        @Override
        public int getCallStackFrameId() {
            return 0;
        }
    };

    private ParseObjectInterceptor interceptor = new ParseObjectInterceptor(new TraceContext() {
        @Override
        public Trace currentTraceObject() {
            return trace;
        }

        @Override
        public Trace currentRawTraceObject() {
            return trace;
        }

        @Override
        public Trace continueTraceObject(TraceId traceId) {
            return trace;
        }

        @Override
        public Trace continueTraceObject(Trace trace) {
            return trace;
        }

        @Override
        public Trace newTraceObject() {
            return trace;
        }

        @Override
        public Trace newAsyncTraceObject() {
            return trace;
        }

        @Override
        public Trace continueAsyncTraceObject(TraceId traceId) {
            return trace;
        }

        @Override
        public Trace continueAsyncTraceObject(AsyncTraceId asyncTraceId, int i, long l) {
            return trace;
        }

        @Override
        public Trace removeTraceObject() {
            return trace;
        }

        @Override
        public Trace removeTraceObject(boolean b) {
            return trace;
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
        public int cacheString(String s) {
            return 0;
        }

        @Override
        public ParsingResult parseSql(String s) {
            return null;
        }

        @Override
        public boolean cacheSql(ParsingResult parsingResult) {
            return false;
        }

        @Override
        public TraceId createTraceId(String s, long l, long l1, short i) {
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
        public void setApiId(int i) {

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
    public void before() {
        interceptor.before(null, null);
    }

    @Test
    public void after() {
        interceptor.after(null, new Object[]{null}, null, null);
    }
}