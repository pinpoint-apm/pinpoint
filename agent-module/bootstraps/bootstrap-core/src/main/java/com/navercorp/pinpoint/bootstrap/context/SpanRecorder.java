package com.navercorp.pinpoint.bootstrap.context;

import com.navercorp.pinpoint.common.trace.LoggingInfo;
import com.navercorp.pinpoint.common.trace.ServiceType;

public interface SpanRecorder extends FrameAttachment, AttributeRecorder, ErrorRecorder {

    boolean canSampled();

    boolean isRoot();

    void recordStartTime(long startTime);

    void recordTime(boolean autoTimeRecoding);

    default void recordException(Throwable throwable) {
        recordException(true, throwable);
    }

    void recordException(boolean markError, Throwable throwable);

    void recordApiId(int apiId);

    void recordApi(MethodDescriptor methodDescriptor);

    void recordApi(MethodDescriptor methodDescriptor, Object[] args);

    void recordApi(MethodDescriptor methodDescriptor, Object args, int index);

    void recordApi(MethodDescriptor methodDescriptor, Object[] args, int start, int end);

    void recordApiCachedString(MethodDescriptor methodDescriptor, String args, int index);

    void recordServiceType(ServiceType serviceType);

    void recordRpcName(String rpc);

    void recordRemoteAddress(String remoteAddress);
    
    void recordEndPoint(String endPoint);

    void recordParentApplication(String parentApplicationName, short parentApplicationType);

    void recordAcceptorHost(String host);
    
    void recordLogging(LoggingInfo loggingInfo);

    void recordStatusCode(int statusCode);

    boolean recordUriTemplate(String uriTemplate);

    boolean recordUriTemplate(String uriTemplate, boolean force);

    boolean recordUriHttpMethod(String httpMethod);

    void recordParentServiceName(String parentServiceName);
}
