package com.nhn.pinpoint.profiler.monitor.metric;

import java.util.Collection;
import java.util.List;

/**
 * @author emeroad
 */
public interface ResponseMetric {

    void addResponseTime(String destinationId, int millis);

    Collection<HistogramSnapshot> createSnapshotList();

}
