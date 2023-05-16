package com.navercorp.pinpoint.profiler.context.recorder;

import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.context.AsyncState;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.ParsingResult;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.util.DataType;
import com.navercorp.pinpoint.profiler.context.AsyncContextFactory;
import com.navercorp.pinpoint.profiler.context.id.LocalTraceRoot;

import java.util.Objects;

/**
 * @author Woonduk Kang(emeroad)
 */
public class DisableSpanEventRecorder implements SpanEventRecorder {

    public static final String UNSUPPORTED_OPERATION = "DisableSpanEventRecorder";
    private final LocalTraceRoot traceRoot;
    private final AsyncContextFactory asyncContextFactory;
    private final AsyncState asyncState;

    public DisableSpanEventRecorder(LocalTraceRoot traceRoot,
                                    AsyncContextFactory asyncContextFactory, AsyncState asyncState) {
        this.traceRoot = Objects.requireNonNull(traceRoot, "traceRoot");
        this.asyncContextFactory = Objects.requireNonNull(asyncContextFactory, "asyncContextFactory");
        this.asyncState = asyncState;
    }

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
    public void recordServiceType(ServiceType serviceType) {

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
        return asyncContextFactory.newDisableAsyncContext(this.traceRoot);
    }

    @Override
    public AsyncContext recordNextAsyncContext(boolean stateful) {
        if (stateful && asyncState != null) {
            final AsyncState asyncState = this.asyncState;
            asyncState.setup();
            return asyncContextFactory.newDisableAsyncContext(this.traceRoot, asyncState);
        }
        return recordNextAsyncContext();
    }

    @Override
    public void recordAttribute(AnnotationKey key, String value) {

    }

    @Override
    public void recordAttribute(AnnotationKey key, int value) {

    }

    @Override
    public void recordAttribute(AnnotationKey key, Integer value) {

    }

    @Override
    public void recordAttribute(AnnotationKey key, long value) {

    }

    @Override
    public void recordAttribute(AnnotationKey key, Long value) {

    }

    @Override
    public void recordAttribute(AnnotationKey key, boolean value) {

    }

    @Override
    public void recordAttribute(AnnotationKey key, double value) {

    }

    @Override
    public void recordAttribute(AnnotationKey key, byte[] value) {

    }

    @Override
    public void recordAttribute(AnnotationKey key, DataType value) {

    }

    @Override
    public void recordAttribute(AnnotationKey key, Object value) {

    }

    @Override
    public Object attachFrameObject(Object frameObject) {
        return null;
    }

    @Override
    public Object getFrameObject() {
        throw new UnsupportedOperationException(UNSUPPORTED_OPERATION);
    }

    @Override
    public Object detachFrameObject() {
        throw new UnsupportedOperationException(UNSUPPORTED_OPERATION);
    }


}
