package com.nhn.pinpoint.bootstrap.interceptor.tracevalue;


import java.util.Map;

/**
 * @author emeroad
 */
public interface BindValueTraceValue extends TraceValue {
    void __setTraceBindValue(Map<Integer, String> value);

    Map<Integer, String> __getTraceBindValue();
}
