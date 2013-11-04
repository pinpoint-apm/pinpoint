package com.nhn.pinpoint.profiler.context;

import com.nhn.pinpoint.common.AnnotationKey;
import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.profiler.interceptor.MethodDescriptor;

import java.util.TimerTask;

/**
 * @author emeroad
 */
public interface AsyncTrace {
    public static final int STATE_INIT = 0;
    public static final int STATE_FIRE = 1;
    public static final int STATE_TIMEOUT = 2;

    int getState();
    boolean fire();

    void setTimeoutTask(TimerTask timeoutTask);

    void setAsyncId(int asyncId);

    int getAsyncId();

    Object getAttachObject();

    void setAttachObject(Object attachObject);

    void traceBlockBegin();

    void markBeforeTime();

    long getBeforeTime();

    void traceBlockEnd();

    void markAfterTime();

    void recordApi(MethodDescriptor methodDescriptor);

    void recordException(Object result);

    void recordAttribute(AnnotationKey key, String value);

    void recordAttribute(AnnotationKey key, int value);

    void recordAttribute(AnnotationKey key, Object value);

    void recordServiceType(ServiceType serviceType);

    void recordRpcName(String rpcName);

    void recordDestinationId(String destinationId);

    // TODO: final String... endPoint로 받으면 합치는데 비용이 들어가 그냥 한번에 받는게 나을것 같음.
    void recordEndPoint(String endPoint);
}
