package com.navercorp.pinpoint.profiler.context.monitor.config;

import com.navercorp.pinpoint.common.config.Value;

public class DefaultExceptionTraceConfig implements ExceptionTraceConfig {
    @Value("${profiler.exceptiontrace.enable}")
    private boolean exceptionTraceEnable = false;
    @Value("${profiler.exceptiontrace.new.throughput}")
    private double exceptionTraceNewThroughPut = 1000;
    @Value("${profiler.exceptiontrace.errormessage.max}")
    private int errorMessageMaxLength = 2048;
    @Value("${profiler.exceptiontrace.max.depth}")
    private int exceptionTraceMaxDepth = 0;
    @Value("${profiler.exceptiontrace.io.buffering.buffersize}")
    private int ioBufferingBufferSize = 20;

    @Override
    public boolean isExceptionTraceEnable() {
        return exceptionTraceEnable;
    }

    @Override
    public double getExceptionTraceNewThroughput() {
        return exceptionTraceNewThroughPut;
    }

    @Override
    public int getErrorMessageMaxLength() {
        return errorMessageMaxLength;
    }

    @Override
    public int getExceptionTraceMaxDepth() {
        return exceptionTraceMaxDepth;
    }

    @Override
    public int getIoBufferingBufferSize() {
        return ioBufferingBufferSize;
    }

    @Override
    public String toString() {
        return "DefaultExceptionTraceConfig{" +
                "exceptionTraceEnable=" + exceptionTraceEnable +
                ", exceptionTraceNewThroughPut=" + exceptionTraceNewThroughPut +
                ", errorMessageMaxLength=" + errorMessageMaxLength +
                ", exceptionTraceMaxDepth=" + exceptionTraceMaxDepth +
                ", ioBufferingBufferSize=" + ioBufferingBufferSize +
                '}';
    }
}
