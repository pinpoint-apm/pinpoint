package com.nhn.pinpoint.profiler.context;

/**
 * @author emeroad
 */
public interface StackFrame {

    int getStackFrameId();

    void setStackFrameId(int stackId);

    void markBeforeTime();

    long getBeforeTime();

    void markAfterTime();

    long getAfterTime();

    int getElapsedTime();

    void setEndPoint(String endPoint);

    void setRpc(String rpc);

    void setApiId(int apiId);

    void setExceptionInfo(int exceptionId, String exceptionMessage);

    void setServiceType(short serviceType);

    void addAnnotation(Annotation annotation);

    void attachFrameObject(Object frameObject);

    Object getFrameObject();

    Object detachFrameObject();
}
