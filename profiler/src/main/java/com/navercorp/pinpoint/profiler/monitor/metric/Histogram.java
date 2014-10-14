package com.nhn.pinpoint.profiler.monitor.metric;

import com.nhn.pinpoint.bootstrap.context.Metric;

/**
 * @author emeroad
 */
public interface Histogram extends Metric {
    short getServiceType();

    void addResponseTime(int millis);

    HistogramSnapshot createSnapshot();
}
