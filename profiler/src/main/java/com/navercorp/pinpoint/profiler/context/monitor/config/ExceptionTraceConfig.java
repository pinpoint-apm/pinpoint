package com.navercorp.pinpoint.profiler.context.monitor.config;

public interface ExceptionTraceConfig {
    boolean isExceptionTraceEnable();

    double getExceptionTraceNewThroughput();

    int getExceptionTraceMaxDepth();

    int getIoBufferingBufferSize();
}
