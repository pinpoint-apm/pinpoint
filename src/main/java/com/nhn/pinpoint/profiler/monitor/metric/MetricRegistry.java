package com.nhn.pinpoint.profiler.monitor.metric;

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

    private final ConcurrentMap<Short, ResponseMetric> rpcCache = new ConcurrentHashMap<Short, ResponseMetric>();

    private final Histogram responseMetric;


    public MetricRegistry(ServiceType serviceType) {
        if (serviceType == null) {
            throw new NullPointerException("serviceType must not be null");
        }
        if (!serviceType.isWas()) {
            throw new IllegalArgumentException("illegal serviceType:" + serviceType);
        }
        this.responseMetric = new Histogram(serviceType);
    }

    public ResponseMetric getRpcMetric(ServiceType serviceType) {
        if (serviceType == null) {
            throw new NullPointerException("serviceType must not be null");
        }
        if (!serviceType.isRecordStatistics()) {
            throw new IllegalArgumentException("illegal serviceType:" + serviceType);
        }
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

    public void addResponseTime(int mills) {
        this.responseMetric.addResponseTime(mills);
    }

    public Collection<HistogramSnapshot> createRpcResponseSnapshot() {
        final List<HistogramSnapshot> histogramSnapshotList = new ArrayList<HistogramSnapshot>(16);
        for (ResponseMetric metric : rpcCache.values()) {
            histogramSnapshotList.addAll(metric.createSnapshotList());
        }
        return histogramSnapshotList;
    }

    public HistogramSnapshot createWasResponseSnapshot() {
        return responseMetric.createSnapshot();
    }
}
