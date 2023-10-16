package com.navercorp.pinpoint.profiler.context.monitor.config;

public interface ExceptionTraceConfig {
    boolean isExceptionTraceEnable();

    int getErrorMessageMaxLength();

    double getExceptionTraceNewThroughput();

    int getExceptionTraceMaxDepth();

    int getIoBufferingBufferSize();
}
