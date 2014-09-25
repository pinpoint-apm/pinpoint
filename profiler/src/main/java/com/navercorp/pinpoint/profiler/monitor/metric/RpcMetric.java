package com.nhn.pinpoint.profiler.monitor.metric;

import com.nhn.pinpoint.bootstrap.context.Metric;

import java.util.Collection;

/**
 * @author emeroad
 */
public interface RpcMetric extends Metric {

    void addResponseTime(String destinationId, int millis);

    Collection<HistogramSnapshot> createSnapshotList();

}
