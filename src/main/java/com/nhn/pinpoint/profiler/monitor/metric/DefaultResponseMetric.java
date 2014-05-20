package com.nhn.pinpoint.profiler.monitor.metric;

import com.nhn.pinpoint.common.HistogramSchema;
import com.nhn.pinpoint.common.ServiceType;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author emeroad
 */
public class DefaultResponseMetric implements ResponseMetric {

    private final ServiceType serviceType;

    private final ConcurrentMap<String, Histogram> counter = new ConcurrentHashMap<String, Histogram>(64, 0.75f, 64);

    public DefaultResponseMetric(ServiceType serviceType) {
        if (serviceType == null) {
            throw new NullPointerException("serviceType must not be null");
        }
        this.serviceType = serviceType;
    }


    @Override
    public void addResponseTime(String destinationId, int millis) {
        if (destinationId == null) {
            throw new NullPointerException("destinationId must not be null");
        }
        Histogram histogram = getHistogram0(destinationId);
        histogram.addResponseTime(millis);
    }

    private Histogram getHistogram0(String destinationId) {
        final Histogram hit = counter.get(destinationId);
        if (hit != null) {
            return hit;
        }
        final HistogramSchema schema = serviceType.getHistogramSchema();
        final Histogram histogram = new Histogram(schema);

        final Histogram exist = counter.putIfAbsent(destinationId, histogram);
        if (exist != null) {
            return exist;
        }
        return histogram;
    }

    public List<HistogramSnapshot> createSnapshotList() {
        final List<HistogramSnapshot> histogramSnapshotList = new ArrayList<HistogramSnapshot>(counter.size() + 4);
        for (Histogram histogram : counter.values()) {
            final HistogramSnapshot snapshot = histogram.createSnapshot();
            histogramSnapshotList.add(snapshot);
        }
        return histogramSnapshotList;
    }
}
