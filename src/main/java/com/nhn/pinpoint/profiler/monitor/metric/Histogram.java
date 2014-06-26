package com.nhn.pinpoint.profiler.monitor.metric;

import com.nhn.pinpoint.bootstrap.context.Metric;
import com.nhn.pinpoint.common.ServiceType;

/**
 * @author emeroad
 */
public interface Histogram extends Metric {
    ServiceType getServiceType();

    void addResponseTime(int millis);

    HistogramSnapshot createSnapshot();
}
