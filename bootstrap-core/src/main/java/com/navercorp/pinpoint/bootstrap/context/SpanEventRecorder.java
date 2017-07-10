package com.navercorp.pinpoint.bootstrap.context;

import com.navercorp.pinpoint.common.annotations.InterfaceStability;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.trace.ServiceType;

public interface SpanEventRecorder extends FrameAttachment {

    void recordTime(boolean time);
    
    void recordException(Throwable throwable);

    void recordException(boolean markError, Throwable throwable);

    void recordApiId(int apiId);

    void recordApi(MethodDescriptor methodDescriptor);

    void recordApi(MethodDescriptor methodDescriptor, Object[] args);

    void recordApi(MethodDescriptor methodDescriptor, Object args, int index);

    void recordApi(MethodDescriptor methodDescriptor, Object[] args, int start, int end);

    void recordApiCachedString(MethodDescriptor methodDescriptor, String args, int index);

    ParsingResult recordSqlInfo(String sql);

    void recordSqlParsingResult(ParsingResult parsingResult);

    void recordSqlParsingResult(ParsingResult parsingResult, String bindValue);

    void recordAttribute(AnnotationKey key, String value);

    void recordAttribute(AnnotationKey key, int value);

    void recordAttribute(AnnotationKey key, Object value);

    void recordServiceType(ServiceType serviceType);

    void recordRpcName(String rpc);

    void recordDestinationId(String destinationId);

    void recordEndPoint(String endPoint);

    void recordNextSpanId(long spanId);

    /**
     * @since 1.7.0
     */
    @InterfaceStability.Evolving
    AsyncContext recordNextAsyncContext();

    /**
     * @since 1.7.0
     */
    @InterfaceStability.Unstable
    AsyncContext recordNextAsyncContext(boolean stateful);

    /**
     * @deprecated Since 1.7.0 Use {@link SpanEventRecorder#recordNextAsyncContext()}
     * This API will be removed in 1.8.0
     */
    @Deprecated
    void recordAsyncId(int asyncId);

    /**
     * @deprecated Since 1.7.0 Use {@link SpanEventRecorder#recordNextAsyncContext()}
     * This API will be removed in 1.8.0
     */
    @Deprecated
    void recordNextAsyncId(int asyncId);

    /**
     * @deprecated Since 1.7.0 Use {@link SpanEventRecorder#recordNextAsyncContext()}
     * This API will be removed in 1.8.0
     */
    @Deprecated
    void recordAsyncSequence(short sequence);
}