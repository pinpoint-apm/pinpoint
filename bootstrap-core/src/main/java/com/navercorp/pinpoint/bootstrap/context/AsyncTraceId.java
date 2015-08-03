package com.navercorp.pinpoint.bootstrap.context;

public interface AsyncTraceId extends TraceId {

    int getAsyncId();
    
    long getSpanStartTime();
    
    TraceId getParentTraceId();
    
    short nextAsyncSequence();
}
