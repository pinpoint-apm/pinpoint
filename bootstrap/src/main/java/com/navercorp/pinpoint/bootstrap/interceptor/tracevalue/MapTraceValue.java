package com.nhn.pinpoint.bootstrap.interceptor.tracevalue;

import java.util.Map;

/**
 * 
 * @author jaehong.kim
 *
 */
public interface MapTraceValue extends TraceValue {
    void __setTraceBindValue(Map<String, Object> value);

    Map<String, Object> __getTraceBindValue();
}
