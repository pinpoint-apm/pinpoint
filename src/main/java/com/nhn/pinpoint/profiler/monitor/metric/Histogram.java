package com.nhn.pinpoint.profiler.monitor.metric;

import com.nhn.pinpoint.common.ServiceType;

/**
 * @author emeroad
 */
public interface Histogram {
    ServiceType getServiceType();

    void addResponseTime(int millis);

    HistogramSnapshot createSnapshot();
}
