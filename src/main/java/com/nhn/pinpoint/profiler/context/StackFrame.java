package com.nhn.pinpoint.profiler.context;

/**
 *
 */
public interface StackFrame {

    int getStackFrameId();

    void setStackFrameId(int stackId);

    void markBeforeTime();

    long getBeforeTime();

    void markAfterTime();

    long getAfterTime();

    void setEndPoint(String endPoint);

    void setRpc(String rpc);

    void setApiId(int apiId);

    void setExceptionId(int id);

    void setServiceType(short serviceType);

    void addAnnotation(Annotation annotation);

    void attachObject(Object object);

    Object getAttachObject(Object object);

    Object detachObject();
}
