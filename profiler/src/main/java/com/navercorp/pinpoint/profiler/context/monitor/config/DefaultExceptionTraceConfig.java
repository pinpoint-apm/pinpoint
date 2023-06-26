package com.navercorp.pinpoint.profiler.context.monitor.config;

import com.navercorp.pinpoint.common.config.Value;

public class DefaultExceptionTraceConfig implements ExceptionTraceConfig {
    @Value("${profiler.exceptiontrace.enable}")
    private boolean exceptionTraceEnable = false;
    @Value("${profiler.exceptiontrace.new.throughput}")
    private double exceptionTraceNewThroughPut = 1000;
    @Value("${profiler.exceptiontrace.max.depth}")
    private int exceptionTraceMaxDepth = 0;

    @Override
    public boolean isExceptionTraceEnable() {
        return exceptionTraceEnable;
    }

    @Override
    public double getExceptionTraceNewThroughput() {
        return exceptionTraceNewThroughPut;
    }

    @Override
    public int getExceptionTraceMaxDepth() {
        return exceptionTraceMaxDepth;
    }

    @Override
    public String toString() {
        return "DefaultExceptionTraceConfig{" +
                "exceptionTraceEnable=" + exceptionTraceEnable +
                ", exceptionTraceNewThroughPut=" + exceptionTraceNewThroughPut +
                ", exceptionTraceMaxDepth=" + exceptionTraceMaxDepth +
                '}';
    }
}
