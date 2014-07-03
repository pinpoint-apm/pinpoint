package com.nhn.pinpoint.bootstrap.interceptor.tracevalue;

/**
 * @author emeroad
 */
public interface IntTraceValue extends TraceValue {
    void __setTraceInt(int value);

    int __getTraceInt();
}
