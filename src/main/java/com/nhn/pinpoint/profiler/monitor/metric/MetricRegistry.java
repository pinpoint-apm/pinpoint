package com.nhn.pinpoint.profiler.monitor.metric;

import com.nhn.pinpoint.common.HistogramSchema;
import com.nhn.pinpoint.common.ServiceType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author emeroad
 */
public class MetricRegistry {

    private final ConcurrentMap<Short, ResponseMetric> rpcCache = new ConcurrentHashMap<Short, ResponseMetric>(128, 0.75f, 128);

    private final ResponseMetric responseMetric;
    private final ServiceType serviceType;


    public MetricRegistry(ServiceType serviceType) {
        if (serviceType == null) {
            throw new NullPointerException("serviceType must not be null");
        }
        this.serviceType = serviceType;
        this.responseMetric = new DefaultResponseMetric(serviceType);
    }

    public ResponseMetric getRpcMetric(ServiceType serviceType) {
        final Short code = serviceType.getCode();
        final ResponseMetric hit = rpcCache.get(code);
        if ( hit!= null) {
            return hit;
        }
        final ResponseMetric responseMetric = new DefaultResponseMetric(serviceType);
        final ResponseMetric exist = rpcCache.putIfAbsent(code, responseMetric);
        if (exist != null) {
            return exist;
        }

        return responseMetric;
    }

    public Collection<HistogramSnapshot> createSnapshot() {
        final List<HistogramSnapshot> histogramSnapshotList = new ArrayList<HistogramSnapshot>(16);
        for (ResponseMetric metric : rpcCache.values()) {
            histogramSnapshotList.addAll(metric.createSnapshotList());
        }
        return histogramSnapshotList;
    }

    public ResponseMetric getWasResponseMetric() {
        return responseMetric;
    }
}
