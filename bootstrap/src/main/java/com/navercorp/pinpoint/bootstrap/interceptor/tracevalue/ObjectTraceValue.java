package com.navercorp.pinpoint.bootstrap.interceptor.tracevalue;

/**
 * @author emeroad
 */
public interface ObjectTraceValue extends TraceValue {

    void __setTraceObject(Object value);

    Object __getTraceObject();
}
