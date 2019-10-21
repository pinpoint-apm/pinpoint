/*
 * Copyright 2017 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.profiler.monitor.metric.rpc;

import com.navercorp.pinpoint.common.trace.ServiceType;

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

    private final ContextMetric contextMetric;


    public MetricRegistry(ServiceType serviceType) {
        if (serviceType == null) {
            throw new NullPointerException("serviceType");
        }
        if (!serviceType.isWas()) {
            throw new IllegalArgumentException("illegal serviceType:" + serviceType);
        }

        this.contextMetric = new ContextMetric(serviceType);
    }

    public RpcMetric getRpcMetric(ServiceType serviceType) {
        if (serviceType == null) {
            throw new NullPointerException("serviceType");
        }
        if (!serviceType.isRecordStatistics()) {
            throw new IllegalArgumentException("illegal serviceType:" + serviceType);
        }
        final Short code = serviceType.getCode();
        final RpcMetric hit = rpcCache.get(code);
        if (hit != null) {
            return hit;
        }
        final RpcMetric rpcMetric = new DefaultRpcMetric(serviceType);
        final RpcMetric exist = rpcCache.putIfAbsent(code, rpcMetric);
        if (exist != null) {
            return exist;
        }

        return rpcMetric;
    }

    public ContextMetric getResponseMetric() {
        return contextMetric;
    }

    public void addResponseTime(int mills, boolean error) {
        this.contextMetric.addResponseTime(mills, error);
    }

    public Collection<HistogramSnapshot> createRpcResponseSnapshot() {
        final List<HistogramSnapshot> histogramSnapshotList = new ArrayList<HistogramSnapshot>(16);
        for (RpcMetric metric : rpcCache.values()) {
            histogramSnapshotList.addAll(metric.createSnapshotList());
        }
        return histogramSnapshotList;
    }

    public HistogramSnapshot createWasResponseSnapshot() {
        return null;
    }
}
