package com.nhn.pinpoint.profiler.monitor.metric;

/**
 * @author emeroad
 */
public interface AcceptHistogram {
    public boolean addResponseTime(String parentApplicationName, short serviceType, int millis);
}
