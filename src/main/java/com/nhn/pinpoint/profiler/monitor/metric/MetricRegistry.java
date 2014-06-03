package com.nhn.pinpoint.profiler.monitor.metric;

import com.nhn.pinpoint.bootstrap.context.Metric;
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

    private final ConcurrentMap<Short, RpcMetric> rpcCache = new ConcurrentHashMap<Short, RpcMetric>();

    private final Histogram responseMetric;


    public MetricRegistry(ServiceType serviceType) {
        if (serviceType == null) {
            throw new NullPointerException("serviceType must not be null");
        }
        if (!serviceType.isWas()) {
            throw new IllegalArgumentException("illegal serviceType:" + serviceType);
        }
        this.responseMetric = new LongAdderHistogram(serviceType);
    }

    public RpcMetric getRpcMetric(ServiceType serviceType) {
        if (serviceType == null) {
            throw new NullPointerException("serviceType must not be null");
        }
        if (!serviceType.isRecordStatistics()) {
            throw new IllegalArgumentException("illegal serviceType:" + serviceType);
        }
        final Short code = serviceType.getCode();
        final RpcMetric hit = rpcCache.get(code);
        if ( hit!= null) {
            return hit;
        }
        final RpcMetric rpcMetric = new DefaultRpcMetric(serviceType);
        final RpcMetric exist = rpcCache.putIfAbsent(code, rpcMetric);
        if (exist != null) {
            return exist;
        }

        return rpcMetric;
    }

    public Metric getResponseMetric() {
        return responseMetric;
    }

    public void addResponseTime(int mills) {
        this.responseMetric.addResponseTime(mills);
    }

    public Collection<HistogramSnapshot> createRpcResponseSnapshot() {
        final List<HistogramSnapshot> histogramSnapshotList = new ArrayList<HistogramSnapshot>(16);
        for (RpcMetric metric : rpcCache.values()) {
            histogramSnapshotList.addAll(metric.createSnapshotList());
        }
        return histogramSnapshotList;
    }

    public HistogramSnapshot createWasResponseSnapshot() {
        return responseMetric.createSnapshot();
    }
}
